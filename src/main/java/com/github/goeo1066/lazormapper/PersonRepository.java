package com.github.goeo1066.lazormapper;

import com.github.goeo1066.lazormapper.repository.LazorCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonRepository extends LazorCrudRepository<PersonInfo> {
}
