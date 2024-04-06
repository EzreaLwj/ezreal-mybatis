package com.ezreal.mybatis.cache;

import com.ezreal.mybatis.cache.decorators.TransactionalCache;

import java.util.HashMap;
import java.util.Map;

/**
 * 事务缓存，管理器
 * @author Ezreal
 * @Date 2024/4/6
 */
public class TransactionalCacheManager {

    private Map<Cache, TransactionalCache> transactionalCaches = new HashMap<>();

    public void clear(Cache cache) {
        getTransactionCache(cache).clear();
    }

    /**
     * 得到某个TransactionalCache的值
     */
    public Object getObject(Cache cache, CacheKey key) {
        return getTransactionCache(cache).getObject(key);
    }

    public void putObject(Cache cache, CacheKey key, Object value) {
        getTransactionCache(cache).putObject(key, value);
    }

    /**
     * 提交时全部提交
     */
    public void commit() {
        for (TransactionalCache txCache : transactionalCaches.values()) {
            txCache.commit();
        }
    }

    /**
     * 回滚时全部回滚
     */
    public void rollback() {
        for (TransactionalCache txCache : transactionalCaches.values()) {
            txCache.rollback();
        }
    }

    private TransactionalCache getTransactionCache(Cache cache) {
        TransactionalCache txCache = transactionalCaches.get(cache);
        if (txCache == null) {
            txCache = new TransactionalCache(cache);
            transactionalCaches.put(cache, txCache);
        }
        return txCache;
    }
}
