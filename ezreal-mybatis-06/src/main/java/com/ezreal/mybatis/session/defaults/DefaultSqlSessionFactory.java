package com.ezreal.mybatis.session.defaults;

import com.ezreal.mybatis.executor.Executor;
import com.ezreal.mybatis.mapping.Environment;
import com.ezreal.mybatis.session.Configuration;
import com.ezreal.mybatis.session.SqlSession;
import com.ezreal.mybatis.session.SqlSessionFactory;
import com.ezreal.mybatis.session.TransactionIsolationLevel;
import com.ezreal.mybatis.transaction.Transaction;
import com.ezreal.mybatis.transaction.TransactionFactory;

import java.sql.SQLException;

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
        Transaction tx = null;
        try {
            Environment environment = configuration.getEnvironment();
            TransactionFactory transactionFactory = environment.getTransactionFactory();

            // 创建事务
            tx = transactionFactory.newTransaction(environment.getDataSource(), TransactionIsolationLevel.READ_COMMITTED, false);

            // 创建执行器
            final Executor executor = configuration.newExecutor(tx);
            return new DefaultSqlSession(configuration, executor);
        } catch (Exception e) {
            try {
                assert tx != null;
                tx.close();
            } catch (SQLException ignore) {
            }
            throw new RuntimeException("Error opening session.  Cause: " + e);
        }
    }

}
