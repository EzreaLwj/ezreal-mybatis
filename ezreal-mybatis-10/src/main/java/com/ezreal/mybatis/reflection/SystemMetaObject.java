package com.ezreal.mybatis.reflection;

import com.ezreal.mybatis.reflection.factory.DefaultObjectFactory;
import com.ezreal.mybatis.reflection.factory.ObjectFactory;
import com.ezreal.mybatis.reflection.wrapper.DefaultObjectWrapperFactory;
import com.ezreal.mybatis.reflection.wrapper.ObjectWrapperFactory;

/**
 * @author Ezreal
 * @Date 2024/3/11
 */
public class SystemMetaObject {

    public static final ObjectFactory DEFAULT_OBJECT_FACTORY = new DefaultObjectFactory();

    public static final ObjectWrapperFactory DEFAULT_OBJECT_WRAPPER_FACTORY = new DefaultObjectWrapperFactory();

    public static final MetaObject NULL_META_OBJECT = MetaObject.forObject(NullObject.class, DEFAULT_OBJECT_FACTORY, DEFAULT_OBJECT_WRAPPER_FACTORY);

    public SystemMetaObject() {
    }

    /**
     * 空对象
     */
    private static class NullObject {

    }

    public static MetaObject forObject(Object object) {
        return MetaObject.forObject(object, DEFAULT_OBJECT_FACTORY, DEFAULT_OBJECT_WRAPPER_FACTORY);
    }
}
