package com.github.goeo1066.lazormapper.composers.insert;

import com.github.goeo1066.lazormapper.composers.LazorColumnInfo;
import com.github.goeo1066.lazormapper.composers.LazorTableInfo;
import org.slf4j.Logger;

import java.util.stream.Collectors;

public class LazorInsertSqlComposerPostgreSQL<S> implements LazorInsertSqlComposer<S> {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(LazorInsertSqlComposerPostgreSQL.class);

    public String composeInsertSql(LazorTableInfo<S> tableInfo, LazorInsertSpec<S> insertSpec) {
        return createInsertSqlTemplate(tableInfo, insertSpec);
    }

    private String createInsertSqlTemplate(LazorTableInfo<S> tableInfo, LazorInsertSpec<S> insertSpec) {
        String tableName = tableInfo.tableFullName();
        String columnList = columnNameListForInsert(tableInfo);
        String replacerList = replacerListForInsert(tableInfo);
        String insertSql = "INSERT INTO " + tableName + " (" + columnList + ") VALUES (" + replacerList + ")";
        logger.debug("Insert SQL: {}", insertSql);
        return insertSql;
    }

    private String columnNameListForInsert(LazorTableInfo<S> tableInfo) {
        return tableInfo.columnInfoList().stream()
                .filter(columnInfo -> !columnInfo.isTransient())
                .map(LazorColumnInfo::columnName)
                .collect(Collectors.joining(", "));
    }

    private String replacerListForInsert(LazorTableInfo<S> tableInfo) {
        return tableInfo.columnInfoList().stream()
                .filter(columnInfo -> !columnInfo.isTransient())
                .map(columnInfo -> ":" + columnInfo.fieldName())
                .collect(Collectors.joining(", "));
    }
}
