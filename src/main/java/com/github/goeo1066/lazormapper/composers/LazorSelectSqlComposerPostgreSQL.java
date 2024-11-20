package com.github.goeo1066.lazormapper.composers;

public class LazorSelectSqlComposerPostgreSQL implements LazorSelectSqlComposer {

    @Override
    public String composeSelectSql(LazorTableInfo tableInfo, String whereClause) {
        return createSelectSql(tableInfo, "T", whereClause);
    }

    @Override
    public String composeCountSql(LazorTableInfo tableInfo, String whereClause) {
        return createCountSql(tableInfo, "T", whereClause);
    }

    public String createCountSql(LazorTableInfo tableInfo, String mainTableAlias, String whereClauseTemplate) {
        return createSelectSqlTemplate(tableInfo, mainTableAlias, "COUNT(*)", whereClauseTemplate);
    }

    public String createSelectSql(LazorTableInfo tableInfo, String mainTableAlias, String whereClauseTemplate) {
        return createSelectSqlTemplate(tableInfo, mainTableAlias, "*", whereClauseTemplate);
    }

    public String createSelectSqlTemplate(LazorTableInfo tableInfo, String mainTableAlias, String columnReplacer, String whereClauseTemplate) {
        String createSelectSqlTemplate = createSelectSubSqlTemplate(tableInfo, whereClauseTemplate);
        return "SELECT " + columnReplacer + " FROM (" + createSelectSqlTemplate + ") " + mainTableAlias;
    }

    public String createSelectSubSqlTemplate(LazorTableInfo tableInfo, String whereClauseTemplate) {
        String tableName = tableInfo.tableFullName();
        String selectSql = "SELECT * FROM " + tableName + " ";
        if (whereClauseTemplate != null && !whereClauseTemplate.isBlank()) {
            selectSql += "WHERE " + whereClauseTemplate;
        }
        return selectSql;
    }
}
