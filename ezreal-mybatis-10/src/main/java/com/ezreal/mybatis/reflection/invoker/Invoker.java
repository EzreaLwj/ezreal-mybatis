package com.ezreal.mybatis.reflection.invoker;

/**
 * @author Ezreal
 * @Date 2024/3/10
 */
public interface Invoker {

    Object invoke(Object target, Object[] args) throws Exception;

    Class<?> getType();
}
