package com.ezreal.mybatis.transaction;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author Ezreal
 * @Date 2024/3/6
 */
public interface Transaction {

    /**
     * 获取链接
     * @return
     * @throws SQLException
     */
    Connection getConnection() throws SQLException;

    /**
     * 提交事务
     * @throws SQLException
     */
    void commit() throws SQLException;

    /**
     * 事务回滚
     * @throws SQLException
     */
    void rollback() throws SQLException;

    /**
     * 关闭事务
     * @throws SQLException
     */
    void close() throws SQLException;

}
