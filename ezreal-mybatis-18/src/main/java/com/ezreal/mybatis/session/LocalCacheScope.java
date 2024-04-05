package com.ezreal.mybatis.session;

/**
 * @author Ezreal
 * @Date 2024/4/5
 */
public enum LocalCacheScope {

    /**
     * SESSION 为默认值，支持使用一级缓存
     * STATEMENT 不支持使用一级缓存，这部分具体的判断使用可以参考源码
     */
    SESSION,
    STATEMENT
}
