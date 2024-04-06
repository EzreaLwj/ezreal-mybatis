package com.ezreal.mybatis.executor;

import com.ezreal.mybatis.cache.CacheKey;
import com.ezreal.mybatis.mapping.BoundSql;
import com.ezreal.mybatis.mapping.MappedStatement;
import com.ezreal.mybatis.session.ResultHandler;
import com.ezreal.mybatis.session.RowBounds;
import com.ezreal.mybatis.transaction.Transaction;

import java.sql.SQLException;
import java.util.List;

/**
 * 执行器
 *
 * @author Ezreal
 * @Date 2024/3/8
 */
public interface Executor {

    ResultHandler NO_RESULT_HANDLER = null;

    int update(MappedStatement mappedStatement, Object parameter) throws SQLException;

    <E> List<E> query(MappedStatement mappedStatement, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, CacheKey cacheKey, BoundSql boundSql);

    <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler) throws SQLException;

    Transaction getTransaction();

    void commit(boolean require) throws SQLException;

    void rollback(boolean require) throws SQLException;

    void close(boolean forceRollback);

    // 清理Session缓存
    void clearLocalCache();

    // 创建缓存 Key
    CacheKey createCacheKey(MappedStatement ms, Object parameterObject, RowBounds rowBounds, BoundSql boundSql);

    void setExecutorWrapper(Executor executor);
}
