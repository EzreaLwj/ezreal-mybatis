package com.ezreal.mybaits.test;

import com.alibaba.fastjson.JSON;
import com.ezreal.mybaits.test.dao.IActivityDao;
import com.ezreal.mybaits.test.po.Activity;
import com.ezreal.mybatis.io.Resources;
import com.ezreal.mybatis.session.SqlSession;
import com.ezreal.mybatis.session.SqlSessionFactory;
import com.ezreal.mybatis.session.SqlSessionFactoryBuilder;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author Ezreal
 * @Date 2024/3/5
 */
public class ApiTest {

    private Logger logger = LoggerFactory.getLogger(ApiTest.class);

    private SqlSession sqlSession;

    @Before
    public void init() throws IOException {
        // 1. 从SqlSessionFactory中获取SqlSession
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(Resources.getResourceAsReader("mybatis-config-datasource.xml"));
        sqlSession = sqlSessionFactory.openSession();
    }

    @Test
    public void test_queryActivityById(){
        // 1. 获取映射器对象
        IActivityDao dao = sqlSession.getMapper(IActivityDao.class);
        // 2. 测试验证
        Activity res = dao.queryActivityById(100001L);
        logger.info("测试结果：{}", JSON.toJSONString(res));
    }

}
