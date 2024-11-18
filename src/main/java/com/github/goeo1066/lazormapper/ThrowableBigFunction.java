package com.github.goeo1066.lazormapper;

public interface ThrowableBigFunction <T, U, R, E extends Throwable> {
    R apply(T t, U u) throws E;
}
