package com.diva.batch.job.youtube;

import static com.diva.batch.entity.QSongOld.songOld;
import static com.diva.batch.entity.QYoutubeFile.youtubeFile;

import com.diva.batch.entity.SongOld;
import com.diva.batch.querydsl.expression.Expression;
import com.diva.batch.querydsl.itemreader.QuerydslNoOffsetPagingItemReader;
import com.diva.batch.querydsl.itemreader.QuerydslPagingItemReader;
import com.diva.batch.querydsl.options.QuerydslNoOffsetNumberOptions;
import com.diva.batch.repository.SongOldRepository;
import com.diva.batch.repository.YoutubeFileRepository;
import com.github.kiulian.downloader.Config;
import com.github.kiulian.downloader.YoutubeDownloader;
import com.github.kiulian.downloader.downloader.request.RequestVideoFileDownload;
import com.github.kiulian.downloader.downloader.request.RequestVideoInfo;
import com.github.kiulian.downloader.downloader.response.Response;
import com.github.kiulian.downloader.model.Extension;
import com.github.kiulian.downloader.model.videos.VideoDetails;
import com.github.kiulian.downloader.model.videos.VideoInfo;
import com.github.kiulian.downloader.model.videos.formats.AudioFormat;
import com.github.kiulian.downloader.model.videos.formats.Format;
import com.github.kiulian.downloader.model.videos.formats.VideoWithAudioFormat;
import jakarta.persistence.EntityManagerFactory;
import java.io.File;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
public class YoutubeFileDownloadJobConfig {
    private final EntityManagerFactory emf;
    private final PlatformTransactionManager transactionManager;
    private final JobRepository jobRepository;

    private final SongOldRepository songOldRepository;
    private final YoutubeFileRepository youtubeFileRepository;

    private static final int CHUNK_SIZE = 10;

    @Bean
    public Job youtubeFileDownloadJob() {
        return new JobBuilder("youtubeFileDownloadJob", jobRepository)
                .start(youtubeFileDownloadJob_step1(null))
                .build();
    }

    @Bean
    @JobScope
    public Step youtubeFileDownloadJob_step1(@Value("#{jobParameters[date]}") String date) {
        return new StepBuilder("youtubeFileDownloadJob_step1", jobRepository)
                .<SongOld, SongOld>chunk(CHUNK_SIZE, transactionManager)
                .reader(youtubeFileDownloadJob_reader())
                .processor(youtubeFileDownloadJob_processor())
                .writer(youtubeFileDownloadJob_writer())
                .build();
    }

    @Bean
    public QuerydslPagingItemReader<SongOld> youtubeFileDownloadJob_reader() {
        // 1. No Offset 옵션
        QuerydslNoOffsetNumberOptions<SongOld, Long> options = new QuerydslNoOffsetNumberOptions<>(
            songOld.id, Expression.ASC);

        // 2. QueryDsl
        return new QuerydslNoOffsetPagingItemReader<>(emf, CHUNK_SIZE, options, queryFactory -> queryFactory
                .selectFrom(songOld)
                .leftJoin(songOld.youtubeFile, youtubeFile)
        );
    }

    private ItemProcessor<SongOld, SongOld> youtubeFileDownloadJob_processor() {
        return song -> {
            log.info(">>>>> searchResult = {}", song.getArtist() + "-" + song.getTitle());

            /////////////////////////////////////////////////////////////////////////////////
            // init downloader with default config
            YoutubeDownloader downloader = new YoutubeDownloader();

            // or configure after init
            Config config = downloader.getConfig();
            config.setMaxRetries(0);

            String videoId = song.getYoutubeFile().getUrl().split("v=")[1];

            // sync parsing
            RequestVideoInfo videoInfoRequest = new RequestVideoInfo(videoId);
            Response<VideoInfo> videoInfoResponse = downloader.getVideoInfo(videoInfoRequest);
            VideoInfo video = videoInfoResponse.data();

            // video details
            VideoDetails details = video.details();
            System.out.println(details.title());
            System.out.println(details.viewCount());

            // get videos formats only with audio
            List<VideoWithAudioFormat> videoWithAudioFormats = video.videoWithAudioFormats();
            videoWithAudioFormats.forEach(it -> {
                System.out.println(it.audioQuality() + ", " + it.videoQuality() + " : " + it.url());
            });

            // get audio formats
            List<AudioFormat> audioFormats = video.audioFormats();
            audioFormats.forEach(it -> {
                System.out.println(it.audioQuality() + " : " + it.url());
            });

            // get best format
            System.out.println("Best video with audio format: " + video.bestVideoWithAudioFormat());
            System.out.println("Best audio format: " + video.bestAudioFormat());

            // filtering formats
            List<Format> formats = video.findFormats(format -> format.extension() == Extension.WEBM);

            // itags can be found here - https://gist.github.com/sidneys/7095afe4da4ae58694d128b1034e01e2
            Format formatByItag = video.findFormatByItag(18); // return null if not found
            if (formatByItag != null) {
                System.out.println(formatByItag.url());
            }

            File outputDir = new File("./youtubeFile");
            Format format = video.bestAudioFormat();

            // sync downloading
            RequestVideoFileDownload videoFileDownloadRequest = new RequestVideoFileDownload(format)
                    // optional params
                    .saveTo(outputDir) // by default "videos" directory
                    .renameTo(song.getArtist() + "-" + song.getTitle()) // by default file name will be same as video title on youtube
                    .overwriteIfExists(true); // if false and file with such name already exits sufix will be added video(1).mp4

            Response<File> fileResponse = downloader.downloadVideoFile(videoFileDownloadRequest);
            File data = fileResponse.data();

            return song;
        };
    }

    @Bean
    public JpaItemWriter<SongOld> youtubeFileDownloadJob_writer() {
        return new JpaItemWriterBuilder<SongOld>()
                .entityManagerFactory(emf)
                .build();
    }
}
