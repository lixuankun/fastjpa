package com.efeichong.generator;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author lxk
 * @date 2020/11/25
 * @description 数据库连接配置
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DataSource {
    /**
     * 数据库连接地址
     */
    private String url;
    /**
     * 数据库连接账号
     */
    private String username;
    /**
     * 数据库连接密码
     */
    private String password;
    /**
     * 数据库连接驱动
     */
    private String driver;
}
