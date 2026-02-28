package com.fabricahair;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FabricHairApplication {
    public static void main(String[] args) {
        SpringApplication.run(FabricHairApplication.class, args);
    }
}
