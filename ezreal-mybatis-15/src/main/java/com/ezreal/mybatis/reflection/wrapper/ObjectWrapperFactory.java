package com.ezreal.mybatis.reflection.wrapper;

import com.ezreal.mybatis.reflection.MetaObject;

/**
 * 对象包装工厂
 * @author Ezreal
 * @Date 2024/3/11
 */
public interface ObjectWrapperFactory {

    /**
     * 判断有没有包装器
     * @param object
     * @return
     */
    boolean hasWrapperFor(Object object);

    /**
     * 得到包装器
     * @param metaObject
     * @param object
     * @return
     */
    ObjectWrapper getWrapperFor(MetaObject metaObject, Object object);
}
