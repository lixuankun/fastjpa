package com.efeichong.jpa;

import com.efeichong.cache.EntityCache;
import com.efeichong.exception.JpaException;
import com.efeichong.logicDelete.LogicDelete;
import com.efeichong.multiTenant.EnableMultiTenant;
import com.efeichong.util.EntityUtils;
import com.efeichong.util.FastJpaSpringUtils;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import java.lang.reflect.Field;

/**
 * @author lxk
 * @date 2020/9/8
 * @description 基础dao 包含增删改查及native查询
 */
 public abstract class AbstractFastJpaRepository<T, ID> extends SimpleJpaRepository<T, ID>  {
    /**
     * 表名
     */
    protected String tableName;
    /**
     * 逻辑删除字段
     */
    protected Field logicDeleteField;
    /**
     * 对象T缓存
     */
    protected EntityCache entityCache;
    /**
     * 主键字段名
     */
    protected String primaryKey;
    /**
     * 是否开启多租户 true:开启 false:不开启
     */
    protected boolean hasEnableMultiTenant;
    /**
     * 是否开启逻辑删除 true:开启 false:不开启
     */
    protected boolean hasFakeDel;
    protected EntityManager entityManager;

    protected String logicDeleteColumn;


    protected JpaEntityInformation<T, ?> entityInformation;




    public AbstractFastJpaRepository(Class<T> domainClass, EntityManager em) {
        this(JpaEntityInformationSupport.getEntityInformation(domainClass, em), em);
    }

    public AbstractFastJpaRepository(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.entityManager = entityManager;
        this.entityInformation = entityInformation;
        this.primaryKey = entityInformation.getIdAttribute().getName();
        this.entityCache = EntityCache.forClass(entityInformation.getJavaType());
        this.hasEnableMultiTenant = entityInformation.getJavaType().isAnnotationPresent(EnableMultiTenant.class);
        this.tableName = EntityUtils.convertToLine(entityInformation.getEntityName());
        if (entityInformation.getJavaType().isAnnotationPresent(LogicDelete.class)) {
            this.hasFakeDel = true;
            LogicDelete logicDelete = entityInformation.getJavaType().getDeclaredAnnotation(LogicDelete.class);
            this.logicDeleteColumn = logicDelete.column();
            if (EntityUtils.isEmpty(logicDeleteColumn)) {
                throw new JpaException("请指定逻辑删除的字段,类型[long,Long,String]");
            }
            this.logicDeleteField = entityCache.getField(this.logicDeleteColumn);
            if (logicDeleteField == null) {
                throw new JpaException("字段" + logicDeleteColumn + "在" + entityInformation.getJavaType().toString() + "中不存在");
            }

            if (!logicDeleteField.getType().equals(String.class) &&
                    !logicDeleteField.getType().equals(Long.class) &&
                    !logicDeleteField.getType().equals(long.class)) {
                throw new JpaException(entityInformation.getJavaType().toString() + "指定逻辑删除的字段类型必须为[long,Long,String]类型");
            }
        }
    }

}
