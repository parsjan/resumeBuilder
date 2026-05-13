package com.app.file.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.aws")
public class S3Properties {

    private String accessKeyId;
    private String secretAccessKey;
    private String region;

    private S3 s3 = new S3();

    @Getter
    @Setter
    public static class S3 {
        private String bucketName;
        private long signedUrlExpirationMinutes = 60;
    }
}
