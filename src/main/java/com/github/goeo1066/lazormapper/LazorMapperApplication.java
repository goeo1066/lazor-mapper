package com.github.goeo1066.lazormapper;

import com.github.goeo1066.lazormapper.composers.select.LazorSelectSpec;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StopWatch;

import java.util.Collections;
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

            for (PersonInfo personInfo : result) {
                System.out.println(personInfo);
            }
            stopWatch.stop();
            System.out.printf("Number of records: %s (took %s)%n", number, stopWatch.shortSummary());

            PersonInfo personInfo = new PersonInfo(0, "John Test", 40, "address");
            List<PersonInfo> newResult = personRepository.insert(List.of(personInfo));
            System.out.println(newResult);

            PersonInfo personInfo2 = new PersonInfo(
                    newResult.get(0).idx(), "John Test", 40, "addresses");
            personRepository.update(Collections.singleton(personInfo2));
            var newPersonInfo = personRepository.select(LazorSelectSpec.builder()
                    .whereClause("IDX = " + personInfo2.idx())
                    .build());
            System.out.println(newPersonInfo);
        };
    }
}
