package com.ezreal.mybatis.session.defaults;

import com.ezreal.mybatis.binding.MapperRegistry;
import com.ezreal.mybatis.session.SqlSession;
import com.ezreal.mybatis.session.SqlSessionFactory;

/**
 * sqlSession 工厂
 *
 * @author Ezreal
 * @Date 2024/3/4
 */
public class DefaultSqlSessionFactory implements SqlSessionFactory {

    private MapperRegistry mapperRegistry;

    public DefaultSqlSessionFactory(MapperRegistry mapperRegistry) {
        this.mapperRegistry = mapperRegistry;
    }

    @Override
    public SqlSession openSession() {
        return new DefaultSqlSession(mapperRegistry);
    }

}
