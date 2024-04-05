package com.ezreal.mybatis.cache;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Ezreal
 * @Date 2024/4/5
 */
public class CacheKey implements Cloneable, Serializable {

    private static final long serialVersionUID = -3796671609418874673L;

    public static final CacheKey NULL_CACHE_KEY = new NullCacheKey();

    private static final int DEFAULT_MULTIRLTER = 37;

    private static final int DEFAULT_HASHCODE = 17;

    private int multiplier;

    private int hashcode;

    private long checksum;

    private int count;

    private List<Object> updateList;

    public CacheKey() {
        this.hashcode = DEFAULT_HASHCODE;
        this.multiplier = DEFAULT_MULTIRLTER;
        this.count = 0;
        this.updateList = new ArrayList<>();
    }

    public CacheKey(Object[] objects) {
        this();
        updateAll(objects);
    }

    public int getUpdateCount() {
        return updateList.size();
    }

    public void update(Object object) {
        // 计算Hash值，校验码
        int baseHashcode = object == null ? 1 : object.hashCode();
        count++;
        checksum += baseHashcode;
        baseHashcode *= count;

        hashcode = multiplier * hashcode + baseHashcode;
        updateList.add(object);
    }

    public void updateAll(Object[] objects) {
        for (Object o : objects) {
            update(o);
        }
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof CacheKey)) {
            return false;
        }

        final CacheKey cacheKey = (CacheKey) object;
        if (hashcode != cacheKey.hashcode) {
            return false;
        }
        if (checksum != cacheKey.checksum) {
            return false;
        }
        if (count != cacheKey.count) {
            return false;
        }

        // 如果hashcode重复，就调用updateList里面的对象的equal方法进行比较
        for (int i = 0; i < updateList.size(); i++) {
            Object thisObject = updateList.get(i);
            Object thatObject = cacheKey.updateList.get(i);
            if (thisObject == null) {
                if (thatObject == null) {
                    return false;
                }
            } else {
                if (!thisObject.equals(thatObject)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return hashcode;
    }

    @Override
    public String toString() {
        StringBuilder returnValue = new StringBuilder().append(hashcode).append(':').append(checksum);
        for (Object obj : updateList) {
            returnValue.append(':').append(obj);
        }
        return returnValue.toString();
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        CacheKey clonedCacheKey = (CacheKey) super.clone();
        clonedCacheKey.updateList = new ArrayList<>(updateList);
        return clonedCacheKey;
    }
}
