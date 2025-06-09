package com.liboshuai.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class MysqlJpaDemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(MysqlJpaDemoApplication.class, args);
    }
}
