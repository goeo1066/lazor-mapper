package com.github.goeo1066.lazormapper.composers.upsert;

import com.github.goeo1066.lazormapper.composers.LazorTableInfo;

public interface LazorUpsertSqlComposer<S> {
    String composeUpsertSql(LazorTableInfo<S> tableInfo, LazorUpsertSpec<S> upsertSpec);

    static <S> LazorUpsertSqlComposer<S> createInstanceOf(String dbType) {
        if (dbType.equals("postgresql")) {
            return new LazorUpsertSqlComposerPostgreSQL<>();
        } else if (dbType.equals("oracle")) {
            return new LazorUpsertSqlComposerOracle<>();
        }
        throw new RuntimeException("Unsupported database type: " + dbType);
    }
}
