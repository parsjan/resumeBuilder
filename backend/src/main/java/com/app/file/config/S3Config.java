package com.app.file.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
@RequiredArgsConstructor
public class S3Config {

    private final S3Properties props;

    /**
     * S3Client for PUT / DELETE operations (server-to-S3).
     * Credentials are resolved from {@code app.aws.*} properties;
     * in production prefer IAM roles (remove the static provider and use
     * {@code DefaultCredentialsProvider.create()} instead).
     */
    @Bean
    S3Client s3Client() {
        return S3Client.builder()
                .region(Region.of(props.getRegion()))
                .credentialsProvider(staticCredentials())
                .build();
    }

    /**
     * S3Presigner for generating time-limited GET URLs that can be handed to clients
     * without exposing the bucket or AWS credentials.
     */
    @Bean
    S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .region(Region.of(props.getRegion()))
                .credentialsProvider(staticCredentials())
                .build();
    }

    private StaticCredentialsProvider staticCredentials() {
        return StaticCredentialsProvider.create(
                AwsBasicCredentials.create(props.getAccessKeyId(), props.getSecretAccessKey()));
    }
}
