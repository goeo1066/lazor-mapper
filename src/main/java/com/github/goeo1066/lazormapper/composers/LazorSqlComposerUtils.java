package com.github.goeo1066.lazormapper.composers;

import com.github.goeo1066.lazormapper.ThrowableBigFunction;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.NonNull;

import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

public class LazorSqlComposerUtils {
    public static LazorTableInfo retrieveTableInfo(Class<?> clazz) {
        Table table = clazz.getAnnotation(Table.class);
        if (table == null) {
            throw new RuntimeException("No @Table annotation found");
        }

        String schema = table.schema();
        String tableName = table.name();

        if (tableName == null || tableName.isBlank()) {
            tableName = camelToSnake(clazz.getName(), true);
        }

        return new LazorTableInfo(schema, tableName);
    }

    private static String camelToSnake(String camel, boolean isUpper) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < camel.length(); i++) {
            char c = camel.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i != 0) {
                    result.append("_");
                }
                result.append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }

        if (isUpper) {
            return result.toString().toUpperCase();
        }

        return result.toString();
    }

    @SuppressWarnings("rawtypes")
    public static <S> RowMapper<S> getRowMapperForRecord(Class<S> entityClass) throws NoSuchMethodException {
        if (!entityClass.isRecord()) {
            throw new RuntimeException("Not a record class");
        }
        var recordComponents = entityClass.getRecordComponents();
        var recordFieldArray = new FieldColumnInfo[recordComponents.length];
        for (int i = 0; i < recordComponents.length; i++) {
            recordFieldArray[i] = new FieldColumnInfo();
            recordFieldArray[i].columnName = camelToSnake(recordComponents[i].getName(), true);
            recordFieldArray[i].fieldType = recordComponents[i].getType();
//            recordFieldNames[i] = camelToSnake(recordComponents[i].getName(), true);
        }

        Function[] retrievers = new Function[recordFieldArray.length];
        for (int i = 0; i < recordFieldArray.length; i++) {
            FieldColumnInfo fieldColumnInfo = recordFieldArray[i];
            if (fieldColumnInfo.fieldType == String.class) {
                retrievers[i] = getDataRetrieverString(fieldColumnInfo.columnName, null);
            } else if (fieldColumnInfo.fieldType == Long.class) {
                retrievers[i] = getDataRetrieverLong(fieldColumnInfo.columnName, null);
            } else if (fieldColumnInfo.fieldType == long.class) {
                retrievers[i] = getDataRetrieverLong(fieldColumnInfo.columnName, 0L);
            } else if (fieldColumnInfo.fieldType == Integer.class) {
                retrievers[i] = getDataRetrieverInt(fieldColumnInfo.columnName, null);
            } else if (fieldColumnInfo.fieldType == int.class) {
                retrievers[i] = getDataRetrieverInt(fieldColumnInfo.columnName, 0);
            } else {
                throw new RuntimeException("Unsupported field type: " + fieldColumnInfo.fieldType);
            }
        }

        Class[] fieldTypes = new Class[recordFieldArray.length];
        for (int i = 0; i < recordFieldArray.length; i++) {
            fieldTypes[i] = recordFieldArray[i].fieldType;
        }
        var constructor = entityClass.getConstructor(fieldTypes);

        return new RowMapper<>() {
            private Set<String> existingColumns = null;

            @Override
            @SuppressWarnings("unchecked")
            public S mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
                if (existingColumns == null) {
                    existingColumns = loadExistingColumns(rs);
                }
                Object[] initializers = new Object[recordFieldArray.length];
                for (int i = 0; i < recordFieldArray.length; i++) {
                    String columnName = recordFieldArray[i].columnName;
                    if (!existingColumns.contains(columnName.toUpperCase()) || retrievers[i] == null) {
                        initializers[i] = getNullOrDefault(recordFieldArray[i].fieldType);
                    } else {
                        initializers[i] = retrievers[i].apply(rs);
                    }
                }
                try {
                    return constructor.newInstance(initializers);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    private static Object getNullOrDefault(Class<?> clazz) {
        if (clazz == int.class) {
            return 0;
        } else if (clazz == short.class) {
            return 0;
        } else if (clazz == byte.class) {
            return 0;
        } else if (clazz == long.class) {
            return 0L;
        } else if (clazz == double.class) {
            return null;
        } else if (clazz == float.class) {
            return null;
        } else if (clazz == boolean.class) {
            return false;
        } else if (clazz == char.class) {
            return '\u0000';
        } else {
            return null;
        }
    }


    private static Set<String> loadExistingColumns(ResultSet rs) throws SQLException {
        int columnCount = rs.getMetaData().getColumnCount();
        Set<String> result = new HashSet<>();
        for (int i = 1; i <= columnCount; i++) {
            result.add(rs.getMetaData().getColumnName(i).toUpperCase());
        }
        return Collections.unmodifiableSet(result);
    }

    private static Function<ResultSet, String> getDataRetrieverString(String columnName, String defaultValue) {
        return getDataRetriever(columnName, defaultValue, ResultSet::getString);
    }

    private static Function<ResultSet, Long> getDataRetrieverLong(String columnName, Long defaultValue) {
        return getDataRetriever(columnName, defaultValue, ResultSet::getLong);
    }

    private static Function<ResultSet, Integer> getDataRetrieverInt(String columnName, Integer defaultValue) {
        return getDataRetriever(columnName, defaultValue, ResultSet::getInt);
    }

    private static <T> Function<ResultSet, T> getDataRetriever(String columnName, T defaultValue, ThrowableBigFunction<ResultSet, String, T, Throwable> getter) {
        return rs -> {
            try {
                T value = getter.apply(rs, columnName);
                if (value == null || rs.wasNull()) {
                    return defaultValue;
                }
                return value;
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        };
    }

    private static class FieldColumnInfo {
        private String columnName;
        private Class<?> fieldType;
    }
}
