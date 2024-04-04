package com.ezreal.mybatis.builder;

import com.ezreal.mybatis.session.Configuration;
import com.ezreal.mybatis.type.TypeAliasRegistry;
import com.ezreal.mybatis.type.TypeHandlerRegistry;

/**
 * 构建器的基类，建造者模式
 * @author Ezreal
 * @Date 2024/3/5
 */
public class BaseBuilder {

    protected Configuration configuration;

    protected final TypeAliasRegistry typeAliasRegistry;

    protected final TypeHandlerRegistry typeHandlerRegistry;

    public BaseBuilder(Configuration configuration) {
        this.configuration = configuration;
        this.typeAliasRegistry = configuration.getTypeAliasRegistry();
        this.typeHandlerRegistry = configuration.getTypeHandlerRegistry();
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    protected Class<?> resolveAlias(String alias) {
        return typeAliasRegistry.resolveAlias(alias);
    }

    // 根据别名解析 Class 类型别名注册/事务管理器别名
    protected Class<?> resolveClass(String alias) {
        if (alias == null) {
            return null;
        }
        try {
            return resolveAlias(alias);
        } catch (Exception e) {
            throw new RuntimeException("Error resolving class. Cause: " + e, e);
        }
    }
}
