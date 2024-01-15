package com.diva.batch.job.youtube;

import com.diva.batch.repository.YoutubeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class YoutubeJob {
    private final YoutubeRepository youtubeRepository;
}
