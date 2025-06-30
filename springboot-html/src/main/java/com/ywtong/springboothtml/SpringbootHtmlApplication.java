package com.ywtong.springboothtml;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("com.ywtong.springboothtml.entity")
@EnableJpaRepositories(basePackages = {"com.ywtong.springboothtml.Dao", "com.ywtong.springboothtml.repository"})
public class SpringbootHtmlApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringbootHtmlApplication.class, args);
    }

}
