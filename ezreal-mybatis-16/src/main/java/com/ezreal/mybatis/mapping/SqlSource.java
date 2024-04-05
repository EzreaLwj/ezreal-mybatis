package com.ezreal.mybatis.mapping;

/**
 * SQL源码
 * @author Ezreal
 * @Date 2024/3/12
 */
public interface SqlSource {

    BoundSql getBoundSql(Object parameterObject);
}
