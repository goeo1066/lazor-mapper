package com.github.goeo1066.lazormapper.composers.update;

import com.github.goeo1066.lazormapper.composers.LazorTableInfo;

public interface LazorUpdateSqlComposer<S> {
    String composeUpdateSql(LazorTableInfo<S> tableInfo, LazorUpdateSpec<S> updateSpec);

    static <S> LazorUpdateSqlComposer<S> createInstanceOf(String dbType) {
        if (dbType.equals("postgresql")) {
            return new LazorUpdateSqlComposerPostgreSQL<>();
        }
        throw new RuntimeException("Unsupported database type: " + dbType);
    }
}
