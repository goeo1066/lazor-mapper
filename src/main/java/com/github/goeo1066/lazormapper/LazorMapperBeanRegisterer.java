package com.github.goeo1066.lazormapper;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AbstractTypeHierarchyTraversingFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.lang.NonNull;

import java.io.IOException;
import java.lang.reflect.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;

@Configuration
public class LazorMapperBeanRegisterer {
    private static final Logger logger = LoggerFactory.getLogger(LazorMapperBeanRegisterer.class);
    private final ApplicationContext applicationContext;
    private final ConfigurableApplicationContext configurableApplicationContext;

    public LazorMapperBeanRegisterer(ApplicationContext applicationContext, ConfigurableApplicationContext configurableApplicationContext) {
        this.applicationContext = applicationContext;
        this.configurableApplicationContext = configurableApplicationContext;
    }

    @PostConstruct
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void init() {
        var list = scanImpls(LazorCrudRepository.class);
        for (WalkClassInfo walkClassInfo : list) {
            LazorCrudRepository t = getInstance(walkClassInfo.subInterface, walkClassInfo.entityClass);
            registerBean(walkClassInfo.subInterface, t);
            logger.info("Bean registered: {}", walkClassInfo.subInterface.getCanonicalName());
        }
        logger.info("Hello, Spring Boot!");
    }

