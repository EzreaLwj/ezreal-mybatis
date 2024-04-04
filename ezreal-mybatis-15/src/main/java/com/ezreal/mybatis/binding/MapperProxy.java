package com.ezreal.mybatis.binding;

import com.ezreal.mybatis.session.SqlSession;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author Ezreal
 * @Date 2024/3/4
 */
public class MapperProxy<T> implements InvocationHandler, Serializable {

    private static final long serialVersionUID = 4999443646458082996L;

    private SqlSession sqlSession;

    private final Class<T> mapperInterface;

    private Map<Method, MapperMethod> cacheMethod;

    public MapperProxy(SqlSession sqlSession, Class<T> mapperInterface, Map<Method, MapperMethod> cacheMethod) {
        this.sqlSession = sqlSession;
        this.mapperInterface = mapperInterface;
        this.cacheMethod = cacheMethod;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (Object.class.equals(method.getDeclaringClass())) {
            return method.invoke(proxy, args);
        } else {
            MapperMethod mapperMethod = cacheMethod.get(method);
            if (mapperMethod == null) {
                mapperMethod = new MapperMethod(sqlSession.getConfiguration(), mapperInterface, method);
                cacheMethod.put(method, mapperMethod);
            }
            return mapperMethod.execute(sqlSession, args);
        }
    }
}
