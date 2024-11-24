package com.github.goeo1066.lazormapper.composers.select;

import com.github.goeo1066.lazormapper.composers.LazorTableInfo;

public class LazorSelectSqlComposerPostgreSQL<S> implements LazorSelectSqlComposer<S> {

    @Override
    public String composeSelectSql(LazorTableInfo<S> tableInfo, LazorSelectSpec selectSpec) {
        return createSelectSql(tableInfo, "T", selectSpec);
    }

    @Override
    public String composeCountSql(LazorTableInfo<S> tableInfo, LazorSelectSpec selectSpec) {
        return createCountSql(tableInfo, "T", selectSpec);
    }

    @Override
    public String composeSelectTestSql(LazorTableInfo<S> tableInfo) {
        LazorSelectSpec selectSpec = LazorSelectSpec.builder()
                .whereClause("1 = 0")
                .build();
        return createSelectSql(tableInfo, "T", selectSpec);
    }

    private String createSelectSql(LazorTableInfo<S> tableInfo, String mainTableAlias, LazorSelectSpec selectSpec) {
        return createSelectSqlTemplate(tableInfo, mainTableAlias, "*", selectSpec);
    }

    private String createCountSql(LazorTableInfo<S> tableInfo, String mainTableAlias, LazorSelectSpec selectSpec) {
        return createSelectSqlTemplate(tableInfo, mainTableAlias, "COUNT(*)", selectSpec);
    }

    private String createSelectSqlTemplate(LazorTableInfo<S> tableInfo, String mainTableAlias, String columnReplacer, LazorSelectSpec selectSpec) {
        String createSelectSqlTemplate = createSelectSubSqlTemplate(tableInfo, "S", selectSpec);
        String selectSql = "SELECT " + columnReplacer + " FROM (" + createSelectSqlTemplate + ") " + mainTableAlias;

        if (selectSpec != null) {
            if (selectSpec.limit() != null && selectSpec.limit() > 0) {
                selectSql += " LIMIT " + selectSpec.limit();
            }
            if (selectSpec.offset() != null && selectSpec.offset() > 0) {
                selectSql += " OFFSET " + selectSpec.offset();
            }
        }
        return selectSql;
    }

    private String createSelectSubSqlTemplate(LazorTableInfo<S> tableInfo, String tableAlias, LazorSelectSpec selectSpec) {
        String tableName = tableInfo.tableFullName();

        String selectSql = "SELECT " + tableAlias + ".* FROM " + tableName + " " + tableAlias;
        if (selectSpec != null) {
            if (selectSpec.whereClause() != null && !selectSpec.whereClause().isBlank()) {
                selectSql += " WHERE " + selectSpec.whereClause();
            }
            if (selectSpec.orderByClause() != null && !selectSpec.orderByClause().isBlank()) {
                selectSql += " ORDER BY " + selectSpec.orderByClause();
            }
        }
        return selectSql;
    }
}
