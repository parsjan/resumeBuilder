package com.app.ai.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.openai")
public class OpenAiProperties {

    private String apiKey;
    private String baseUrl;
    private String model;
    private int maxTokens;
    private int timeoutSeconds;

    /** Maximum retry attempts on transient 5xx / network failures. */
    private int maxRetries = 3;
}
