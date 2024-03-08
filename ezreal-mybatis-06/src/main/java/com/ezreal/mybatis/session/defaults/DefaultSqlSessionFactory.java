package com.ezreal.mybatis.session.defaults;

import com.ezreal.mybatis.session.Configuration;
import com.ezreal.mybatis.session.SqlSession;
import com.ezreal.mybatis.session.SqlSessionFactory;

/**
 * sqlSession 工厂
 *
 * @author Ezreal
 * @Date 2024/3/4
 */
public class DefaultSqlSessionFactory implements SqlSessionFactory {

    private Configuration configuration;

    public DefaultSqlSessionFactory(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public SqlSession openSession() {
        return new DefaultSqlSession(configuration);
    }

}
