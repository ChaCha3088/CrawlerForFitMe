package com.diva.batch.job.songrange;

import static com.diva.batch.entity.QSong.song;

import com.diva.batch.entity.Song;
import com.diva.batch.entity.SongRange;
import com.diva.batch.querydsl.expression.Expression;
import com.diva.batch.querydsl.itemreader.QuerydslNoOffsetPagingItemReader;
import com.diva.batch.querydsl.itemreader.QuerydslPagingItemReader;
import com.diva.batch.querydsl.options.QuerydslNoOffsetNumberOptions;
import com.diva.batch.repository.SongRangeRepository;
import com.diva.batch.utils.RecommendArtist;
import jakarta.persistence.EntityManagerFactory;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
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
public class SongRangeJobConfig {
    private final EntityManagerFactory emf;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    private final RecommendArtist recommendArtist;
    private final SongRangeRepository songRangeRepository;

    private static final int CHUNK_SIZE = 1;
    private BufferedReader br;

    @Bean
    public Job SongRangeJob() throws FileNotFoundException {
        System.setIn(new FileInputStream("SongRange.txt"));
        br = new BufferedReader(new InputStreamReader(System.in));

        return new JobBuilder("songRangeJob", jobRepository)
            .start(songRangeStep1(null))
            .build();
    }

    @Bean
    @JobScope
    public Step songRangeStep1(@Value("#{jobParameters[date]}") String date) {
        return new StepBuilder("songRangeStep1", jobRepository)
            .<Song, Song> chunk(CHUNK_SIZE, transactionManager)
            .reader(songRangeReader())
            .processor(songRangeProcessor())
            .writer(songRangeWriter())
            .build();
    }

    @Bean
    public QuerydslPagingItemReader<Song> songRangeReader() {
        QuerydslNoOffsetNumberOptions<Song, Long> options = new QuerydslNoOffsetNumberOptions<>(
            song.id, Expression.DESC);

        return new QuerydslNoOffsetPagingItemReader<>(emf, CHUNK_SIZE, options, queryFactory -> queryFactory
            .selectFrom(song)
            .where(song.songRange.isNull())
        );
    }

    private ItemProcessor<Song, Song> songRangeProcessor() {
        return song -> {
            try {
                StringTokenizer st = new StringTokenizer(br.readLine());
                Long songId = Long.parseLong(st.nextToken());
                Long genre = Long.parseLong(st.nextToken());
                String highestNote = st.nextToken();
                int highestMidi = recommendArtist.noteToMidi(highestNote, true);

                // songId와 song의 id가 같은 경우에만 songRange을 생성한다.
                if (song.getId().equals(songId)) {
                    SongRange songRange = SongRange.builder()
                        .highestNote(highestNote)
                        .highestMidi(highestMidi)
                        .genre(genre)
                        .song(song)
                        .build();

                    songRangeRepository.save(songRange);

                    return song;
                }

                return null;

            } catch (Exception e) {
                log.error("songRangeProcessor error", e);
                return null;
            }
        };
    }

    @Bean
    public JpaItemWriter<Song> songRangeWriter() {
        return new JpaItemWriterBuilder<Song>()
            .entityManagerFactory(emf)
            .build();
    }
}
