package com.ezreal.mybatis.executor;

import com.ezreal.mybatis.executor.statement.StatementHandler;
import com.ezreal.mybatis.mapping.BoundSql;
import com.ezreal.mybatis.mapping.MappedStatement;
import com.ezreal.mybatis.session.Configuration;
import com.ezreal.mybatis.session.ResultHandler;
import com.ezreal.mybatis.session.RowBounds;
import com.ezreal.mybatis.transaction.Transaction;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * 执行器实现类
 *
 * @author Ezreal
 * @Date 2024/3/8
 */
public class SimpleExecutor extends BaseExecutor {

    public SimpleExecutor(Configuration configuration, Transaction transaction) {
        super(configuration, transaction);
    }

    @Override
    protected <E> List<E> doQuery(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {

        Statement statement = null;
        try {
            // 实例化连接
            Connection connection = transaction.getConnection();

            // 参数化创建语句处理器
            StatementHandler preparedStatementHandler = configuration.newStatementHandler(this, ms, parameter, rowBounds, resultHandler, boundSql);

            // 准备语句
            statement = preparedStatementHandler.prepare(connection);

            // 参数传递
            preparedStatementHandler.parameterize(statement);

            //执行SQL
            return preparedStatementHandler.query(statement, resultHandler);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            closeStatement(statement);
        }
    }

    @Override
    protected int doUpdate(MappedStatement mappedStatement, Object parameter) throws SQLException {

        Statement stmt = null;
        try {
            Configuration configuration = mappedStatement.getConfiguration();
            StatementHandler statementHandler = configuration.newStatementHandler(this, mappedStatement, parameter, RowBounds.DEFAULT, null, null);
            stmt = prepareStatement(statementHandler);
            // 执行update操作
            return statementHandler.update(stmt);
        } finally {
            closeStatement(stmt);
        }

    }

    private Statement prepareStatement(StatementHandler statementHandler) throws SQLException {
        Connection connection = transaction.getConnection();
        Statement statement = statementHandler.prepare(connection);
        statementHandler.parameterize(statement);
        return statement;
    }
}
