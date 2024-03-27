package com.ezreal.mybatis.builder;

import com.ezreal.mybatis.mapping.BoundSql;
import com.ezreal.mybatis.mapping.ParameterMapping;
import com.ezreal.mybatis.mapping.SqlSource;
import com.ezreal.mybatis.session.Configuration;

import java.util.List;

/**
 * 静态SQL源码
 * @author Ezreal
 * @Date 2024/3/13
 */
public class StaticSqlSource implements SqlSource {

    private String sql;

    private List<ParameterMapping> parameterMappings;

    private Configuration configuration;

    public StaticSqlSource(String sql, Configuration configuration) {
        this(sql, null, configuration);
    }

    public StaticSqlSource(String sql, List<ParameterMapping> parameterMappings, Configuration configuration) {
        this.sql = sql;
        this.parameterMappings = parameterMappings;
        this.configuration = configuration;
    }

    @Override
    public BoundSql getBoundSql(Object parameterObject) {
        return new BoundSql(configuration, sql, parameterMappings, parameterObject);
    }

}
