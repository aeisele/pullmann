package com.andreaseisele.pullmann;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class PullmannApplication {

    public static void main(String[] args) {
        SpringApplication.run(PullmannApplication.class, args);
    }

}
