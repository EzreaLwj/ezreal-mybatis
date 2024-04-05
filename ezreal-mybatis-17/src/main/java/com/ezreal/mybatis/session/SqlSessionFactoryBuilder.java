package com.ezreal.mybatis.session;

import com.ezreal.mybatis.builder.xml.XmlConfigBuilder;
import com.ezreal.mybatis.session.defaults.DefaultSqlSessionFactory;

import java.io.Reader;

/**
 * @author Ezreal
 * @Date 2024/3/5
 */
public class SqlSessionFactoryBuilder {

    public SqlSessionFactory build(Reader reader) {
        XmlConfigBuilder xmlConfigBuilder = new XmlConfigBuilder(reader);
        return new DefaultSqlSessionFactory(xmlConfigBuilder.parse());
    }
}
