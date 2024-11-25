package com.github.goeo1066.lazormapper.composers.update;

import com.github.goeo1066.lazormapper.composers.LazorColumnInfo;
import com.github.goeo1066.lazormapper.composers.LazorTableInfo;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class LazorUpdateSqlComposerPostgreSQL<S> implements LazorUpdateSqlComposer<S> {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(LazorUpdateSqlComposerPostgreSQL.class);

    @Override
    public String composeUpdateSql(LazorTableInfo<S> tableInfo, LazorUpdateSpec<S> updateSpec) {
        String tableName = tableInfo.tableFullName();
        String setClause = setClauseForUpdate(tableInfo);
        String whereClause = whereClauseForUpdate(tableInfo);
        String updateSql = "UPDATE " + tableName + " SET " + setClause + " WHERE 1 = 1 AND (" + whereClause + ")";
        logger.debug("Update SQL: {}", updateSql);
        return updateSql;
    }

    private String setClauseForUpdate(LazorTableInfo<S> tableInfo) {
        List<String> rows = new ArrayList<>();
        for (LazorColumnInfo lazorColumnInfo : tableInfo.columnInfoList()) {
            if (lazorColumnInfo.isTransient() || lazorColumnInfo.isPrimaryKey()) {
                continue;
            }
            rows.add(lazorColumnInfo.columnName() + " = :" + lazorColumnInfo.fieldName());
        }
        return String.join(", ", rows);
    }

    private String whereClauseForUpdate(LazorTableInfo<S> tableInfo) {
        List<String> rows = new ArrayList<>();
        for (LazorColumnInfo lazorColumnInfo : tableInfo.columnInfoList()) {
            if (!lazorColumnInfo.isPrimaryKey()) {
                continue;
            }
            rows.add(lazorColumnInfo.columnName() + " = :" + lazorColumnInfo.fieldName());
        }
        return String.join(" AND ", rows);
    }
}
