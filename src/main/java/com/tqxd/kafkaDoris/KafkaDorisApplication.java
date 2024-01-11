package com.tqxd.kafkaDoris;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan(basePackages = {"com.tqxd.kafkaDoris.mapper"})
@EnableScheduling
public class KafkaDorisApplication {

    public static void main(String[] args) {
        SpringApplication.run(KafkaDorisApplication.class, args);
    }
}
