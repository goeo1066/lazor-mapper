package com.github.goeo1066.lazormapper;

public interface ThrowableFunction<T, U, R, E extends Throwable> {
    R apply(T t, U u) throws E;
}
