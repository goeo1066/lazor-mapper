package com.github.goeo1066.lazormapper.composers.key;

import com.github.goeo1066.lazormapper.composers.LazorColumnInfo;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.RecordComponent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecordKeyAssignerImpl<S> implements EntityKeyAssigner<S> {
    private final Map<String, Integer> keyFieldIndexMap;
    private final Class<?>[] parameterTypes;
    private final Class<S> entityClass;
    private final RecordComponent[] recordComponents;

    public RecordKeyAssignerImpl(Class<S> entityClass, List<LazorColumnInfo> columnInfos) {
        this.keyFieldIndexMap = initFieldIndexMap(columnInfos);
        this.parameterTypes = initParameterTypes(entityClass);
        this.recordComponents = entityClass.getRecordComponents();
        this.entityClass = entityClass;
    }

    private Class<?>[] initParameterTypes(Class<S> entityClass) {
        RecordComponent[] recordComponents = entityClass.getRecordComponents();
        Class<?>[] parameterTypes = new Class<?>[recordComponents.length];

        for (int i = 0; i < recordComponents.length; i++) {
            RecordComponent recordComponent = recordComponents[i];
            parameterTypes[i] = recordComponent.getType();
        }

        return parameterTypes;
    }

    private Map<String, Integer> initFieldIndexMap(List<LazorColumnInfo> columnInfos) {
        Map<String, Integer> fieldIndexMap = new HashMap<>();
        for (int i = 0; i < columnInfos.size(); i++) {
            LazorColumnInfo columnInfo = columnInfos.get(i);
            if (columnInfo.isPrimaryKey()) {
                fieldIndexMap.put(columnInfo.fieldName(), i);
            }
        }
        return fieldIndexMap;
    }

    @Override
    public S assignKeys(S entity, Map<String, Object> keyValues) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, InstantiationException {
        Object[] newValues = retrieveValues(entity);

        for (String key : keyFieldIndexMap.keySet()) {
            Integer index = keyFieldIndexMap.get(key);
            newValues[index] = keyValues.get(key);
        }

        return entityClass.getConstructor(parameterTypes).newInstance(newValues);
    }

    private Object[] retrieveValues(S entity) throws InvocationTargetException, IllegalAccessException {
        Object[] newValues = new Object[recordComponents.length];

        for (int i = 0; i < recordComponents.length; i++) {
            RecordComponent recordComponent = recordComponents[i];
            newValues[i] = recordComponent.getAccessor().invoke(entity);
        }

        return newValues;
    }
}
