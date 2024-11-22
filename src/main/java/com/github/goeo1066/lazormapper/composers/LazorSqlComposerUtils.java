package com.github.goeo1066.lazormapper.composers;

import com.github.goeo1066.lazormapper.ThrowableFunction;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.NonNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;

public class LazorSqlComposerUtils {
    public static <S> LazorTableInfo<S> retrieveTableInfo(Class<S> clazz) throws NoSuchMethodException {
        Table table = clazz.getAnnotation(Table.class);
        if (table == null) {
            throw new RuntimeException("No @Table annotation found");
        }

        String schema = table.schema();
        String tableName = table.name();

        if (tableName == null || tableName.isBlank()) {
            tableName = camelToSnake(clazz.getName(), true);
        }

        var fieldInfoList = retrieveColumnInfo(clazz);
        var primaryKeyInfoList = fieldInfoList.stream().filter(LazorTableInfo.LazorColumnInfo::isPrimaryKey).toList();
        var rowMapper = getRowMapperForRecord(clazz, fieldInfoList);
        return new LazorTableInfo<>(schema, tableName, fieldInfoList, primaryKeyInfoList, rowMapper);
    }

    public static List<LazorTableInfo.LazorColumnInfo> retrieveColumnInfo(Class<?> clazz) {
        if (!clazz.isRecord()) {
            throw new RuntimeException("Not a record class");
        }
        var recordComponents = clazz.getRecordComponents();
        if (recordComponents.length < 1) {
            throw new RuntimeException("Record class must have exactly one component");
        }
        var recordFieldArray = new LazorTableInfo.LazorColumnInfo[recordComponents.length];
        for (int i = 0; i < recordComponents.length; i++) {
            var builder = LazorTableInfo.LazorColumnInfo.builder();
            builder.columnName(camelToSnake(recordComponents[i].getName(), true));
            builder.fieldName(recordComponents[i].getName());
            builder.fieldType(recordComponents[i].getType());
            Method accessor = recordComponents[i].getAccessor();
            if (accessor != null) {
                builder.isPrimaryKey(accessor.isAnnotationPresent(Id.class));
                builder.isTransient(accessor.isAnnotationPresent(Transient.class));
            }
            recordFieldArray[i] = builder.build();
        }
        return List.of(recordFieldArray);
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
    public static <S> RowMapper<S> getRowMapperForRecord(Class<S> clazz, List<LazorTableInfo.LazorColumnInfo> columnInfoList) throws NoSuchMethodException {
        Function[] retrievers = new Function[columnInfoList.size()];
        Class[] fieldTypes = new Class[columnInfoList.size()];
        for (int i = 0; i < columnInfoList.size(); i++) {
            LazorTableInfo.LazorColumnInfo columnInfo = columnInfoList.get(i);
            Class fieldType = columnInfo.fieldType();
            String columnName = columnInfo.columnName();
            if (fieldType == String.class) {
                retrievers[i] = getDataRetrieverString(columnName, null);
            } else if (fieldType == Long.class) {
                retrievers[i] = getDataRetrieverLong(columnName, null);
            } else if (fieldType == long.class) {
                retrievers[i] = getDataRetrieverLong(columnName, 0L);
            } else if (fieldType == Integer.class) {
                retrievers[i] = getDataRetrieverInt(columnName, null);
            } else if (fieldType == int.class) {
                retrievers[i] = getDataRetrieverInt(columnName, 0);
            } else {
                throw new RuntimeException("Unsupported field type: " + fieldType);
            }
            fieldTypes[i] = fieldType;
        }

        var constructor = clazz.getConstructor(fieldTypes);
        return new RowMapper<>() {
            private Set<String> existingColumns = null;

            @Override
            @SuppressWarnings("unchecked")
            public S mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
                if (existingColumns == null) {
                    existingColumns = loadExistingColumns(rs);
                }
                Object[] initializers = new Object[columnInfoList.size()];
                for (int i = 0; i < columnInfoList.size(); i++) {
                    String columnName = columnInfoList.get(i).columnName();
                    if (!existingColumns.contains(columnName.toUpperCase()) || retrievers[i] == null) {
                        initializers[i] = getNullOrDefault(columnInfoList.get(i).fieldType());
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

    private static Set<String> loadExistingColumns(ResultSet rs) throws SQLException {
        int columnCount = rs.getMetaData().getColumnCount();
        Set<String> result = new HashSet<>();
        for (int i = 1; i <= columnCount; i++) {
            result.add(rs.getMetaData().getColumnName(i).toUpperCase());
        }
        return Collections.unmodifiableSet(result);
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

    private static Function<ResultSet, String> getDataRetrieverString(String columnName, String defaultValue) {
        return getDataRetriever(columnName, defaultValue, ResultSet::getString);
    }

    private static Function<ResultSet, Long> getDataRetrieverLong(String columnName, Long defaultValue) {
        return getDataRetriever(columnName, defaultValue, ResultSet::getLong);
    }

    private static Function<ResultSet, Integer> getDataRetrieverInt(String columnName, Integer defaultValue) {
        return getDataRetriever(columnName, defaultValue, ResultSet::getInt);
    }

    private static <T> Function<ResultSet, T> getDataRetriever(String columnName, T defaultValue, ThrowableFunction<ResultSet, String, T, Throwable> getter) {
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

    public static <T> Iterable<List<T>> partition(Collection<T> partition, int partitionSize) {
        Iterator<T> iterator = partition.iterator();
        return new Iterable<>() {
            @Override
            @NonNull
            public Iterator<List<T>> iterator() {
                return new Iterator<>() {
                    @Override
                    public boolean hasNext() {
                        return iterator.hasNext();
                    }

                    @Override
                    public List<T> next() {
                        return readNext();
                    }

                    private List<T> readNext() {
                        List<T> result = new ArrayList<>(partitionSize);
                        for (int i = 0; i < partitionSize; i++) {
                            if (iterator.hasNext()) {
                                result.add(iterator.next());
                            } else {
                                break;
                            }
                        }
                        return result;
                    }
                };
            }
        };
    }
}
