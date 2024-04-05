package com.ezreal.mybatis.executor.statement;

import com.ezreal.mybatis.executor.Executor;
import com.ezreal.mybatis.executor.parameter.ParameterHandler;
import com.ezreal.mybatis.executor.resultset.ResultSetHandler;
import com.ezreal.mybatis.mapping.BoundSql;
import com.ezreal.mybatis.mapping.MappedStatement;
import com.ezreal.mybatis.session.Configuration;
import com.ezreal.mybatis.session.ResultHandler;
import com.ezreal.mybatis.session.RowBounds;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author Ezreal
 * @Date 2024/3/8
 */
public abstract class BaseStatementHandler implements StatementHandler {

    protected final Configuration configuration;

    protected final Executor executor;

    protected final MappedStatement mappedStatement;

    protected final Object parameterObject;

    protected final BoundSql boundSql;

    protected final ResultSetHandler resultSetHandler;

    protected final ParameterHandler parameterHandler;

    protected final RowBounds rowBounds;

    public BaseStatementHandler(Executor executor, MappedStatement mappedStatement, Object parameterObject, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
        this.executor = executor;
        this.mappedStatement = mappedStatement;
        this.parameterObject = parameterObject;
        if (boundSql == null) {
            boundSql = mappedStatement.getSqlSource().getBoundSql(parameterObject);
        }
        this.boundSql = boundSql;
        this.rowBounds = rowBounds;
        this.configuration = mappedStatement.getConfiguration();
        this.resultSetHandler = configuration.newResultSetHandler(executor, mappedStatement, rowBounds, resultHandler, boundSql);
        this.parameterHandler = configuration.newParameterHandler(mappedStatement, parameterObject, boundSql);
    }

    @Override
    public Statement prepare(Connection connection) throws SQLException {
        Statement statement = null;

        try {
            statement = instantiateStatement(connection);
            statement.setQueryTimeout(350);
            statement.setFetchSize(10000);
            return statement;
        } catch (SQLException e) {
            throw new RuntimeException("Error preparing statement.  Cause: " + e, e);
        }

    }

    protected abstract Statement instantiateStatement(Connection connection) throws SQLException;

    @Override
    public BoundSql getBoundSql() {
        return boundSql;
    }
}
