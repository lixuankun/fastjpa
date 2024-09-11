package com.efeichong.jpa;

import lombok.Getter;

/**
 * @author lxk
 * @date 2020/9/8
 * @description 模糊查询类型 开头,结尾,包含
 */
@Getter
public enum LikeType {
    //以%开头
    LEFT,
    //以%结尾
    RIGHT,
    //以%开头以%结尾
    CONTAINS;
}
