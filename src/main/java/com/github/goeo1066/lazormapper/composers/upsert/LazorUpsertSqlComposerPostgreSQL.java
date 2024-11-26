package com.github.goeo1066.lazormapper.composers.upsert;

import com.github.goeo1066.lazormapper.composers.LazorColumnInfo;
import com.github.goeo1066.lazormapper.composers.LazorTableInfo;

import java.util.ArrayList;
import java.util.List;

public class LazorUpsertSqlComposerPostgreSQL<S> implements LazorUpsertSqlComposer<S> {
    public String composeUpsertSql(LazorTableInfo<S> tableInfo, LazorUpsertSpec<S> upsertSpec) {
        if (upsertSpec == null) {
            upsertSpec = LazorUpsertSpec.ofDefault();
        }
        String sql = createUpsert(tableInfo, upsertSpec);
        System.out.println(sql);
        return sql;
    }

    public String createUpsert(LazorTableInfo<S> tableInfo, LazorUpsertSpec<S> upsertSpec) {
        String insert = createInsert(tableInfo);
        String conflictTarget = createConflictTarget(tableInfo);
        String onConflict;
        if (upsertSpec.doUpdate()) {
            onConflict = createDoUpdate(tableInfo);
        } else {
            onConflict = createDoNothing();
        }
        return insert + " ON CONFLICT (" + conflictTarget + ") " + onConflict;
    }

    public String createInsert(LazorTableInfo<S> tableInfo) {
        String columnRows = createInsertColumn(tableInfo);
        String replacerRows = createInsertReplacer(tableInfo);
        return "INSERT INTO " + tableInfo.tableFullName() + " (" + columnRows + ") VALUES (" + replacerRows + ")";
    }

    public String createInsertColumn(LazorTableInfo<S> tableInfo) {
        List<String> columns = new ArrayList<>(tableInfo.columnInfoList().size());
        for (LazorColumnInfo lazorColumnInfo : tableInfo.columnInfoList()) {
            if (lazorColumnInfo.isTransient()) {
                continue;
            }
            columns.add(lazorColumnInfo.columnName());
        }
        return String.join(", ", columns);
    }

    public String createInsertReplacer(LazorTableInfo<S> tableInfo) {
        List<String> columns = new ArrayList<>(tableInfo.columnInfoList().size());
        for (LazorColumnInfo lazorColumnInfo : tableInfo.columnInfoList()) {
            if (lazorColumnInfo.isTransient()) {
                continue;
            }
            String columnRow = ":" + lazorColumnInfo.fieldName();
            columns.add(columnRow);
        }
        return String.join(", ", columns);
    }

    public String createConflictTarget(LazorTableInfo<S> tableInfo) {
        List<String> columns = new ArrayList<>(tableInfo.primaryKeyInfoList().size());
        for (LazorColumnInfo lazorColumnInfo : tableInfo.primaryKeyInfoList()) {
            if (lazorColumnInfo.isTransient()) {
                continue;
            }
            columns.add(lazorColumnInfo.columnName());
        }
        return String.join(", ", columns);
    }

    public String createDoNothing() {
        return "DO NOTHING";
    }

    public String createDoUpdate(LazorTableInfo<S> tableInfo) {
        String columnRows = createDoUpdateColumn(tableInfo);
        return "DO UPDATE SET " + columnRows;
    }

    public String createDoUpdateColumn(LazorTableInfo<S> tableInfo) {
        List<String> columns = new ArrayList<>(tableInfo.columnInfoList().size());
        for (LazorColumnInfo lazorColumnInfo : tableInfo.columnInfoList()) {
            if (lazorColumnInfo.isPrimaryKey()) {
                continue;
            }
            if (lazorColumnInfo.isTransient()) {
                continue;
            }
            String columnRow = lazorColumnInfo.columnName() + " = EXCLUDED." + lazorColumnInfo.columnName();
            columns.add(columnRow);
        }
        return String.join(", ", columns);
    }
}
