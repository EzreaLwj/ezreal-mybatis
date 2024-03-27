package com.ezreal.mybatis.executor.statement;

import com.ezreal.mybatis.session.ResultHandler;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * @author Ezreal
 * @Date 2024/3/8
 */
public interface StatementHandler {

    /**
     * 准备语句
     *
     * @param connection 数据库连接
     * @return
     * @throws SQLException
     */
    Statement prepare(Connection connection) throws SQLException;

    /**
     * 向语句填充参数
     *
     * @param statement 语句
     * @throws SQLException
     */
    void parameterize(Statement statement) throws SQLException;

    /**
     * 执行查询
     *
     * @param statement     语句
     * @param resultHandler 结果处理器
     * @param <E>
     * @return
     * @throws SQLException
     */
    <E> List<E> query(Statement statement, ResultHandler resultHandler) throws SQLException;

    /**
     * 更新操作
     * @param stmt
     * @return
     */
    int update(Statement stmt) throws SQLException;
}
