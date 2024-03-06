package com.ezreal.mybaits.test;

import com.alibaba.fastjson.JSON;
import com.ezreal.mybaits.test.dao.IUserDao;
import com.ezreal.mybaits.test.po.User;
import com.ezreal.mybatis.builder.xml.XmlConfigBuilder;
import com.ezreal.mybatis.io.Resources;
import com.ezreal.mybatis.session.Configuration;
import com.ezreal.mybatis.session.SqlSession;
import com.ezreal.mybatis.session.SqlSessionFactory;
import com.ezreal.mybatis.session.SqlSessionFactoryBuilder;
import com.ezreal.mybatis.session.defaults.DefaultSqlSession;
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

    private Logger logger = LoggerFactory.getLogger(ApiTest.class);

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
        User user = userDao.queryUserInfoById(1L);
        log.info("调用结果为：{}", user);
    }

    @Test
    public void test_selectOne() throws IOException {
        // 解析 XML
        Reader reader = Resources.getResourceAsReader("mybatis-config-datasource.xml");
        XmlConfigBuilder xmlConfigBuilder = new XmlConfigBuilder(reader);
        Configuration configuration = xmlConfigBuilder.parse();

        // 获取 DefaultSqlSession
        SqlSession sqlSession = new DefaultSqlSession(configuration);
        // 执行查询：默认是一个集合参数

        Object[] req = {1L};
        Object res = sqlSession.selectOne("com.ezreal.mybaits.test.dao.IUserDao.queryUserInfoById", req);
        logger.info("测试结果：{}", JSON.toJSONString(res));
    }

}
