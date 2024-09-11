package com.efeichong.jpa;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lxk
 * @date 2022/1/25
 * @description
 */
@AllArgsConstructor
@Data
public class QueryCacheResult {
    private Root root;
    private CriteriaQuery query;
    private List<TupleGroup> groups = new ArrayList();
}
