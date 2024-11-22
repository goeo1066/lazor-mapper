package com.github.goeo1066.lazormapper.composers;

public interface LazorSelectSqlComposer<S> {
    String composeSelectSql(LazorTableInfo<S> tableInfo, LazorSelectSpec selectSpec);

    String composeCountSql(LazorTableInfo<S> tableInfo, LazorSelectSpec selectSpec);

    String composeSelectTestSql(LazorTableInfo<S> tableInfo);

    static <S> LazorSelectSqlComposer<S> createInstanceOf(String dbType) {
        if (dbType.equals("postgresql")) {
            return new LazorSelectSqlComposerPostgreSQL<>();
        }
        throw new RuntimeException("Unsupported database type: " + dbType);
    }
}
