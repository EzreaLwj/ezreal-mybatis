package com.ezreal.mybatis.transaction.jdbc;

import com.ezreal.mybatis.session.TransactionIsolationLevel;
import com.ezreal.mybatis.transaction.Transaction;
import com.ezreal.mybatis.transaction.TransactionFactory;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * @author Ezreal
 * @Date 2024/3/6
 */
public class JdbcTransactionFactory implements TransactionFactory {

    @Override
    public Transaction newTransaction(Connection connection) {
        return new JdbcTransaction(connection);
    }

    @Override
    public Transaction newTransaction(DataSource dataSource, TransactionIsolationLevel level, boolean autoCommit) {
        return new JdbcTransaction(dataSource, autoCommit, level);
    }
}
