package com.ezreal.mybatis.scripting.defaults;

import com.ezreal.mybatis.builder.SqlSourceBuilder;
import com.ezreal.mybatis.mapping.BoundSql;
import com.ezreal.mybatis.mapping.SqlSource;
import com.ezreal.mybatis.scripting.xmltags.DynamicContext;
import com.ezreal.mybatis.scripting.xmltags.SqlNode;
import com.ezreal.mybatis.session.Configuration;

import java.util.HashMap;

/**
 * 原始SQL源码，比 DynamicSqlSource 动态SQL处理快
 * @author Ezreal
 * @Date 2024/3/13
 */
public class RawSqlSource implements SqlSource {

    private SqlSource sqlSource;

    public RawSqlSource(Configuration configuration, SqlNode rootSqlNode, Class<?> parameterType) {
        this(configuration, getSql(configuration, rootSqlNode), parameterType);
    }

    private static String getSql(Configuration configuration, SqlNode rootSqlNode) {
        DynamicContext context = new DynamicContext(configuration, null);
        rootSqlNode.apply(context);
        return context.getSql();
    }

    public RawSqlSource(Configuration configuration, String sql, Class<?> parameterType) {
        SqlSourceBuilder sqlSourceBuilder = new SqlSourceBuilder(configuration);
        Class<?> clazz = parameterType == null ? Object.class : parameterType;
        sqlSource = sqlSourceBuilder.parse(sql, clazz, new HashMap<>());
    }


    @Override
    public BoundSql getBoundSql(Object parameterObject) {
        return sqlSource.getBoundSql(parameterObject);
    }
}
