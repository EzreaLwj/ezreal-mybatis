package com.ezreal.mybaits.test;

import com.ezreal.mybaits.test.dao.IUserDao;
import com.ezreal.mybatis.io.Resources;
import com.ezreal.mybatis.session.SqlSession;
import com.ezreal.mybatis.session.SqlSessionFactory;
import com.ezreal.mybatis.session.SqlSessionFactoryBuilder;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;

/**
 * @author Ezreal
 * @Date 2024/3/5
 */
public class ApiTest {

    private final Logger log = LoggerFactory.getLogger(ApiTest.class);

    @Test
    public void test_sqlSessionFactory() throws IOException {
        // 1.构建SqlSession工厂
        Reader reader = Resources.getResourceAsReader("mybatis-config-datasource.xml");
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);

        // 2.开启SqlSession
        SqlSession sqlSession = sqlSessionFactory.openSession();
        IUserDao userDao = sqlSession.getMapper(IUserDao.class);

        // 3.调用方法
        String result = userDao.queryUserInfoById("123456");
        log.info("调用结果为：{}", result);
    }
}
