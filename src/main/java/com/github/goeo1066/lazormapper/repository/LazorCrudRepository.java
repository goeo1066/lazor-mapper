package com.github.goeo1066.lazormapper.repository;

import com.github.goeo1066.lazormapper.composers.select.LazorSelectSpec;

import java.util.Collection;
import java.util.List;

public interface LazorCrudRepository<S> {
    List<S> select(LazorSelectSpec selectSpec);
    long count(LazorSelectSpec selectSpec);
    List<S> insert(Collection<S> entity);
    void update(Collection<S> entity);
}
