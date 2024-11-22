package com.github.goeo1066.lazormapper.repository;

import com.github.goeo1066.lazormapper.composers.LazorSelectSpec;

import java.util.List;
import java.util.Optional;

public interface LazorCrudRepository<S> {
    List<S> select(LazorSelectSpec selectSpec);
    long count(LazorSelectSpec selectSpec);
    Optional<S> insert(List<S> entity);
}
