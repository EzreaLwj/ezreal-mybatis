package com.ezreal.mybatis.executor;

import com.ezreal.mybatis.executor.statement.PreparedStatementHandler;
import com.ezreal.mybatis.executor.statement.StatementHandler;
import com.ezreal.mybatis.mapping.BoundSql;
import com.ezreal.mybatis.mapping.Environment;
import com.ezreal.mybatis.mapping.MappedStatement;
import com.ezreal.mybatis.session.Configuration;
import com.ezreal.mybatis.session.ResultHandler;
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
    protected <E> List<E> doQuery(MappedStatement ms, Object parameter, ResultHandler resultHandler, BoundSql boundSql) {

        try {
            // 实例化连接
            Connection connection = transaction.getConnection();

            // 参数化创建语句处理器
            PreparedStatementHandler preparedStatementHandler = (PreparedStatementHandler) configuration.newStatementHandler(this, ms, parameter, resultHandler, boundSql);

            // 准备语句
            Statement statement = preparedStatementHandler.prepare(connection);

            // 参数传递
            preparedStatementHandler.parameterize(statement);

            //执行SQL
            return preparedStatementHandler.query(statement, resultHandler);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
