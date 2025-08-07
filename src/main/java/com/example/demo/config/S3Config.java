package com.example.demo.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration // 이 클래스가 설정 파일임을 스프링에게 알려줍니다.
public class S3Config {

    // application.yml (또는 .properties) 에서 설정한 값들을 가져옵니다.
    @Value("${cloud.aws.credentials.access-key}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secret-key}")
    private String secretKey;

    @Value("${cloud.aws.region.static}")
    private String region;

    // AmazonS3Client 객체를 생성하여 Bean으로 등록합니다.
    @Bean
    public AmazonS3Client amazonS3Client() {
        // 1. AWS 자격 증명을 생성합니다.
        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);

        // 2. 자격 증명과 리전으로 S3 클라이언트를 빌드합니다.
        return (AmazonS3Client) AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .withRegion(region)
                .build();
    }
}
