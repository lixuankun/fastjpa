package com.efeichong.config;

import com.efeichong.cache.EntityCache;
import com.efeichong.jpa.UpdateGlobalConfig;
import com.efeichong.util.EntityUtils;
import org.hibernate.EmptyInterceptor;
import org.hibernate.type.Type;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Optional;

public class CustomInterceptor extends EmptyInterceptor {

    @Override
    public boolean onFlushDirty(Object entity, Serializable id, Object[] currentState, Object[] previousState, String[] propertyNames, Type[] types) {
        EntityCache entityCache = EntityCache.forClass(entity.getClass());

        Optional<Field> optional = entityCache.getAllFields().stream().filter(field -> field.getType().isAssignableFrom(UpdateGlobalConfig.class))
                .findFirst();
        UpdateGlobalConfig config = null;
        if (optional.isPresent()) {
            config = (UpdateGlobalConfig) entityCache.getValue(entity, optional.get().getName());
        }
        if (config == null) {
            config = UpdateGlobalConfig.DEFAULT_UPDATE_GLOBAL_CONFIG;
        }

        //忽略更新的字段
        String[] ignoreUpdateProps = config.ignoreUpdateProps();
        //可以更新为空的字段
        String[] canBeNullProps = config.canBeNullProps();

        for (int i = 0; i < currentState.length; i++) {
            String propertyName = propertyNames[i];
            if (EntityUtils.contains(ignoreUpdateProps, propertyName)) {
                currentState[i] = previousState[i];
                continue;
            }
            if (!EntityUtils.contains(canBeNullProps, propertyName) && EntityUtils.isEmpty(currentState[i])) {
                currentState[i] = previousState[i];
            }
        }
        return false;
    }



}
