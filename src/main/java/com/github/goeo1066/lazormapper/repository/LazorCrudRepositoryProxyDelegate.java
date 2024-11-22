package com.github.goeo1066.lazormapper.repository;

import com.github.goeo1066.lazormapper.composers.*;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LazorCrudRepositoryProxyDelegate<S> {
    private final LazorSelectSqlComposer<S> selectSqlComposer;
    private final LazorInsertSqlComposer<S> insertSqlComposer;
    private final LazorTableInfo<S> tableInfo;
    private Set<String> columnNames;

    public LazorCrudRepositoryProxyDelegate(
            LazorSelectSqlComposer<S> selectSqlComposer,
            LazorInsertSqlComposer<S> insertSqlComposer,
            LazorTableInfo<S> tableInfo
    ) {
        this.selectSqlComposer = selectSqlComposer;
        this.insertSqlComposer = insertSqlComposer;
        this.tableInfo = tableInfo;
    }

    public static <S> LazorCrudRepositoryProxyDelegate<S> create(Class<S> entityClass, String dbType) throws NoSuchMethodException {
        LazorSelectSqlComposer<S> selectComposer = LazorSelectSqlComposer.createInstanceOf(dbType);
        LazorInsertSqlComposer<S> insertComposer = LazorInsertSqlComposer.createInstanceOf(dbType);
        LazorTableInfo<S> tableInfo = LazorSqlComposerUtils.retrieveTableInfo(entityClass);
        return new LazorCrudRepositoryProxyDelegate<>(selectComposer, insertComposer, tableInfo);
    }

    public List<S> select(NamedParameterJdbcTemplate jdbcTemplate, LazorSelectSpec selectSpec) {
        final String sql = selectSqlComposer.composeSelectSql(tableInfo, selectSpec);
        try (var stream = jdbcTemplate.queryForStream(sql, Map.of(), tableInfo.rowMapper())) {
            return stream.toList();
        }
    }

    public long count(NamedParameterJdbcTemplate jdbcTemplate, LazorSelectSpec selectSpec) {
        final String sql = selectSqlComposer.composeCountSql(tableInfo, selectSpec);
        Long count = jdbcTemplate.queryForObject(sql, Map.of(), Long.class);
        return count == null ? 0 : count;
    }

    // todo return Optional<S>
    public void insert(NamedParameterJdbcTemplate jdbcTemplate, Collection<S> entities) {
        final String sql = insertSqlComposer.composeInsertSql(tableInfo, null);
        for (List<S> entitySublist : LazorSqlComposerUtils.partition(entities, 500)) {
            BeanPropertySqlParameterSource[] sqlParameterSources = new BeanPropertySqlParameterSource[entitySublist.size()];
            for (int i = 0; i < entitySublist.size(); i++) {
                sqlParameterSources[i] = new BeanPropertySqlParameterSource(entitySublist.get(i));
            }
            jdbcTemplate.batchUpdate(sql, sqlParameterSources);
            // todo get generated keys and assign to entities
        }
    }

    // todo load existing columns from database
}
