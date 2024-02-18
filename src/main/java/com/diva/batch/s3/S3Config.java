package com.diva.batch.s3;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class S3Config {
    @Value("${AWS.ACCESS}")
    private String accessKey;
    @Value("${AWS.SECRET}")
    private String secretKey;
    @Value("${AWS_REGION_STATIC}")
    private String region;
    @Bean
    public AmazonS3 amazonS3Client(){
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
        return AmazonS3ClientBuilder
            .standard()
            .withRegion(region).enablePathStyleAccess()
            .withCredentials(new AWSStaticCredentialsProvider(awsCredentials))
            .build();
    }
}
