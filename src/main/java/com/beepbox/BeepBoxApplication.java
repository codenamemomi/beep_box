package com.beepbox;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BeepBoxApplication {

    public static void main(String[] args) {
        SpringApplication.run(BeepBoxApplication.class, args);
    }
}
