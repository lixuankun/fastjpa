package com.efeichong.proxy;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lxk
 * @date 2021/1/18
 * @description 代理类的属性描述
 */
@AllArgsConstructor
@Setter
@Getter
public class PropDesc {
    /**
     * 字段名
     */
    private String fieldName;
    /**
     * 字段类型
     */
    private Class<?> clazz;
    /**
     * 字段的值
     */
    private Object value;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private List<PropDesc> propDescList = new ArrayList<>();

        public Builder add(String fieldName, Class<?> clazz, Object value) {
            PropDesc propDesc = new PropDesc(fieldName, clazz, value);
            this.propDescList.add(propDesc);
            return this;
        }

        public List<PropDesc> build() {
            return this.propDescList;
        }

    }
}
