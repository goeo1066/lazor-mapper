package com.github.goeo1066.lazormapper;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import static com.github.goeo1066.lazormapper.LazorMapperBeanRegisterer.*;

@SpringBootApplication
public class LazorMapperApplication {

    public static void main(String[] args) {
        SpringApplication.run(LazorMapperApplication.class, args);
    }

    @Bean
    public ApplicationRunner applicationRunner(NamedParameterJdbcTemplate jdbcTemplate) {
        return args -> {
            var result = select(PersonInfo.class, jdbcTemplate, "AGE > 30 AND AGE < 40");
            var number = count(PersonInfo.class, jdbcTemplate, "AGE > 30 AND AGE < 40");
            for (PersonInfo personInfo : result) {
                System.out.println(personInfo);
            }
            System.out.println("Number of records: " + number);
        };
    }
}
