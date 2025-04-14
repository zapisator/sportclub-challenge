package com.sportclub.challenge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class SportclubChallengeApplication {

    public static void main(String[] args) {
        SpringApplication.run(SportclubChallengeApplication.class, args);
    }

}