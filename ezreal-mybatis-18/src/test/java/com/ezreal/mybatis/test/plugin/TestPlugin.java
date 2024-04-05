package com.ezreal.mybatis.test.plugin;

import com.ezreal.mybatis.executor.statement.StatementHandler;
import com.ezreal.mybatis.mapping.BoundSql;
import com.ezreal.mybatis.plugin.Interceptor;
import com.ezreal.mybatis.plugin.Intercepts;
import com.ezreal.mybatis.plugin.Invocation;
import com.ezreal.mybatis.plugin.Signature;

import java.sql.Connection;
import java.util.Properties;

/**
 * @author Ezreal
 * @Date 2024/4/5
 */
@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class})})
public class TestPlugin implements Interceptor {

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();

        BoundSql boundSql = statementHandler.getBoundSql();

        String sql = boundSql.getSql();
        System.out.println("拦截SQL：" + sql);

        return invocation.proceed();
    }

    @Override
    public void setProperties(Properties properties) {
        System.out.println("参数输出：" + properties.getProperty("test00"));
    }
}
