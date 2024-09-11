package com.efeichong.jpa;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * @author lxk
 * @date 2022/4/20
 * @description 生成条件
 */
public interface ExecExpression {

    /**
     * 生成条件
     *
     * @param root
     * @param criteriaBuilder
     * @return Predicate
     */
    Predicate genExpression(Root root, CriteriaBuilder criteriaBuilder);


}
