package com.theroot.featureflags;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FeatureFlagsApplication {

    private static final Logger log = LoggerFactory.getLogger(FeatureFlagsApplication.class);

    public static void main(String[] args) {
        log.info("Starting Feature Flags API...");
        SpringApplication.run(FeatureFlagsApplication.class, args);
        log.info("Feature Flags API started successfully");
    }
}
