package com.ezreal.mybatis.binding;

import com.ezreal.mybatis.session.SqlSession;

import java.lang.reflect.Proxy;

/**
 * @author Ezreal
 * @Date 2024/3/4
 */
public class MapperProxyFactory<T> {

    private final Class<T> mapperInterface;

    public MapperProxyFactory(Class<T> mapperInterface) {
        this.mapperInterface = mapperInterface;
    }

    /**
     * 创建代理对象
     *
     * @param sqlSession SQLSession
     * @return
     */
    public T newInstance(SqlSession sqlSession) {

        // 创建代理对象时需要传入sqlSession 因为sqlSession是增删改查的操作对象
        MapperProxy<T> mapperProxy = new MapperProxy<>(sqlSession, mapperInterface);
        return (T) Proxy.newProxyInstance(mapperInterface.getClassLoader(), new Class[]{mapperInterface}, mapperProxy);
    }
}
