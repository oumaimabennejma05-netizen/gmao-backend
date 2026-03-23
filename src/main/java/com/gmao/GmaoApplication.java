package com.gmao;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GmaoApplication {
    public static void main(String[] args) {
        SpringApplication.run(GmaoApplication.class, args);
    }
}
