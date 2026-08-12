package org.ict.datemanagerbackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

// application.yaml의 cloud.aws.* 설정(.env의 S3_ACCESS_KEY/SECRET_KEY)으로 S3Client 빈을 만든다.
// 이 빈이 실제로 쓰이는 곳은 ProfileImageService.
@Configuration
public class S3Config {

    // application.yaml에서 cloud.aws.*가 spring: 블록 아래 중첩돼 있어서 실제 경로는 spring.cloud.aws.*다.
    @Value("${spring.cloud.aws.credentials.access-key}")
    private String accessKey;

    @Value("${spring.cloud.aws.credentials.secret-key}")
    private String secretKey;

    @Value("${spring.cloud.aws.region}")
    private String region;

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .build();
    }
}
