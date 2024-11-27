package com.github.goeo1066.lazormapper.composers.delete;

import com.github.goeo1066.lazormapper.composers.LazorTableInfo;

public class LazorDeleteSqlComposerPostgreSQL<S> implements LazorDeleteSqlComposer<S> {

    @Override
    public String composeDeleteSql(LazorTableInfo<S> tableInfo, LazorDeleteSpec deleteSpec) {
        return createDeleteSqlTemplate(tableInfo, "T", deleteSpec.whereClause());
    }

    private String createDeleteSqlTemplate(LazorTableInfo<S> tableInfo, String mainTableAlias, String whereClauses) {
        String sql = "DELETE FROM " + tableInfo.tableFullName() + " " + mainTableAlias + " WHERE 1 = 1";
        return sql + " AND (" + whereClauses + ")";
    }
}
