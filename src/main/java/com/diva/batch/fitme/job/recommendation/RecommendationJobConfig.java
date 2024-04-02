package com.diva.batch.fitme.job.recommendation;

import com.diva.batch.fitme.entity.Product;
import com.diva.batch.fitme.entity.ProductRecommendation;
import com.diva.batch.repository.ProductRecommendationRepository;
import com.diva.batch.repository.ProductRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.io.*;
import java.util.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RecommendationJobConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManager entityManager;

    private final ProductRepository productRepository;
    private final ProductRecommendationRepository productRecommendationRepository;

    @Bean
    public Job RecommendationJob() {
        return new JobBuilder("recommendationJob", jobRepository)
                .start(recommendationStep1(null))
                .build();
    }

    @Bean
    @JobScope
    public Step recommendationStep1(@Value("#{jobParameters[date]}") String date) {
        return new StepBuilder("recommendationStep1", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    log.info(">>>>> Recommendation Step1 Start");

                    // csv 파일을 읽는다.
                    try {
                        BufferedReader br = new BufferedReader(new FileReader("src/main/resources/result.csv"));
                        String line;
                        while ((line = br.readLine()) != null) {
                            // 콤마(,)로 구분
                            String[] array = line.split(",");

                            // array[0] : productId
                            Long productId = Long.parseLong(array[0]);
                            System.out.println("productId = " + productId);
                            Product product = productRepository.findById(productId).orElseThrow(
                                () -> new IllegalArgumentException(
                                    "해당 상품이 존재하지 않습니다. productId=" + productId)
                            );

                            // array[1] ~ array[10] : productRecommendId
                            List<Long> productRecommendIds = new ArrayList<>();
                            for (int i = 1; i < array.length; i++) {
                                productRecommendIds.add(Long.parseLong(array[i]));
                            }

                            productRepository.findAllById(productRecommendIds).forEach(productToRecommend -> {
                                ProductRecommendation productRecommendation = ProductRecommendation.builder()
                                    .product(product)
                                    .recommendation(productToRecommend)
                                    .build();

                                product.addRecommendation(productRecommendation);

                                productRecommendationRepository.save(productRecommendation);
                            });

                            productRepository.save(product);

                            entityManager.flush();
                        }
                        br.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    return RepeatStatus.FINISHED;
                }, transactionManager).build();
    }
}
