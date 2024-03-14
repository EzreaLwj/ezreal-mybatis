package com.ezreal.mybatis.reflection.wrapper;

import com.ezreal.mybatis.reflection.MetaObject;
import com.ezreal.mybatis.reflection.factory.ObjectFactory;
import com.ezreal.mybatis.reflection.property.PropertyTokenizer;

import java.util.List;

/**
 * 对象包装器接口
 * @author Ezreal
 * @Date 2024/3/11
 */
public interface ObjectWrapper {
    Object get(PropertyTokenizer prop);

    void set(PropertyTokenizer prop, Object value);

    String findProperty(String name, boolean useCamelCaseMapping);

    String[] getGetterNames();

    String[] getSetterNames();

    Class<?> getSetterType(String name);

    Class<?> getGetterType(String name);

    boolean hasGetter(String name);

    boolean hasSetter(String name);

    // 实例化属性
    MetaObject instantiatePropertyValue(String name, PropertyTokenizer prop, ObjectFactory objectFactory);

    // 是否是集合
    boolean isCollection();

    void add(Object element);

    // 添加属性
    <E> void addAll(List<E> element);
}
