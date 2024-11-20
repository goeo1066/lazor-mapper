package com.github.goeo1066.lazormapper.composers;

public interface LazorSelectSqlComposer {
    String composeSelectSql(LazorTableInfo tableInfo, String whereClause);

    String composeCountSql(LazorTableInfo tableInfo, String whereClause);

    static LazorSelectSqlComposer createInstanceOf(String dbType) {
        if (dbType.equals("postgresql")) {
            return new LazorSelectSqlComposerPostgreSQL();
        }
        throw new RuntimeException("Unsupported database type: " + dbType);
    }
}
