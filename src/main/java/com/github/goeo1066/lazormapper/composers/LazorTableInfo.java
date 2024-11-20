package com.github.goeo1066.lazormapper.composers;

public record LazorTableInfo(
        String schema,
        String tableName
) {
    public String tableFullName() {
        if (schema == null || schema.isBlank()) {
            return tableName;
        }
        return schema + "." + tableName;
    }
}
