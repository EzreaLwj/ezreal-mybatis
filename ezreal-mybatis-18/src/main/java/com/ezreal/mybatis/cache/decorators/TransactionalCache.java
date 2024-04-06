package com.ezreal.mybatis.cache.decorators;

import com.ezreal.mybatis.cache.Cache;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Ezreal
 * @Date 2024/4/6
 */
public class TransactionalCache implements Cache {

    private Cache delegate;
    // commit 时要不要清缓存
    private boolean clearOnCommit;
    // commit 时要添加的元素
    private Map<Object, Object> entriesToAddOnCommit;
    private Set<Object> entriesMissedInCache;

    public TransactionalCache(Cache delegate) {
        this.delegate = delegate;
        this.clearOnCommit = false;
        this.entriesMissedInCache = new HashSet<>();
        this.entriesToAddOnCommit = new HashMap<>();
    }

    @Override
    public String getId() {
        return delegate.getId();
    }

    @Override
    public void putObject(Object key, Object value) {
        entriesToAddOnCommit.put(key, value);
    }

    @Override
    public Object getObject(Object key) {
        // key：CacheKey 拼装后的哈希码
        Object object = delegate.getObject(key);
        if (object == null) {
            entriesMissedInCache.add(key);
        }
        return clearOnCommit ? null : object;
    }

    @Override
    public Object removeObject(Object key) {
        return null;
    }

    @Override
    public void clear() {
        clearOnCommit = true;
        entriesToAddOnCommit.clear();
    }

    @Override
    public int getSize() {
        return delegate.getSize();
    }

    public void commit() {
        if (clearOnCommit) {
            delegate.clear();
        }
        flushPendingEntries();
        reset();
    }

    private void reset() {
        clearOnCommit = false;
        entriesToAddOnCommit.clear();
        entriesMissedInCache.clear();
    }

    /**
     * 刷新数据到 MappedStatement#Cache 中，也就是把数据填充到 Mapper XML 级别下。
     * flushPendingEntries 方法把事务缓存下的数据，填充到 FifoCache 中。
     */
    private void flushPendingEntries() {
        for (Map.Entry<Object, Object> entry : entriesToAddOnCommit.entrySet()) {
            delegate.putObject(entry.getKey(), entry.getValue());
        }
        for (Object entry : entriesMissedInCache) {
            if (!entriesToAddOnCommit.containsKey(entry)) {
                delegate.putObject(entry, null);
            }
        }
    }

    private void unlockMissedEntries() {
        for (Object entry : entriesMissedInCache) {
            delegate.putObject(entry, null);
        }
    }

    public void rollback() {
        unlockMissedEntries();
        reset();
    }

}
