package com.github.goeo1066.lazormapper.repository;

import com.github.goeo1066.lazormapper.composers.LazorSelectSqlComposer;
import com.github.goeo1066.lazormapper.composers.LazorSqlComposerUtils;
import com.github.goeo1066.lazormapper.composers.LazorTableInfo;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.List;
import java.util.Map;

public class LazorCrudRepositoryProxyDelegate<S> {
    private final LazorSelectSqlComposer composer;
    private final RowMapper<S> rowMapper;
    private final LazorTableInfo tableInfo;

    public LazorCrudRepositoryProxyDelegate(
            LazorSelectSqlComposer composer,
            RowMapper<S> rowMapper,
            LazorTableInfo tableInfo
    ) {
        this.composer = composer;
        this.rowMapper = rowMapper;
        this.tableInfo = tableInfo;
    }

    public static <S> LazorCrudRepositoryProxyDelegate<S> create(Class<S> entityClass, String dbType) throws NoSuchMethodException {
        LazorSelectSqlComposer composer = LazorSelectSqlComposer.createInstanceOf(dbType);
        RowMapper<S> rowMapper = LazorSqlComposerUtils.getRowMapperForRecord(entityClass);
        LazorTableInfo tableInfo = LazorSqlComposerUtils.retrieveTableInfo(entityClass);
        return new LazorCrudRepositoryProxyDelegate<>(composer, rowMapper, tableInfo);
    }

    public List<S> select(NamedParameterJdbcTemplate jdbcTemplate, LazorSelectSpec selectSpec) {
        final String sql = composer.composeSelectSql(tableInfo, selectSpec);
        try (var stream = jdbcTemplate.queryForStream(sql, Map.of(), rowMapper)) {
            return stream.toList();
        }
    }

    public long count(NamedParameterJdbcTemplate jdbcTemplate, LazorSelectSpec selectSpec) {
        final String sql = composer.composeCountSql(tableInfo, selectSpec);
        Long count = jdbcTemplate.queryForObject(sql, Map.of(), Long.class);
        return count == null ? 0 : count;
    }
}
