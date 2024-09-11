package com.efeichong.uuid;


import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

import java.io.Serializable;

/**
 * 基于Snowflake算法优化实现64位自增ID算法。
 *
 * @author lxk
 * @date 2020/11/10
 * @description 1. 如果发现当前时间少于上次生成id的时间(时间回拨)，着计算回拨的时间差
 * 2. 如果时间差(offset)小于等于5ms，着等待 offset * 2 的时间再生成
 * 3. 如果offset大于5，则直接抛出异常
 */
public class IdGenerator implements IdentifierGenerator {
    private static IdWorker idWorker = IdWorker.getFlowIdWorkerInstance();

    public static long getId() {
        return idWorker.nextId();
    }

    public static String getIdStr() {
        return String.valueOf(idWorker.nextId());
    }

    @Override
    public Serializable generate(SharedSessionContractImplementor sharedSessionContractImplementor, Object o) throws HibernateException {
        return getId();
    }
}