    public <T> void registerBean(Class<T> clazz, T bean) {
        BeanDefinitionRegistry registry = (BeanDefinitionRegistry) configurableApplicationContext.getBeanFactory();
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(clazz, () -> bean);
        registry.registerBeanDefinition(clazz.getName(), beanDefinitionBuilder.getBeanDefinition());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public <S, T extends LazorCrudRepository> T getInstance(Class<T> subInterfaceClass, Class<S> entityClass) {
        return (T) Proxy.newProxyInstance(subInterfaceClass.getClassLoader(), new Class[]{subInterfaceClass}, new InvocationHandler() {
            @Override
            public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
                if ("find".equals(method.getName())) {
                    logger.info("invoke {}.{}", subInterfaceClass.getName(), "find");
                    return List.of();
                }
                return InvocationHandler.invokeDefault(o, method, objects);
            }
        });
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

    private static Function<ResultSet, String> getDataRetrieverString(String columnName, String defaultValue) {
        return getDataRetriever(columnName, defaultValue, ResultSet::getString);
    }

    private static Function<ResultSet, Long> getDataRetrieverLong(String columnName, Long defaultValue) {
        return getDataRetriever(columnName, defaultValue, ResultSet::getLong);
    }

    private static Function<ResultSet, Integer> getDataRetrieverInt(String columnName, Integer defaultValue) {
        return getDataRetriever(columnName, defaultValue, ResultSet::getInt);
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
    private <T extends LazorCrudRepository> List<WalkClassInfo> scanImpls(Class<T> clazz) {
        String basePackage = getBasePackage();
        if (basePackage == null) {
            throw new RuntimeException("No base package found");
        }
        return findSubClasses(clazz, basePackage);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private <S extends LazorCrudRepository> List<WalkClassInfo> findSubClasses(Class<S> clazz, String basePackage) {
        List<MetadataReader> metadataReaders = new ArrayList<>();

        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(walkSuperClasses(clazz, metadataReaders));
        provider.findCandidateComponents(basePackage);

        List<WalkClassInfo> result = new ArrayList<>();
        for (MetadataReader metadataReader : metadataReaders) {
            try {
                Class<S> subclass = (Class<S>) Class.forName(metadataReader.getClassMetadata().getClassName());
                WalkClassInfo walkClassInfo = new WalkClassInfo();
                walkClassInfo.subInterface = subclass;
                if (subclass.getGenericInterfaces().length == 1) {
                    if (subclass.getGenericInterfaces()[0] instanceof ParameterizedType p) {
                        String typeName = p.getActualTypeArguments()[0].getTypeName();
                        walkClassInfo.entityClass = Class.forName(typeName);
                    }
                }
                result.add(walkClassInfo);
            } catch (ClassNotFoundException e) {
                logger.warn("Class not found", e);
            }
        }
        return result;
    }

    private <T> TypeFilter walkSuperClasses(Class<T> clazz, List<MetadataReader> out) {
        return new AbstractTypeHierarchyTraversingFilter(true, true) {
            @Override
            public boolean match(@NonNull MetadataReader metadataReader, @NonNull MetadataReaderFactory metadataReaderFactory) throws IOException {
                String[] interfaces = metadataReader.getClassMetadata().getInterfaceNames();
                if (contains(clazz.getCanonicalName(), interfaces)) {
                    out.add(metadataReader);
                }
                return super.match(metadataReader, metadataReaderFactory);
            }
        };
    }

    private boolean contains(String target, String[] array) {
        for (String s : array) {
            if (s.equals(target)) {
                return true;
            }
        }
        return false;
    }

    private String getBasePackage() {
        for (Map.Entry<String, Object> stringObjectEntry : applicationContext.getBeansWithAnnotation(ComponentScan.class).entrySet()) {
            return stringObjectEntry.getValue().getClass().getPackageName();
        }
        return null;
    }

    @SuppressWarnings("rawtypes")
    private static class WalkClassInfo<S extends LazorCrudRepository> {
        private Class<S> subInterface;
        private Class<?> entityClass;
    }

    public static <T> List<T> select(Class<T> clazz, NamedParameterJdbcTemplate jdbcTemplate, String whereClauseTemplate) throws NoSuchMethodException {
        RowMapper<T> rowMapper = getRowMapperForRecord(clazz);
        String selectSql = createSelectSql(clazz, "T", whereClauseTemplate);
        try (var stream = jdbcTemplate.queryForStream(selectSql, Map.of(), rowMapper)){
            return stream.toList();
        }
    }

    public static long count(Class<?> clazz, NamedParameterJdbcTemplate jdbcTemplate, String whereClauseTemplate) throws NoSuchMethodException {
        String countSql = createCountSql(clazz, "T");
        Long count = jdbcTemplate.queryForObject(countSql, Map.of(), Long.class);
        return count == null ? 0 : count;
    }

    public static String createCountSql(Class<?> clazz, String mainTableAlias) throws NoSuchMethodException {
        return createSelectSqlTemplate(clazz, mainTableAlias, "COUNT(*)", null);
    }

    public static String createSelectSql(Class<?> clazz, String mainTableAlias, String whereClauseTemplate) throws NoSuchMethodException {
        return createSelectSqlTemplate(clazz, mainTableAlias, "*", whereClauseTemplate);
    }

    public static String createSelectSqlTemplate(Class<?> clazz, String mainTableAlias, String columnReplacer, String whereClauseTemplate) throws NoSuchMethodException {
        String createSelectSqlTemplate = createSelectSubSqlTemplate(clazz, whereClauseTemplate);
        return "SELECT " + columnReplacer + " FROM (" + createSelectSqlTemplate + ") " + mainTableAlias;
    }

    public static String createSelectSubSqlTemplate(Class<?> clazz, String whereClauseTemplate) {
        String tableName = retrieveTableName(clazz);
        String selectSql = "SELECT * FROM " + tableName + " ";
        if (whereClauseTemplate != null && !whereClauseTemplate.isBlank()) {
            selectSql += "WHERE " + whereClauseTemplate;
        }
        return selectSql;
    }

    public static String retrieveTableName(Class<?> clazz) {
        Table table = clazz.getAnnotation(Table.class);
        if (table == null) {
            throw new RuntimeException("No @Table annotation found");
        }

        String schema = table.schema();
        String tableName = table.name();

        if (tableName == null || tableName.isBlank()) {
            tableName = camelToSnake(clazz.getName(), true);
        }

        String tableFullName;
        if (schema == null || schema.isBlank()) {
            tableFullName = tableName;
        } else {
            tableFullName = schema + "." + tableName;
        }
        return tableFullName;
    }
}
