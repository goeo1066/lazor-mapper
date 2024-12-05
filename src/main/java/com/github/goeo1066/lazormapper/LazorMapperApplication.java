package com.github.goeo1066.lazormapper;

import com.github.goeo1066.lazormapper.composers.LazorSqlComposerUtils;
import com.github.goeo1066.lazormapper.composers.delete.LazorDeleteSpec;
import com.github.goeo1066.lazormapper.composers.select.LazorSelectSpec;
import com.github.goeo1066.lazormapper.repository.LazorRepositoryRegisterer;
import net.bytebuddy.agent.ByteBuddyAgent;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StopWatch;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.function.Consumer;

@SpringBootApplication
public class LazorMapperApplication {

    public static void main(String[] args) {

        ByteBuddyAgent.install();

        SpringApplication.run(LazorMapperApplication.class, args);
    }

    @Bean
    public ApplicationRunner applicationRunner(PersonRepository personRepository) {
        return args -> {
            Object num1 = Integer.valueOf(0);
            System.out.println(num1.hashCode());
            Object num2 = Integer.valueOf(0);
            System.out.println(num2.hashCode());

            PersonInfo personInfo = new PersonInfo(
                    0, "John Test", 30, "addresses"
            );
            System.out.println(personInfo.getName());
            personInfo.isOld();
            LazorSqlComposerUtils.retrieveColumnInfo(PersonInfo.class);
//            Object proxied = LazorRepositoryRegisterer.proxiedRecord(PersonInfo.class);

//            StopWatch stopWatch = new StopWatch();
//            stopWatch.start();
//            var selectSpec = LazorSelectSpec.builder()
//                    .whereClause("age > 30 AND age < 50")
//                    .orderByClause("age DESC")
//                    .limit(10)
//                    .offset(0)
//                    .build();
//            var result = personRepository.select(selectSpec);
//            var number = personRepository.count(selectSpec.withoutPaging());
//
//            PersonInfo personInfo3 = new PersonInfo(
//                    0, "John Test a", 40, "addresses"
//            );
//            personRepository.upsert(List.of(personInfo3), null);
//            personRepository.delete(new LazorDeleteSpec("idx = 0"));
////            for (PersonInfo personInfo : personRepository.select(LazorSelectSpec.builder().whereClause("idx = 0 or idx = 1").build())) {
////                System.out.println(personInfo);
////            }
        };
    }

    public <T, S extends T> void proxyRecord(Class<T> entityClass, Consumer<S> consumer) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        S proxied = (S) LazorRepositoryRegisterer.proxiedRecord(entityClass);
        System.out.println("proxied: " + proxied);
        consumer.accept(proxied);
    }
}
