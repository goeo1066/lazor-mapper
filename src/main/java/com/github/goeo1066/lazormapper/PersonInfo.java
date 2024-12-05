package com.github.goeo1066.lazormapper;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table(name = "PERSON_INFO", schema = "PUBLIC")
public class PersonInfo {
    @Id
    private int idx;
    private String name;
    private int age;
    private String address;
    @Transient
    private boolean isOld;

    public PersonInfo(
            int idx,
            String name,
            int age,
            String address
    ) {
        this.idx = idx;
        this.name = name;
        this.age = age;
        this.address = address;
    }
}
