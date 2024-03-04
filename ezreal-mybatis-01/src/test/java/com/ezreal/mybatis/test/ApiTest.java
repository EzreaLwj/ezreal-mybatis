package com.ezreal.mybatis.test;

import com.ezreal.mybatis.binding.MapperProxyFactory;
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

        // 1. 创建代理工厂
        MapperProxyFactory<IUserDao> mapperProxyFactory = new MapperProxyFactory<>(IUserDao.class);

        // 2. 创建代理类
        Map<String, String> sqlSession = new HashMap<>();
        sqlSession.put("com.ezreal.mybatis.test.dao.IUserDao.queryUserName", "模拟执行 Mapper.xml 中 SQL 语句的操作：查询用户姓名");
        sqlSession.put("com.ezreal.mybatis.test.dao.IUserDao.queryUserAge", "模拟执行 Mapper.xml 中 SQL 语句的操作：查询用户年龄");
        IUserDao userDao = mapperProxyFactory.newInstance(sqlSession);

        // 3. 查询用户信息
        String name = userDao.queryUserName("123456");

        System.out.println("name: " + name);
    }
}
