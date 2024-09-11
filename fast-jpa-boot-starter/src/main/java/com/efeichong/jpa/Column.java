package com.efeichong.jpa;

import com.efeichong.exception.JpaException;
import com.efeichong.util.EntityUtils;

/**
 * @author lxk
 * @date 2022/1/27
 * @description 以字段作为查询条件  例如: where column1=column2
 */
public class Column {

    private String column;

    private Column(String column) {
        this.column = column;
    }

    public static Column of(String column) {
        if (EntityUtils.isEmpty(column)) {
            throw new JpaException("column 不能为空");
        }
        return new Column(column);
    }

    protected String getColumn() {
        return column;
    }

    protected void setColumn(String column) {
        this.column = column;
    }
}
