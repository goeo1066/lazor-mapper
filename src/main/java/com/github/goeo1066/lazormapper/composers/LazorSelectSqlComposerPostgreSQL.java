package com.github.goeo1066.lazormapper.composers;

import com.github.goeo1066.lazormapper.repository.LazorSelectSpec;

public class LazorSelectSqlComposerPostgreSQL implements LazorSelectSqlComposer {

    @Override
    public String composeSelectSql(LazorTableInfo tableInfo, LazorSelectSpec selectSpec) {
        return createSelectSql(tableInfo, "T", selectSpec);
    }

    @Override
    public String composeCountSql(LazorTableInfo tableInfo, LazorSelectSpec selectSpec) {
        return createCountSql(tableInfo, "T", selectSpec);
    }

    public String createSelectSql(LazorTableInfo tableInfo, String mainTableAlias, LazorSelectSpec selectSpec) {
        return createSelectSqlTemplate(tableInfo, mainTableAlias, "*", selectSpec);
    }

    public String createCountSql(LazorTableInfo tableInfo, String mainTableAlias, LazorSelectSpec selectSpec) {
        return createSelectSqlTemplate(tableInfo, mainTableAlias, "COUNT(*)", selectSpec);
    }

    public String createSelectSqlTemplate(LazorTableInfo tableInfo, String mainTableAlias, String columnReplacer, LazorSelectSpec selectSpec) {
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

    public String createSelectSubSqlTemplate(LazorTableInfo tableInfo, String tableAlias, LazorSelectSpec selectSpec) {
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
