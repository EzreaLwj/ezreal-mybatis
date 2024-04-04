package com.ezreal.mybatis.transaction.jdbc;

import com.ezreal.mybatis.session.TransactionIsolationLevel;
import com.ezreal.mybatis.transaction.Transaction;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author Ezreal
 * @Date 2024/3/6
 */
public class JdbcTransaction implements Transaction {

    private Connection connection;
    private DataSource dataSource;
    private boolean autoCommit;
    protected TransactionIsolationLevel transactionIsolationLevel = TransactionIsolationLevel.READ_REPEATABLE;

    public JdbcTransaction(Connection connection) {
        this.connection = connection;
    }

    public JdbcTransaction(DataSource dataSource, boolean autoCommit, TransactionIsolationLevel transactionIsolationLevel) {
        this.dataSource = dataSource;
        this.autoCommit = autoCommit;
        this.transactionIsolationLevel = transactionIsolationLevel;
    }

    @Override
    public Connection getConnection() throws SQLException {
        if (connection != null) {
            return connection;
        }
        connection = dataSource.getConnection();
        connection.setAutoCommit(autoCommit);
        connection.setTransactionIsolation(transactionIsolationLevel.getLevel());
        return connection;
    }

    @Override
    public void commit() throws SQLException {
        if (connection != null && !connection.getAutoCommit()) {
            connection.commit();
        }
    }

    @Override
    public void rollback() throws SQLException {
        if (connection != null && !connection.getAutoCommit()) {
            connection.rollback();
        }
    }

    @Override
    public void close() throws SQLException {
        if (connection != null && !connection.getAutoCommit()) {
            connection.close();
        }
    }
}
