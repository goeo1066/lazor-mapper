package com.github.goeo1066.lazormapper.composers;

public record LazorColumnInfo(
        String columnName,
        String fieldName,
        Class<?> fieldType,
        boolean isPrimaryKey,
        boolean isTransient
) {
    public static LazorColumnInfo.Builder builder() {
        return new LazorColumnInfo.Builder();
    }

    public static class Builder {
        private String columnName;
        private String fieldName;
        private Class<?> fieldType;
        private boolean isPrimaryKey;
        private boolean isTransient;

        public LazorColumnInfo.Builder columnName(String columnName) {
            this.columnName = columnName;
            return this;
        }

        public LazorColumnInfo.Builder fieldName(String fieldName) {
            this.fieldName = fieldName;
            return this;
        }

        public LazorColumnInfo.Builder fieldType(Class<?> fieldType) {
            this.fieldType = fieldType;
            return this;
        }

        public LazorColumnInfo.Builder isPrimaryKey(boolean isPrimaryKey) {
            this.isPrimaryKey = isPrimaryKey;
            return this;
        }

        public LazorColumnInfo.Builder isTransient(boolean isTransient) {
            this.isTransient = isTransient;
            return this;
        }

        public LazorColumnInfo build() {
            return new LazorColumnInfo(columnName, fieldName, fieldType, isPrimaryKey, isTransient);
        }
    }
}
