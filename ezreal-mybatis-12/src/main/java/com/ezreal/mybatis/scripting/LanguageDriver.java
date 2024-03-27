package com.ezreal.mybatis.scripting;

import com.ezreal.mybatis.executor.parameter.ParameterHandler;
import com.ezreal.mybatis.mapping.BoundSql;
import com.ezreal.mybatis.mapping.MappedStatement;
import com.ezreal.mybatis.mapping.SqlSource;
import com.ezreal.mybatis.session.Configuration;
import org.dom4j.Element;

/**
 * 脚本语言驱动
 * @author Ezreal
 * @Date 2024/3/12
 */
public interface LanguageDriver {

    SqlSource createSqlSource(Configuration configuration, Element object, Class<?> parameterType);

    SqlSource createSqlSource(Configuration configuration, String script, Class<?> parameterType);

    ParameterHandler createParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql);
}
