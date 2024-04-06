package com.ezreal.mybatis.executor;

import com.ezreal.mybatis.cache.CacheKey;
import com.ezreal.mybatis.cache.impl.PerpetualCache;
import com.ezreal.mybatis.mapping.BoundSql;
import com.ezreal.mybatis.mapping.MappedStatement;
import com.ezreal.mybatis.mapping.ParameterMapping;
import com.ezreal.mybatis.reflection.MetaObject;
import com.ezreal.mybatis.session.Configuration;
import com.ezreal.mybatis.session.LocalCacheScope;
import com.ezreal.mybatis.session.ResultHandler;
import com.ezreal.mybatis.session.RowBounds;
import com.ezreal.mybatis.transaction.Transaction;
import com.ezreal.mybatis.type.TypeHandlerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * @author Ezreal
 * @Date 2024/3/8
 */
public abstract class BaseExecutor implements Executor {

    private Logger logger = LoggerFactory.getLogger(BaseExecutor.class);

    protected Configuration configuration;

    protected Executor wrapper;

    protected Transaction transaction;

    protected boolean closed;

    // 本地缓存
    protected PerpetualCache localCache;

    // 查询堆栈
    protected int queryStack = 0;

    public BaseExecutor(Configuration configuration, Transaction transaction) {
        this.configuration = configuration;
        this.transaction = transaction;
        this.wrapper = this;
        this.localCache = new PerpetualCache("LocalCache");
    }

    protected abstract <E> List<E> doQuery(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql);

    @Override
    public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, CacheKey key, BoundSql boundSql) {
        if (closed) {
            throw new RuntimeException("Executor was closed");
        }
        // 清理局部缓存，查询堆栈为0则清理。queryStack避免递归调用清理
        if (queryStack == 0 && ms.isFlushCacheRequired()) {
            clearLocalCache();
        }
        List<E> list;
        try {
            queryStack++;
            // 根据cacheKey从localCache中查询数据
            list = resultHandler == null ? (List<E>) localCache.getObject(key) : null;
            if (list == null) {
                list = queryFromDatabase(ms, parameter, rowBounds, resultHandler, key, boundSql);
            }
        } finally {
            queryStack--;
        }

        if (queryStack == 0) {
            if (configuration.getLocalCacheScope() == LocalCacheScope.STATEMENT) {
                clearLocalCache();
            }
        }

        return list;
    }

    private <E> List<E> queryFromDatabase(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, CacheKey key, BoundSql boundSql) {
        List<E> list;
        localCache.putObject(key, ExecutionPlaceholder.EXECUTION_PLACEHOLDER);

        try {
            list = doQuery(ms, parameter, rowBounds, resultHandler, boundSql);
        } finally {
            localCache.removeObject(key);
        }

        localCache.putObject(key, list);
        return list;
    }

    @Override
    public <E> List<E> query(MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler) throws SQLException {
        //1.获取绑定SQL
        BoundSql boundSql = ms.getBoundSql(parameter);
        //2.创建缓存key
        CacheKey key = createCacheKey(ms, parameter, rowBounds, boundSql);
        return query(ms, parameter, rowBounds, resultHandler, key, boundSql);
    }

    @Override
    public void clearLocalCache() {
        if (!closed) {
            localCache.clear();
        }
    }

    /**
     * 缓存Key的创建，需要依赖于；mappedStatementId + offset + limit + SQL + queryParams + environment 信息构建出一个哈希值，
     * 所以这里把这些对应的信息分别传递给 cacheKey#update 方法。
     */
    @Override
    public CacheKey createCacheKey(MappedStatement ms, Object parameterObject, RowBounds rowBounds, BoundSql boundSql) {
        if (closed) {
            throw new RuntimeException("Executor was closed");
        }
        CacheKey cacheKey = new CacheKey();
        cacheKey.update(ms.getId());
        cacheKey.update(rowBounds.getOffset());
        cacheKey.update(rowBounds.getLimit());
        cacheKey.update(boundSql.getSql());
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        TypeHandlerRegistry typeHandlerRegistry = ms.getConfiguration().getTypeHandlerRegistry();
        for (ParameterMapping parameterMapping : parameterMappings) {
            Object value;
            String propertyName = parameterMapping.getProperty();
            if (boundSql.hasAdditionalParameter(propertyName)) {
                value = boundSql.getAdditionalParameter(propertyName);
            } else if (parameterObject == null) {
                value = null;
            } else if (typeHandlerRegistry.hasTypeHandler(parameterObject.getClass())) {
                value = parameterObject;
            } else {
                MetaObject metaObject = configuration.newMetaObject(parameterObject);
                value = metaObject.getValue(propertyName);
            }
            cacheKey.update(value);
        }
        if (configuration.getEnvironment() != null) {
            cacheKey.update(configuration.getEnvironment().getId());
        }
        return cacheKey;
    }

    @Override
    public int update(MappedStatement mappedStatement, Object parameter) throws SQLException {
        if (closed) {
            throw new RuntimeException("Executor was closed.");
        }
        clearLocalCache();
        return doUpdate(mappedStatement, parameter);
    }

    protected abstract int doUpdate(MappedStatement mappedStatement, Object parameter) throws SQLException;

    @Override
    public Transaction getTransaction() {
        if (closed) {
            throw new RuntimeException("Executor was closed");
        }
        return transaction;
    }

    @Override
    public void commit(boolean require) throws SQLException {
        if (closed) {
            throw new RuntimeException("Executor was closed");
        }
        clearLocalCache();
        if (require) {
            transaction.commit();
        }
    }

    @Override
    public void rollback(boolean require) throws SQLException {
        if (!closed) {
            try {
                clearLocalCache();
            } finally {
                if (require) {
                    transaction.rollback();
                }
            }
        }
    }

    @Override
    public void close(boolean forceRollback) {
        try {
            try {
                rollback(forceRollback);
            } finally {
                transaction.close();
            }
        } catch (SQLException e) {
            logger.warn("Unexpected exception on closing transaction.  Cause: " + e);
        } finally {
            transaction = null;
            closed = true;
        }

    }

    @Override
    public void setExecutorWrapper(Executor executor) {
        this.wrapper = wrapper;
    }

    protected void closeStatement(Statement statement) {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException ignore) {
            }
        }
    }
}
