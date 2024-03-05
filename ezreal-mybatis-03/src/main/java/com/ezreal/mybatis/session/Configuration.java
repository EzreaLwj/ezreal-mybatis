package com.ezreal.mybatis.session;

import com.ezreal.mybatis.binding.MapperRegistry;
import com.ezreal.mybatis.mapping.MappedStatement;

import java.util.HashMap;
import java.util.Map;

/**
 * Mybatis配置类
 * @author Ezreal
 * @Date 2024/3/5
 */
public class Configuration {

    /**
     * Mapper注册器
     */
    private MapperRegistry mapperRegistry = new MapperRegistry(this);

    /**
     * Mapper语句映射 key: 类路径+方法名称
     */
    private Map<String, MappedStatement> mappedStatements = new HashMap<>();

    public void addMapper(String packageName) {
        mapperRegistry.addMapper(packageName);
    }

    public <T> void addMapper(Class<T> type) {
        mapperRegistry.addMapper(type);
    }

    public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
        return mapperRegistry.getMapper(type, sqlSession);
    }

    public boolean hasMapper(Class<?> type) {
        return mapperRegistry.hasMapper(type);
    }

    public void addMappedStatement(MappedStatement mappedStatement) {
        mappedStatements.put(mappedStatement.getId(), mappedStatement);
    }

    public MappedStatement getMappedStatement(String id) {
        return mappedStatements.get(id);
    }

}
