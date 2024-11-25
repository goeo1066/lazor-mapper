package com.github.goeo1066.lazormapper.repository;

import com.github.goeo1066.lazormapper.composers.LazorSqlComposerUtils;
import com.github.goeo1066.lazormapper.composers.LazorTableInfo;
import com.github.goeo1066.lazormapper.composers.insert.LazorInsertSqlComposer;
import com.github.goeo1066.lazormapper.composers.key.RecordKeyAssignerImpl;
import com.github.goeo1066.lazormapper.composers.select.LazorSelectSpec;
import com.github.goeo1066.lazormapper.composers.select.LazorSelectSqlComposer;
import com.github.goeo1066.lazormapper.composers.update.LazorUpdateSqlComposer;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class LazorCrudRepositoryProxyDelegate<S> {
    private final LazorSelectSqlComposer<S> selectSqlComposer;
    private final LazorInsertSqlComposer<S> insertSqlComposer;
    private final LazorUpdateSqlComposer<S> updateSqlComposer;
    private final RecordKeyAssignerImpl<S> recordKeyAssigner;
    private final LazorTableInfo<S> tableInfo;

    public LazorCrudRepositoryProxyDelegate(
            Class<S> entityClass,
            LazorSelectSqlComposer<S> selectSqlComposer,
            LazorInsertSqlComposer<S> insertSqlComposer,
            LazorUpdateSqlComposer<S> updateSqlComposer,
            LazorTableInfo<S> tableInfo
    ) {
        this.recordKeyAssigner = new RecordKeyAssignerImpl<>(entityClass, tableInfo.columnInfoList());
        this.selectSqlComposer = selectSqlComposer;
        this.insertSqlComposer = insertSqlComposer;
        this.updateSqlComposer = updateSqlComposer;
        this.tableInfo = tableInfo;
    }

    public static <S> LazorCrudRepositoryProxyDelegate<S> create(Class<S> entityClass, String dbType) throws NoSuchMethodException {
        LazorSelectSqlComposer<S> selectComposer = LazorSelectSqlComposer.createInstanceOf(dbType);
        LazorInsertSqlComposer<S> insertComposer = LazorInsertSqlComposer.createInstanceOf(dbType);
        LazorUpdateSqlComposer<S> updateComposer = LazorUpdateSqlComposer.createInstanceOf(dbType);
        LazorTableInfo<S> tableInfo = LazorSqlComposerUtils.retrieveTableInfo(entityClass);
        return new LazorCrudRepositoryProxyDelegate<>(
                entityClass,
                selectComposer,
                insertComposer,
                updateComposer,
                tableInfo
        );
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

    public List<S> insert(NamedParameterJdbcTemplate jdbcTemplate, Collection<S> entities) {
        final String sql = insertSqlComposer.composeInsertSql(tableInfo, null);
        List<S> result = new ArrayList<>(entities.size());
        for (List<S> entitySublist : LazorSqlComposerUtils.partition(entities, 500)) {
            GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
            BeanPropertySqlParameterSource[] sqlParameterSources = new BeanPropertySqlParameterSource[entitySublist.size()];
            for (int i = 0; i < entitySublist.size(); i++) {
                sqlParameterSources[i] = new BeanPropertySqlParameterSource(entitySublist.get(i));
            }
            jdbcTemplate.batchUpdate(sql, sqlParameterSources, keyHolder);

            for (int i = 0; i < entitySublist.size(); i++) {
                S entity = entitySublist.get(i);
                var keyMap = keyHolder.getKeyList().get(i);
                try {
                    S newEntity = recordKeyAssigner.assignKeys(entity, keyMap);
                    result.add(newEntity);
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException |
                         InstantiationException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return result;
    }

    public void update(NamedParameterJdbcTemplate jdbcTemplate, Collection<S> entities) {
        final String sql = updateSqlComposer.composeUpdateSql(tableInfo, null);
        for (List<S> entitySublist : LazorSqlComposerUtils.partition(entities, 500)) {
            BeanPropertySqlParameterSource[] sqlParameterSources = new BeanPropertySqlParameterSource[entitySublist.size()];
            for (int i = 0; i < entitySublist.size(); i++) {
                sqlParameterSources[i] = new BeanPropertySqlParameterSource(entitySublist.get(i));
            }
            jdbcTemplate.batchUpdate(sql, sqlParameterSources);
        }
    }
}
