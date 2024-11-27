package com.github.goeo1066.lazormapper;

import com.github.goeo1066.lazormapper.composers.delete.LazorDeleteSpec;
import com.github.goeo1066.lazormapper.composers.select.LazorSelectSpec;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StopWatch;

import java.util.List;

@SpringBootApplication
public class LazorMapperApplication {

    public static void main(String[] args) {
        SpringApplication.run(LazorMapperApplication.class, args);
    }

    @Bean
    public ApplicationRunner applicationRunner(PersonRepository personRepository) {
        return args -> {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            var selectSpec = LazorSelectSpec.builder()
                    .whereClause("age > 30 AND age < 50")
                    .orderByClause("age DESC")
                    .limit(10)
                    .offset(0)
                    .build();
            var result = personRepository.select(selectSpec);
            var number = personRepository.count(selectSpec.withoutPaging());

            PersonInfo personInfo3 = new PersonInfo(
                    0, "John Test a", 40, "addresses"
            );
            personRepository.upsert(List.of(personInfo3), null);
            personRepository.delete(new LazorDeleteSpec("idx = 0"));
            for (PersonInfo personInfo : personRepository.select(LazorSelectSpec.builder().whereClause("idx = 0 or idx = 1").build())) {
                System.out.println(personInfo);
            }
        };
    }
}
