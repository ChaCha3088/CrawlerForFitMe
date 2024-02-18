package com.diva.batch.job.albumcover;

import static com.diva.batch.entity.QSong.song;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.util.IOUtils;
import com.diva.batch.entity.Song;
import com.diva.batch.querydsl.expression.Expression;
import com.diva.batch.querydsl.itemreader.QuerydslNoOffsetPagingItemReader;
import com.diva.batch.querydsl.itemreader.QuerydslPagingItemReader;
import com.diva.batch.querydsl.options.QuerydslNoOffsetNumberOptions;
import jakarta.persistence.EntityManagerFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class AlbumCoverConfig {
    private final EntityManagerFactory emf;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    private final AmazonS3 amazonS3;

    @Value("${AWS.BUCKET}")
    private String bucket;

    private static final int CHUNK_SIZE = 10;

    @Bean
    public Job AlbumCoverJob() {
        return new JobBuilder("albumCoverJob", jobRepository)
            .start(albumCoverStep1(null))
            .build();
    }

    @Bean
    @JobScope
    public Step albumCoverStep1(@Value("#{jobParameters[date]}") String date) {
        return new StepBuilder("albumCoverStep1", jobRepository)
            .<Song, Song> chunk(CHUNK_SIZE, transactionManager)
            .reader(albumCoverReader())
            .processor(albumCoverProcessor())
            .writer(albumCoverWriter())
            .build();
    }

    @Bean
    public QuerydslPagingItemReader<Song> albumCoverReader() {
        QuerydslNoOffsetNumberOptions<Song, Long> options = new QuerydslNoOffsetNumberOptions<>(
            song.id, Expression.ASC);

        return new QuerydslNoOffsetPagingItemReader<>(emf, CHUNK_SIZE, options, queryFactory -> queryFactory
                .selectFrom(song)
        );
    }

    private ItemProcessor<Song, Song> albumCoverProcessor() {
        return song -> {
            if (!song.getCoverImg().isBlank()) {
                log.info("Already has album cover: " + song.getArtist() + " - " + song.getTitle());
                return song;
            }

            log.info("Processing song: " + song.getArtist() + " - " + song.getTitle());

            // 벅스에 검색한다.
            String bugsSearchUrl = "https://music.bugs.co.kr/search/integrated?q=";
            // artist와 title을 이용해서 검색한다.
            String searchKeyword = song.getArtist() + " " + song.getTitle();
            // searchKeyword의 띄어쓰기를 %20으로 바꾼다.
            String encodedSearchKeyword = searchKeyword.replace(" ", "%20");

            // 최종 검색 주소를 만든다.
            String searchUrl = bugsSearchUrl + encodedSearchKeyword;

            try {

                // JSoup을 이용해서 검색한다.
                // HTML을 받아온다.
                Connection connection = Jsoup.connect(searchUrl);
                connection.timeout(100_000); // 100초
                final Response response = connection.execute();

                // HTML을 파싱한다.
                final Document doc = response.parse();

                // table 태그 중에서 class가 "list trackList"인 것을 찾는다.
                Elements table = doc.getElementsByTag("table");

                Elements listTrackList = table.attr("class", "list trackList");

                // tr 태그 중에서
                Elements tr = listTrackList.get(1).getElementsByTag("tr");

                // class로 albumid를 가진 것을 찾는다.
                Elements albumIds = tr.attr("class", "albumid");

                // albumid를 가져온다.
                String albumId = albumIds.get(1).attr("albumid");

                // 뒤에 숫자 두개를 제거한다.
                String albumIdWithoutVersion = albumId.substring(0, albumId.length() - 2);

                // 다운로드 주소를 만든다.
                String downloadUrlBase = "https://image.bugsm.co.kr/album/images/original/";
                String downloadUrl = downloadUrlBase + albumIdWithoutVersion + "/" + albumId + ".jpg";

                // 다운로드를 시도한다.
                URL url = new URL(downloadUrl);
                HttpURLConnection imageConnection = (HttpURLConnection) url.openConnection();
                imageConnection.setRequestMethod("GET");
                InputStream in = imageConnection.getInputStream();

                // 성공하면 s3에 업로드한다.
                byte[] imageData = IOUtils.toByteArray(in);
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentType(imageConnection.getContentType());
                metadata.setContentLength(imageData.length);

                amazonS3.putObject(bucket, "song/" + song.getArtist() + "-" + song.getTitle() + "/coverImg.jpg", new ByteArrayInputStream(imageData), metadata);

                in.close();
                imageConnection.disconnect();

                // song에 coverImg를 업데이트한다.
                song.setCoverImg("https://diva-s3.s3.ap-northeast-2.amazonaws.com/song/" + song.getArtist() + "-" + song.getTitle() + "/coverImg.jpg");

                log.info("Success album cover: " + song.getArtist() + " - " + song.getTitle());
            } catch (Exception e) {
                log.info("Failed to download album cover: " + song.getArtist() + " - " + song.getTitle());
            }

            // 실패하면 다음곡으로 넘어간다.
            return song;
        };
    }

    @Bean
    public JpaItemWriter<Song> albumCoverWriter() {
        return new JpaItemWriterBuilder<Song>()
            .entityManagerFactory(emf)
            .build();
    }
}
