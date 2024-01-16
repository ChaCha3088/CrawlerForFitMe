package com.diva.batch.config;

import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class YouTubeConfig {
    @Bean
    public HttpTransport httpTransport() {
        return new NetHttpTransport();
    }

    @Bean
    public JsonFactory jsonFactory() {
        return new JacksonFactory();
    }

    @Bean
    public HttpRequestInitializer httpRequestInitializer() {
        return request -> {
        };
    }
}
