package com.diva.batch.job.tj;

import com.diva.batch.entity.Category;
import com.diva.batch.entity.Song;
import com.diva.batch.repository.CategoryRepository;
import com.diva.batch.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class TJJobConfig {
    private final CategoryRepository categoryRepository;
    private final SongRepository songRepository;

    @Bean
    public Job tjJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new JobBuilder("tjJob", jobRepository)
                .start(tjStep1(null, jobRepository, transactionManager))
                .next(tjStep2(null, jobRepository, transactionManager))
                .build();
    }

    @Bean
    @JobScope
    public Step tjStep1(@Value("#{jobParameters[date]}") String date, JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("tjStep1", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    log.info(">>>>> TJ Step1 Start");

                    Category 가요 = Category.builder()
                            .name("가요")
                            .crawlingUrl("https://www.tjmedia.com/tjsong/song_monthPopular.asp?strType=1&SYY=2024&SMM=01&EYY=2024&EMM=01")
                        .build();

                    Category pop = Category.builder()
                            .name("POP")
                            .crawlingUrl("https://www.tjmedia.com/tjsong/song_monthPopular.asp?strType=2&SYY=2024&SMM=01&EYY=2024&EMM=01")
                        .build();

                    categoryRepository.save(가요);
                    categoryRepository.save(pop);

                    log.info(">>>>> TJ Step1 End");

                    return RepeatStatus.FINISHED;
                }, transactionManager)
            .build();
    }

    @Bean
    @JobScope
    public Step tjStep2(@Value("#{jobParameters[date]}") String date, JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("tjStep2", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    log.info(">>>>> TJ Step2 Start");

                    // Category를 가져온다.
                    List<Category> categories = categoryRepository.findAll();

                    for (Category category : categories) {
                        // HTML을 받아온다.
                        Connection connection = Jsoup.connect(category.getCrawlingUrl());
                        connection.timeout(100_000); // 100초
                        final Response response = connection.execute();

                        // HTML을 파싱한다.
                        final Document doc = response.parse();

                        // 파싱한 데이터를 DTO에 저장한다.
                        List<Song> songs = new ArrayList<>();

                        // div id="BoardType1"인 태그를 찾는다.
                        Element boardType1 = doc.getElementById("BoardType1");

                        // 그 안에서 tbody를 찾고
                        Element tbody = boardType1.getElementsByTag("tbody").get(0);

                        // tbody 안에서 tr을 찾는다.
                        Elements trs = tbody.getElementsByTag("tr");

                        // 첫번째를 제외하고 tr을 순회한다.
                        for (int i = 1; i <= 100; i++) {
                            // 두번째 td는 tjId
                            Element tjId = trs.get(i).getElementsByTag("td").get(1);

                            // 세번째 td는 title
                            Element title = trs.get(i).getElementsByTag("td").get(2);

                            // 네번째 td는 artist
                            Element artist = trs.get(i).getElementsByTag("td").get(3);

                            // Song 객체 생성
                            songs.add(Song.builder()
                                    .tjId(Long.valueOf(tjId.text()))
                                    .title(title.text())
                                    .artist(artist.text())
                                    .category(category)
                                    .build());
                        }

                        // 파싱한 데이터를 DB에 저장한다.
                        songRepository.saveAll(songs);
                    }

                    log.info(">>>>> TJ Step2 End");

                    return RepeatStatus.FINISHED;
                }, transactionManager)
            .build();
    }
}
