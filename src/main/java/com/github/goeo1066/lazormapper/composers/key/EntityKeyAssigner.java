package com.github.goeo1066.lazormapper.composers.key;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public interface EntityKeyAssigner<S> {
    S assignKeys(S entity, Map<String, Object> keyValues) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, InstantiationException;
}
