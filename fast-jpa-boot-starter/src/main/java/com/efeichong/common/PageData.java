package com.efeichong.common;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author lxk
 * @date 2020/9/18
 * @description
 */
@Setter
@Getter
public class PageData<T> {
    /**
     * 每页多少条数据
     */
    private int pageSize;
    /**
     * 当前页码
     */
    private int pageIndex;
    /**
     * 总共多少页
     */
    private int totalPage;
    /**
     * 记录总数
     */
    private long totalRows;
    /**
     * 查询的数据
     */
    private List<T> list;
}
