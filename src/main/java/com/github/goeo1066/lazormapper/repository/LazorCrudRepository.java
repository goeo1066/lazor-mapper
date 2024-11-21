package com.github.goeo1066.lazormapper.repository;

import java.util.List;

public interface LazorCrudRepository<S> {
    List<S> select(LazorSelectSpec selectSpec);
    long count(LazorSelectSpec selectSpec);
}
