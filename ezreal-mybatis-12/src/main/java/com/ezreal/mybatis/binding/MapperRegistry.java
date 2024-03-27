package com.ezreal.mybatis.binding;

import cn.hutool.core.lang.ClassScanner;
import com.ezreal.mybatis.builder.annotations.MapperAnnotationBuilder;
import com.ezreal.mybatis.session.Configuration;
import com.ezreal.mybatis.session.SqlSession;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Mapper注册器
 *
 * @author Ezreal
 * @Date 2024/3/4
 */
public class MapperRegistry {

    private Configuration configuration;

    private Map<Class<?>, MapperProxyFactory<?>> knownMappers = new HashMap<>();

    public MapperRegistry(Configuration configuration) {
        this.configuration = configuration;
    }

    /**
     * 获取Mapper对象
     *
     * @param type       mapper类型
     * @param sqlSession SQLSession
     * @param <T>        泛型对象
     * @return mapper对象
     */
    public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
        final MapperProxyFactory<T> mapperProxyFactory = (MapperProxyFactory<T>) knownMappers.get(type);
        if (mapperProxyFactory == null) {
            throw new RuntimeException("Type:" + type + " is not known to the MapperRegistry");
        }

        try {
            return mapperProxyFactory.newInstance(sqlSession);
        } catch (Exception e) {
            throw new RuntimeException("Error getting mapper instance. Cause: " + e, e);
        }
    }

    public <T> boolean hasMapper(Class<T> type) {
        return knownMappers.containsKey(type);
    }

    public <T> void addMapper(Class<T> type) {
        // 只有接口才能够添加
        if (type.isInterface()) {
            if (hasMapper(type)) {
                // 如果重复添加了，报错
                throw new RuntimeException("Type " + type + " is already known to the MapperRegistry.");
            }

            MapperProxyFactory<T> mapperProxyFactory = new MapperProxyFactory<T>(type);
            knownMappers.put(type, mapperProxyFactory);

            // 解析配置
            MapperAnnotationBuilder parser = new MapperAnnotationBuilder(configuration, type);
            parser.parse();

        }
    }

    public void addMapper(String packageName) {
        Set<Class<?>> classSet = ClassScanner.scanPackage(packageName);
        for (Class<?> aClass : classSet) {
            addMapper(aClass);
        }
    }
}
