package com.efeichong.jpa;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

/**
 * @author lxk
 * @date 2022/4/20
 * @description 生成条件
 */
public interface SortExecExpression {

    /**
     * 生成条件
     *
     * @param root
     * @param criteriaBuilder
     * @return Predicate
     */
    Expression genExpression(Root root, CriteriaBuilder criteriaBuilder);


}
