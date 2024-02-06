package com.diva.batch.job.movesong;

import static com.diva.batch.entity.QSongOld.songOld;

import com.diva.batch.entity.Song;
import com.diva.batch.entity.SongOld;
import com.diva.batch.entity.SongRange;
import com.diva.batch.querydsl.expression.Expression;
import com.diva.batch.querydsl.itemreader.QuerydslNoOffsetPagingItemReader;
import com.diva.batch.querydsl.itemreader.QuerydslPagingItemReader;
import com.diva.batch.querydsl.options.QuerydslNoOffsetNumberOptions;
import com.diva.batch.repository.SongRangeRepository;
import com.diva.batch.utils.RecommendArtist;
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

@Slf4j
@Configuration
@RequiredArgsConstructor
public class MoveSongConfig {
    private final EntityManagerFactory emf;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final RecommendArtist recommendArtist;

    private final SongRangeRepository songRangeRepository;

    private static final int CHUNK_SIZE = 10;

    @Bean
    public Job moveSongJob() {
        return new JobBuilder("moveSongJob", jobRepository)
                .start(moveSongStep1(null))
                .build();
    }

    @Bean
    @JobScope
    public Step moveSongStep1(@Value("#{jobParameters[date]}") String date) {
        return new StepBuilder("moveSongStep1", jobRepository)
            .<SongOld, Song> chunk(CHUNK_SIZE, transactionManager)
            .reader(moveSongReader())
            .processor(moveSongProcessor())
            .writer(moveSongWriter())
            .build();
    }

    @Bean
    public QuerydslPagingItemReader<SongOld> moveSongReader() {
        // 1. No Offset 옵션
        QuerydslNoOffsetNumberOptions<SongOld, Long> options = new QuerydslNoOffsetNumberOptions<>(
            songOld.id, Expression.ASC);

        // 2. QueryDsl
        return new QuerydslNoOffsetPagingItemReader<>(emf, CHUNK_SIZE, options, queryFactory -> queryFactory
                .selectFrom(songOld)
        );
    }

    private ItemProcessor<SongOld, Song> moveSongProcessor() {
        return songOld -> {
            Song song;

            // https://diva-s3.s3.ap-northeast-2.amazonaws.com/song/10cm-폰서트/10cm-폰서트_MR.mp3
            String newMrUrl = "https://diva-s3.s3.ap-northeast-2.amazonaws.com/song/" + songOld.getArtist() + "-" + songOld.getTitle() + "/" + songOld.getArtist() + "-" + songOld.getTitle() + "_MR.mp3";

            // songOld의 highestNote가 null이 아니면
            if (songOld.getHighestNote() != null) {
                // songOld의 highestNote의 형태를 변환
                String oldHighestNote = songOld.getHighestNote();
                String newHighestNote = "";

                // highestNote가 _로 구분되어 있으면
                if (oldHighestNote.contains("_")) {
                    newHighestNote += oldHighestNote.substring(0, 1) + "#" + oldHighestNote.substring(1, 2);
                }
                else {
                    newHighestNote += oldHighestNote.substring(0, 2);
                }

                // 변환된 highestNote를 midi로 변환
                Integer newMidi = recommendArtist.noteToMidi(newHighestNote, true);

                // 변환된 highestNote와 midi를 가지고 SongRange 객체 생성
                SongRange songRange = SongRange.builder()
                    .highestNote(newHighestNote)
                    .highestMidi(newMidi)
                    .build();

                songRange = songRangeRepository.save(songRange);

                // SongRange 객체와 Song 객체 생성
                song = Song.builder()
                    .title(songOld.getTitle())
                    .artist(songOld.getArtist())
                    .mrUrl(newMrUrl)
                    .songRange(songRange)
                    .build();
            }
            // songOld의 highestNote가 null이면
            else {
            song = Song.builder()
                .title(songOld.getTitle())
                .artist(songOld.getArtist())
                .mrUrl(newMrUrl)
                .build();
            }

            System.out.println(newMrUrl.length()git );

            return song;
        };
    }

    @Bean
    public JpaItemWriter<Song> moveSongWriter() {
        return new JpaItemWriterBuilder<Song>()
            .entityManagerFactory(emf)
            .build();
    }
}
