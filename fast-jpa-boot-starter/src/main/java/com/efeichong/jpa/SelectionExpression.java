package com.efeichong.jpa;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Selection;

/**
 * @author lxk
 * @date 2022/4/20
 * @description 生成条件
 */
public interface SelectionExpression {

    Selection genSelection(Root root, CriteriaBuilder criteriaBuilder);


}
