package com.ezreal.mybatis.mapping;

import com.ezreal.mybatis.session.Configuration;
import com.ezreal.mybatis.type.JdbcType;
import com.ezreal.mybatis.type.TypeHandler;

/**
 * 结果映射
 * @author Ezreal
 * @Date 2024/3/14
 */
public class ResultMapping {

    private Configuration configuration;

    private String property;

    private String column;

    private Class<?> javaType;

    private JdbcType jdbcType;

    private TypeHandler<?> typeHandler;

    public ResultMapping() {
    }
}
