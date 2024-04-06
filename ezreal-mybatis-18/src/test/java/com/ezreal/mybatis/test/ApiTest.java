package com.ezreal.mybatis.test;

import com.alibaba.fastjson.JSON;
import com.ezreal.mybatis.builder.xml.XmlConfigBuilder;
import com.ezreal.mybatis.executor.Executor;
import com.ezreal.mybatis.io.Resources;
import com.ezreal.mybatis.mapping.Environment;
import com.ezreal.mybatis.session.*;
import com.ezreal.mybatis.session.defaults.DefaultSqlSession;
import com.ezreal.mybatis.test.dao.IActivityDao;
import com.ezreal.mybatis.test.po.Activity;
import com.ezreal.mybatis.transaction.Transaction;
import com.ezreal.mybatis.transaction.TransactionFactory;
import org.junit.Before;
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

    private SqlSession sqlSession;

    @Before
    public void init() throws IOException {
        // 1. 从SqlSessionFactory中获取SqlSession
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(Resources.getResourceAsReader("mybatis-config-datasource.xml"));
        sqlSession = sqlSessionFactory.openSession();
    }

    @Test
    public void query_second_cache() throws IOException {
        // 1. 从SqlSessionFactory中获取SqlSession
        Reader reader = Resources.getResourceAsReader("mybatis-config-datasource.xml");
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);

        // 2. 请求对象
        Activity req = new Activity();
        req.setActivityId(100001L);

        // 3. 第一组：SqlSession
        // 3.1 开启 Session
        SqlSession sqlSession01 = sqlSessionFactory.openSession();
        // 3.2 获取映射器对象
        IActivityDao dao01 = sqlSession01.getMapper(IActivityDao.class);
        logger.info("测试结果01：{}", JSON.toJSONString(dao01.queryActivityById(req)));
        sqlSession01.close();

        // 4. 第一组：SqlSession
        // 4.1 开启 Session
        SqlSession sqlSession02 = sqlSessionFactory.openSession();
        // 4.2 获取映射器对象
        IActivityDao dao02 = sqlSession02.getMapper(IActivityDao.class);
        logger.info("测试结果02：{}", JSON.toJSONString(dao02.queryActivityById(req)));
        sqlSession02.close();
    }

    @Test
    public void query_cache() throws IOException {
        // 1. 从SqlSessionFactory中获取SqlSession
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(Resources.getResourceAsReader("mybatis-config-datasource.xml"));
        SqlSession sqlSession = sqlSessionFactory.openSession();

        // 2. 获取映射器对象
        IActivityDao dao = sqlSession.getMapper(IActivityDao.class);

        // 3. 测试验证
        Activity req = new Activity();
        req.setActivityId(100001L);
        logger.info("测试结果：{}", JSON.toJSONString(dao.queryActivityById(req)));
        // sqlSession.commit();
        // sqlSession.clearCache();
        // sqlSession.close();
        logger.info("测试结果：{}", JSON.toJSONString(dao.queryActivityById(req)));
    }

    @Test
    public void test_queryActivityById1() throws IOException {
        // 1. 从SqlSessionFactory中获取SqlSession
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(Resources.getResourceAsReader("mybatis-config-datasource.xml"));
        SqlSession sqlSession = sqlSessionFactory.openSession();

        // 2. 获取映射器对象
        IActivityDao dao = sqlSession.getMapper(IActivityDao.class);

        // 3. 测试验证
        Activity req = new Activity();
        req.setActivityId(100001L);
        Activity res = dao.queryActivityById(req);
        logger.info("测试结果：{}", JSON.toJSONString(res));
    }


    @Test
    public void test_insert_select() throws IOException {
        // 解析 XML
        Reader reader = Resources.getResourceAsReader("mybatis-config-datasource.xml");
        XmlConfigBuilder xmlConfigBuilder = new XmlConfigBuilder(reader);
        Configuration configuration = xmlConfigBuilder.parse();

        // 获取 DefaultSqlSession
        final Environment environment = configuration.getEnvironment();
        TransactionFactory transactionFactory = environment.getTransactionFactory();
        Transaction tx = transactionFactory.newTransaction(configuration.getEnvironment().getDataSource(), TransactionIsolationLevel.READ_COMMITTED, false);

        // 创建执行器
        final Executor executor = configuration.newExecutor(tx);
        SqlSession sqlSession = new DefaultSqlSession(configuration, executor);

        // 执行查询：默认是一个集合参数
        Activity activity = new Activity();
        activity.setActivityId(10006L);
        activity.setActivityName("测试活动");
        activity.setActivityDesc("测试数据插入");
        activity.setCreator("xiaofuge");
        int res = sqlSession.insert("com.ezreal.mybaits.test.dao.IActivityDao.insert", activity);

        Object obj = sqlSession.selectOne("com.ezreal.mybaits.test.dao.IActivityDao.insert!selectKey");
        logger.info("测试结果：count：{} idx：{}", res, JSON.toJSONString(obj));

        sqlSession.commit();
    }

}
