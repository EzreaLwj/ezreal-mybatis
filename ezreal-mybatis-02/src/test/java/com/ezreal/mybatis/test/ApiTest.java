package com.ezreal.mybatis.test;

import com.ezreal.mybatis.binding.MapperProxyFactory;
import com.ezreal.mybatis.binding.MapperRegistry;
import com.ezreal.mybatis.session.SqlSession;
import com.ezreal.mybatis.session.defaults.DefaultSqlSessionFactory;
import com.ezreal.mybatis.test.dao.IUserDao;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Ezreal
 * @Date 2024/3/4
 */

public class ApiTest {

    @Test
    public void test_mapperProxy() {

        // 1.注册mapper
        MapperRegistry mapperRegistry = new MapperRegistry();
        mapperRegistry.addMapper("com.ezreal.mybatis.test.dao");

        // 2.获取SqlSession工厂
        DefaultSqlSessionFactory defaultSqlSessionFactory = new DefaultSqlSessionFactory(mapperRegistry);
        SqlSession sqlSession = defaultSqlSessionFactory.openSession();

        // 3.获取mapper
        IUserDao userDao = sqlSession.getMapper(IUserDao.class);

        // 4.测试结果
        String name = userDao.queryUserName("123456");
        System.out.println("结果为：" + name);
    }
}
