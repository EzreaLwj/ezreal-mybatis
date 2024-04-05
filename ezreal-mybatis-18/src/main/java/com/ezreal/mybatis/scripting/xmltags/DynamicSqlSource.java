package com.ezreal.mybatis.scripting.xmltags;

import com.ezreal.mybatis.builder.SqlSourceBuilder;
import com.ezreal.mybatis.mapping.BoundSql;
import com.ezreal.mybatis.mapping.SqlSource;
import com.ezreal.mybatis.session.Configuration;

import java.util.Map;

/**
 * @author Ezreal
 * @Date 2024/4/4
 */
public class DynamicSqlSource implements SqlSource {

    private Configuration configuration;

    private SqlNode rootSqlNode;

    public DynamicSqlSource(Configuration configuration, SqlNode rootSqlNode) {
        this.configuration = configuration;
        this.rootSqlNode = rootSqlNode;
    }

    @Override
    public BoundSql getBoundSql(Object parameterObject) {

        //生成一个DynamicContext动态上下文
        DynamicContext context = new DynamicContext(configuration, parameterObject);
        //sqlNode.apply 将${}参数替换掉，不替换#{}这种参数
        rootSqlNode.apply(context);
        // 调用SqlSourceBuilder
        SqlSourceBuilder sqlSourceParser = new SqlSourceBuilder(configuration);
        Class<?> parameterType = parameterObject == null ? Object.class : parameterObject.getClass();
        // SqlSourceBuilder.parse 这里返回的是 StaticSqlSource，解析过程就把那些参数都替换成?了，也就是最基本的JDBC的SQL语句。
        SqlSource sqlSource = sqlSourceParser.parse(context.getSql(), parameterType, context.getBindings());
        //SqlSource.getBoundSql，非递归调用，而是调用 StaticSqlSource实现类
        BoundSql boundSql = sqlSource.getBoundSql(parameterObject);
        for (Map.Entry<String, Object> entry : context.getBindings().entrySet()) {
            boundSql.setAdditionalParameter(entry.getKey(), entry.getValue());
        }

        return boundSql;
    }
}
