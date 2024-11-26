package com.github.goeo1066.lazormapper;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "PERSON_INFO", schema = "PUBLIC")
public record PersonInfo(
        @Id
        int idx,
        String name,
        int age,
        String address
) {
}
