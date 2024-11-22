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

    public record LazorColumnInfo(
            String columnName,
            String fieldName,
            Class<?> fieldType,
            boolean isPrimaryKey,
            boolean isTransient
    ) {
        public static LazorColumnInfo.Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String columnName;
            private String fieldName;
            private Class<?> fieldType;
            private boolean isPrimaryKey;
            private boolean isTransient;

            public Builder columnName(String columnName) {
                this.columnName = columnName;
                return this;
            }

            public Builder fieldName(String fieldName) {
                this.fieldName = fieldName;
                return this;
            }

            public Builder fieldType(Class<?> fieldType) {
                this.fieldType = fieldType;
                return this;
            }

            public Builder isPrimaryKey(boolean isPrimaryKey) {
                this.isPrimaryKey = isPrimaryKey;
                return this;
            }

            public Builder isTransient(boolean isTransient) {
                this.isTransient = isTransient;
                return this;
            }

            public LazorColumnInfo build() {
                return new LazorColumnInfo(columnName, fieldName, fieldType, isPrimaryKey, isTransient);
            }
        }
    }
}
