package com.github.goeo1066.lazormapper.composers;

public record LazorColumnInfo(
        String columnName,
        String fieldName,
        String getterName,
        String setterName,
        Class<?> fieldType,
        boolean isPrimaryKey,
        boolean isTransient,
        boolean isRecordClass
) {
    public static LazorColumnInfo ofRecord(String columnName, String fieldName, Class<?> fieldType, boolean isPrimaryKey, boolean isTransient) {
        return new LazorColumnInfo
                .Builder(columnName, fieldType, isPrimaryKey, isTransient, true)
                .fieldName(fieldName)
                .build();
    }

    public static LazorColumnInfo ofClass(String columnName, String getterName, String setterName, Class<?> fieldType, boolean isPrimaryKey, boolean isTransient) {
        return new LazorColumnInfo
                .Builder(columnName, fieldType, isPrimaryKey, isTransient, false)
                .getterName(getterName)
                .setterName(setterName)
                .build();
    }

    public static class Builder {
        private final String columnName;
        private final Class<?> fieldType;
        private final boolean isPrimaryKey;
        private final boolean isTransient;
        private final boolean isRecordClass;

        private String fieldName;
        private String getterName;
        private String setterName;

        public Builder(String columnName,
                       Class<?> fieldType,
                       boolean isPrimaryKey,
                       boolean isTransient,
                       boolean isRecordClass) {
            this.columnName = columnName;
            this.fieldType = fieldType;
            this.isPrimaryKey = isPrimaryKey;
            this.isTransient = isTransient;
            this.isRecordClass = isRecordClass;
        }

        public Builder fieldName(String fieldName) {
            this.fieldName = fieldName;
            return this;
        }

        public Builder getterName(String getterName) {
            this.getterName = getterName;
            return this;
        }

        public Builder setterName(String setterName) {
            this.setterName = setterName;
            return this;
        }

        public LazorColumnInfo build() {
            return new LazorColumnInfo(
                    columnName,
                    fieldName,
                    getterName,
                    setterName,
                    fieldType,
                    isPrimaryKey,
                    isTransient,
                    isRecordClass
            );
        }
    }
}
