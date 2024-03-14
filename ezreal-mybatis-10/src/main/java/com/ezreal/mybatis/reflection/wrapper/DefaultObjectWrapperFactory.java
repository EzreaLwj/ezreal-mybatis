package com.ezreal.mybatis.reflection.wrapper;

import com.ezreal.mybatis.reflection.MetaObject;

/**
 * 默认对象包装工厂
 * @author Ezreal
 * @Date 2024/3/11
 */
public class DefaultObjectWrapperFactory implements ObjectWrapperFactory{
    @Override
    public boolean hasWrapperFor(Object object) {
        return false;
    }

    @Override
    public ObjectWrapper getWrapperFor(MetaObject metaObject, Object object) {
        throw new RuntimeException("The DefaultObjectWrapperFactory should never be called to provide an ObjectWrapper.");
    }
}
