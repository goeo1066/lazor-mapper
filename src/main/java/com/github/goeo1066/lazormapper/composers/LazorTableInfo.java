package com.github.goeo1066.lazormapper.composers;

import org.springframework.jdbc.core.RowMapper;

import java.util.List;

public record LazorTableInfo<S>(
        String schema,
        String tableName,
        List<LazorColumnInfo> columnInfoList,
        List<LazorColumnInfo> primaryKeyInfoList,
        RowMapper<S> rowMapper
) {

    public String tableFullName() {
        if (schema == null || schema.isBlank()) {
            return tableName;
        }
        return schema + "." + tableName;
    }
}
