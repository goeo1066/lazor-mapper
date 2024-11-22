package com.github.goeo1066.lazormapper.composers;

public interface LazorInsertSqlComposer<S> {
    String composeInsertSql(LazorTableInfo<S> tableInfo, LazorInsertSpec<S> insertSpec);

    static <S> LazorInsertSqlComposer<S> createInstanceOf(String dbType) {
        if (dbType.equals("postgresql")) {
            return new LazorInsertSqlComposerPostgreSQL<S>();
        }
        throw new RuntimeException("Unsupported database type: " + dbType);
    }
}
