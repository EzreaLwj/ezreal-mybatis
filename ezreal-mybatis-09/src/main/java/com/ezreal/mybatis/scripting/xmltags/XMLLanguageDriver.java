package com.ezreal.mybatis.scripting.xmltags;

import com.ezreal.mybatis.executor.parameter.ParameterHandler;
import com.ezreal.mybatis.mapping.BoundSql;
import com.ezreal.mybatis.mapping.MappedStatement;
import com.ezreal.mybatis.mapping.SqlSource;
import com.ezreal.mybatis.scripting.LanguageDriver;
import com.ezreal.mybatis.scripting.defaults.DefaultParameterHandler;
import com.ezreal.mybatis.session.Configuration;
import org.dom4j.Element;

/**
 * XML语言驱动器
 * @author Ezreal
 * @Date 2024/3/13
 */
public class XMLLanguageDriver implements LanguageDriver {

    @Override
    public SqlSource createSqlSource(Configuration configuration, Element script, Class<?> parameterType) {
        XMLScriptBuilder builder = new XMLScriptBuilder(configuration, script, parameterType);
        return builder.parseScriptNode();
    }

    @Override
    public ParameterHandler createParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql) {
        return new DefaultParameterHandler(mappedStatement, parameterObject, boundSql);
    }
}
