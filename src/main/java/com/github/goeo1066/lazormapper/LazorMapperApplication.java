package com.github.goeo1066.lazormapper;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.Map;

import static com.github.goeo1066.lazormapper.LazorMapperBeanRegisterer.getRowMapperForRecord;

@SpringBootApplication
public class LazorMapperApplication {

    public static void main(String[] args) {
        SpringApplication.run(LazorMapperApplication.class, args);
    }

    @Bean
    public ApplicationRunner applicationRunner(NamedParameterJdbcTemplate jdbcTemplate) {
        return args -> {
            RowMapper<PersonInfo> personInfoRowMapper = getRowMapperForRecord(PersonInfo.class);
            try (var stream = jdbcTemplate.queryForStream("SELECT * FROM person_info", Map.of(), personInfoRowMapper)) {
                stream.forEach(System.out::println);
            }
        };
    }
}
