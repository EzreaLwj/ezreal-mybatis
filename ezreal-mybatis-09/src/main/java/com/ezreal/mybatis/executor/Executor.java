package com.ezreal.mybatis.executor;

import com.ezreal.mybatis.mapping.BoundSql;
import com.ezreal.mybatis.mapping.MappedStatement;
import com.ezreal.mybatis.session.ResultHandler;
import com.ezreal.mybatis.transaction.Transaction;

import java.sql.SQLException;
import java.util.List;

/**
 * 执行器
 * @author Ezreal
 * @Date 2024/3/8
 */
public interface Executor {

    ResultHandler NO_RESULT_HANDLER = null;

    <E> List<E> query(MappedStatement mappedStatement, Object parameter, ResultHandler resultHandler, BoundSql boundSql);

    Transaction getTransaction();

    void commit(boolean require) throws SQLException;

    void rollback(boolean require) throws SQLException;

    void close(boolean forceRollback) throws SQLException;
}
