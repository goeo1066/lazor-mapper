package com.github.goeo1066.lazormapper.composers;

import com.github.goeo1066.lazormapper.repository.LazorSelectSpec;

public interface LazorSelectSqlComposer {
    String composeSelectSql(LazorTableInfo tableInfo, LazorSelectSpec selectSpec);

    String composeCountSql(LazorTableInfo tableInfo, LazorSelectSpec selectSpec);

    static LazorSelectSqlComposer createInstanceOf(String dbType) {
        if (dbType.equals("postgresql")) {
            return new LazorSelectSqlComposerPostgreSQL();
        }
        throw new RuntimeException("Unsupported database type: " + dbType);
    }
}
