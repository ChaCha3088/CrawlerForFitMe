package com.diva.batch.job.youtube;

import com.diva.batch.entity.Song;
import com.diva.batch.entity.YoutubeFile;
import com.diva.batch.querydsl.expression.Expression;
import com.diva.batch.querydsl.itemreader.QuerydslNoOffsetPagingItemReader;
import com.diva.batch.querydsl.itemreader.QuerydslPagingItemReader;
import com.diva.batch.querydsl.options.QuerydslNoOffsetNumberOptions;
import com.diva.batch.repository.SongRepository;
import com.diva.batch.repository.YoutubeFileRepository;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.SearchResult;
import jakarta.persistence.EntityManagerFactory;
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

import java.util.List;

import static com.diva.batch.entity.QSong.song;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class YoutubeLinkJobConfig {
    private final EntityManagerFactory emf;
    private final PlatformTransactionManager transactionManager;
    private final JobRepository jobRepository;

    private final SongRepository songRepository;
    private final YoutubeFileRepository youtubeFileRepository;

    private final HttpTransport httpTransport;
    private final JsonFactory jsonFactory;
    private final HttpRequestInitializer httpRequestInitializer;
    
    private static final int CHUNK_SIZE = 10;

    @Value("${YOUTUBE.API_KEY}")
    private String API_KEY;

    @Bean
    public Job youtubeLinkJob() {
        return new JobBuilder("youtubeLinkJob", jobRepository)
                .start(youtubeLinkJob_step1(null))
                .build();
    }

    @Bean
    @JobScope
    public Step youtubeLinkJob_step1(@Value("#{jobParameters[date]}") String date) {
        return new StepBuilder("youtubeLinkJob_step1", jobRepository)
                .<Song, Song>chunk(CHUNK_SIZE, transactionManager)
                .reader(youtubeLinkJob_reader())
                .processor(youtubeLinkJob_processor())
                .writer(youtubeLinkJob_writer())
                .build();
    }

    @Bean
    public QuerydslPagingItemReader<Song> youtubeLinkJob_reader() {
        // 1. No Offset 옵션
        QuerydslNoOffsetNumberOptions<Song, Long> options = new QuerydslNoOffsetNumberOptions<>(song.id, Expression.ASC);
        
        // 2. QueryDsl
        return new QuerydslNoOffsetPagingItemReader<>(emf, CHUNK_SIZE, options, queryFactory -> queryFactory
                .selectFrom(song)
                .where(song.youtubeFile.id.isNull())
        );
    }

    private ItemProcessor<Song, Song> youtubeLinkJob_processor() {
        return song -> {
            // 이미 유튜브 링크가 있는 경우
            if (song.getYoutubeFile() != null) {
                // 스킵
                return null;
            }
            
            YouTube youTube = new YouTube.Builder(httpTransport, jsonFactory, httpRequestInitializer).setApplicationName("CrawlerForDiva").build();
            List<SearchResult> searchResults = youTube.search().list("id,snippet")
                    .setKey(API_KEY)
                    .setQ(song.getArtist() + " " + song.getTitle())
                    .setType("video")
                    .setMaxResults(1L)
                    .execute()
                    .getItems();

            if (!searchResults.isEmpty()) {
                log.info(">>>>> searchResult = {}", searchResults.get(0));

                YoutubeFile youtubeFile = YoutubeFile.builder()
                        .url("https://www.youtube.com/watch?v=" + searchResults.get(0).getId().getVideoId())
                        .song(song)
                        .build();

                songRepository.save(song);
                youtubeFileRepository.save(youtubeFile);

                return song;
            }

            return null;
        };
    }

    @Bean
    public JpaItemWriter<Song> youtubeLinkJob_writer() {
        return new JpaItemWriterBuilder<Song>()
                .entityManagerFactory(emf)
                .build();
    }
}
