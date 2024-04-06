package com.ezreal.mybatis.executor;

import com.alibaba.fastjson.JSON;
import com.ezreal.mybatis.cache.Cache;
import com.ezreal.mybatis.cache.CacheKey;
import com.ezreal.mybatis.cache.TransactionalCacheManager;
import com.ezreal.mybatis.mapping.BoundSql;
import com.ezreal.mybatis.mapping.MappedStatement;
import com.ezreal.mybatis.session.ResultHandler;
import com.ezreal.mybatis.session.RowBounds;
import com.ezreal.mybatis.transaction.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.sql.SQLException;
import java.util.List;

/**
 * 二级缓存执行器是一个装饰器模式，将 SimpleExecutor 做一层包装，提供缓存的能力
 * @author Ezreal
 * @Date 2024/4/6
 */
public class CachingExecutor implements Executor {

    private Logger logger = LoggerFactory.getLogger(CachingExecutor.class);

    private Executor delegate;

    private TransactionalCacheManager tcm = new TransactionalCacheManager();

    public CachingExecutor(Executor delegate) {
        this.delegate = delegate;
        delegate.setExecutorWrapper(this);
    }

    @Override
    public int update(MappedStatement ms, Object parameter) throws SQLException {
        return delegate.update(ms, parameter);
    }

    @Override
    public <E> List<E> query(MappedStatement mappedStatement, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, CacheKey cacheKey, BoundSql boundSql) {
        // 从对应的MapperStatement中获取二级cache
        Cache cache = mappedStatement.getCache();
        if (cache != null) {
            flushCacheIfRequired(mappedStatement);

            if (mappedStatement.isUseCache() && resultHandler == null) {
                //尝试获取二级缓存
                List<E> list = (List<E>) tcm.getObject(cache, cacheKey);
                if (list == null) {
                    // cache：缓存队列实现类，FIFO
                    // key：哈希值 [mappedStatementId + offset + limit + SQL + queryParams + environment]
                    // list：查询的数据
                    list = delegate.query(mappedStatement, parameter, rowBounds, resultHandler, cacheKey, boundSql);
                    // 存入二级缓存
                    tcm.putObject(cache, cacheKey, list);
                }
                // 打印调试日志，记录二级缓存获取数据
                if (logger.isDebugEnabled() && cache.getSize() > 0) {
                    logger.debug("二级缓存：{}", JSON.toJSONString(list));
                }
                return list;
            }
        }
        //通过装饰器模式调用基类的查询方法
        return delegate.query(mappedStatement, parameter, rowBounds, resultHandler, cacheKey, boundSql);
    }

    @Override
    public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler) throws SQLException {
        // 1.获取绑定SQL
        BoundSql boundSql = ms.getBoundSql(parameter);
        // 2.创建缓存key
        CacheKey key = createCacheKey(ms, parameter, rowBounds, boundSql);
        return query(ms, parameter, rowBounds, resultHandler, key, boundSql);
    }

    @Override
    public Transaction getTransaction() {
        return delegate.getTransaction();
    }

    @Override
    public void commit(boolean require) throws SQLException {
        delegate.commit(require);
        tcm.commit();
    }

    @Override
    public void rollback(boolean require) throws SQLException {
        try {
            delegate.rollback(require);
        } finally {
            if (require) {
                tcm.rollback();
            }
        }
    }

    @Override
    public void close(boolean forceRollback) {
        try {
            if (forceRollback) {
                tcm.rollback();
            } else {
                tcm.commit();
            }
        } finally {
            delegate.close(forceRollback);
        }
    }

    @Override
    public void clearLocalCache() {
        delegate.clearLocalCache();
    }

    @Override
    public CacheKey createCacheKey(MappedStatement ms, Object parameterObject, RowBounds rowBounds, BoundSql boundSql) {
        return delegate.createCacheKey(ms, parameterObject, rowBounds, boundSql);
    }

    @Override
    public void setExecutorWrapper(Executor executor) {
        throw new UnsupportedOperationException("This method should not be called");
    }

    private void flushCacheIfRequired(MappedStatement ms) {
        Cache cache = ms.getCache();
        if (cache != null && ms.isFlushCacheRequired()) {
            tcm.clear(cache);
        }
    }
}
