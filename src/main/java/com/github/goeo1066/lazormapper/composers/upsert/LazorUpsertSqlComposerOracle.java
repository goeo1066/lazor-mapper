package com.github.goeo1066.lazormapper.composers.upsert;

import com.github.goeo1066.lazormapper.composers.LazorColumnInfo;
import com.github.goeo1066.lazormapper.composers.LazorTableInfo;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class LazorUpsertSqlComposerOracle<S> implements LazorUpsertSqlComposer<S> {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(LazorUpsertSqlComposerOracle.class);

    @Override
    public String composeUpsertSql(LazorTableInfo<S> tableInfo, LazorUpsertSpec<S> upsertSpec) {
        return createUpsert(tableInfo, upsertSpec, "O", "T");
    }

    public String createUpsert(LazorTableInfo<S> tableInfo, LazorUpsertSpec<S> upsertSpec, String mainTableAlias, String tempTableAlias) {
        StringBuilder builder = new StringBuilder();
        builder.append(createHeader(tableInfo, mainTableAlias, tempTableAlias)).append(' ');
        builder.append(createInsert(tableInfo, mainTableAlias, tempTableAlias)).append(' ');
        if (upsertSpec.doUpdate()) {
            builder.append(createUpdate(tableInfo, mainTableAlias, tempTableAlias)).append(' ');
        }
        return builder.toString();
    }

    public String createInsert(LazorTableInfo<S> tableInfo, String mainTableAlias, String tempTableAlias) {
        String columnRows = createInsertColumn(tableInfo, mainTableAlias);
        String replacerRows = createInsertReplacer(tableInfo, tempTableAlias);
        return "WHEN NOT MATCHED THEN INSERT (" + columnRows + ") VALUES (" + replacerRows + ")";
    }

    public String createInsertColumn(LazorTableInfo<S> tableInfo, String mainTableAlias) {
        List<String> columns = new ArrayList<>(tableInfo.columnInfoList().size());
        for (LazorColumnInfo lazorColumnInfo : tableInfo.columnInfoList()) {
            if (lazorColumnInfo.isTransient()) {
                continue;
            }
            String columnRow = mainTableAlias + "." + lazorColumnInfo.columnName();
            columns.add(columnRow);
        }
        return String.join(", ", columns);
    }

    public String createInsertReplacer(LazorTableInfo<S> tableInfo, String tempTableAlias) {
        List<String> columns = new ArrayList<>(tableInfo.columnInfoList().size());
        for (LazorColumnInfo lazorColumnInfo : tableInfo.columnInfoList()) {
            if (lazorColumnInfo.isTransient()) {
                continue;
            }
            String columnRow = tempTableAlias + "." + lazorColumnInfo.columnName();
            columns.add(columnRow);
        }
        return String.join(", ", columns);
    }

    public String createUpdate(LazorTableInfo<S> tableInfo, String mainTableAlias, String tempTableAlias) {
        String columnRows = createUpdateColumn(tableInfo, mainTableAlias, tempTableAlias);
        return "WHEN MATCHED THEN UPDATE SET " + columnRows;
    }

    public String createUpdateColumn(LazorTableInfo<S> tableInfo, String mainTableAlias, String tempTableAlias) {
        List<String> columns = new ArrayList<>(tableInfo.columnInfoList().size());
        for (LazorColumnInfo lazorColumnInfo : tableInfo.columnInfoList()) {
            if (lazorColumnInfo.isPrimaryKey()) {
                continue;
            }
            if (lazorColumnInfo.isTransient()) {
                continue;
            }
            String columnRow = mainTableAlias + "." + lazorColumnInfo.columnName() + " = " + tempTableAlias + "." + lazorColumnInfo.columnName();
            columns.add(columnRow);
        }
        return String.join(", ", columns);
    }

    public String createHeader(LazorTableInfo<S> tableInfo, String mainTableAlias, String tempTableAlias) {
        String columnRows = createHeaderColumns(tableInfo);
        String onClause = createHeaderOnClause(tableInfo, mainTableAlias, tempTableAlias);
        return "MERGE INTO " + tableInfo.tableFullName() + " " + mainTableAlias +
                " USING ( SELECT " + columnRows + ") " + tempTableAlias +
                " ON (" + onClause + ") ";
    }

    public String createHeaderOnClause(LazorTableInfo<S> tableInfo, String mainTableAlias, String tempTableAlias) {
        List<String> columns = new ArrayList<>(tableInfo.primaryKeyInfoList().size());
        for (LazorColumnInfo lazorColumnInfo : tableInfo.primaryKeyInfoList()) {
            if (lazorColumnInfo.isTransient()) {
                continue;
            }
            String columnRow = mainTableAlias + "." + lazorColumnInfo.columnName() + " = " + tempTableAlias + "." + lazorColumnInfo.columnName();
            columns.add(columnRow);
        }
        return String.join(" AND ", columns);
    }

    public String createHeaderColumns(LazorTableInfo<S> tableInfo) {
        List<String> columns = new ArrayList<>(tableInfo.columnInfoList().size());
        for (LazorColumnInfo lazorColumnInfo : tableInfo.columnInfoList()) {
            if (lazorColumnInfo.isTransient()) {
                continue;
            }
            String columnRow = ":" + lazorColumnInfo.fieldName() + " AS " + lazorColumnInfo.columnName();
            columns.add(columnRow);
        }
        return String.join(", ", columns);
    }
}
