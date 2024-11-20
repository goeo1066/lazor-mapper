package com.github.goeo1066.lazormapper;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StopWatch;

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
            var result = personRepository.select("AGE > 30 AND AGE < 40");
            var number = personRepository.count("AGE > 30 AND AGE < 40");

            for (PersonInfo personInfo : result) {
                System.out.println(personInfo);
            }
            stopWatch.stop();
            System.out.printf("Number of records: %s (took %s)%n", number, stopWatch.shortSummary());
        };
    }
}
