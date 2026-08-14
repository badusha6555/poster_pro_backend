package com.posterpro.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class PosterProApplication {
    public static void main(String[] args) {
        SpringApplication.run(PosterProApplication.class, args);
    }
}
