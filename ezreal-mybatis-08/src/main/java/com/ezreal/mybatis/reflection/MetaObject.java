package com.ezreal.mybatis.reflection;

import com.ezreal.mybatis.reflection.factory.ObjectFactory;
import com.ezreal.mybatis.reflection.property.PropertyTokenizer;
import com.ezreal.mybatis.reflection.wrapper.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author Ezreal
 * @Date 2024/3/11
 */
public class MetaObject {

    // 原对象
    private Object originalObject;

    // 对象包装器
    private ObjectWrapper objectWrapper;

    // 对象工厂
    private ObjectFactory objectFactory;

    // 对象包装工厂
    private ObjectWrapperFactory objectWrapperFactory;

    public MetaObject(Object object, ObjectFactory objectFactory, ObjectWrapperFactory objectWrapperFactory) {
        this.originalObject = object;
        this.objectFactory = objectFactory;
        this.objectWrapperFactory = objectWrapperFactory;

        if (object instanceof ObjectWrapper) {
            // 如果对象本身已经是ObjectWrapper类型，则直接赋值给objectwrapper
            this.objectWrapper = (ObjectWrapper) object;
        } else if (objectWrapperFactory.hasWrapperFor(object)) {
            // 如果有包装器，则调用objectWrapperFactory.getWrapperFor
            this.objectWrapper = objectWrapperFactory.getWrapperFor(this, object);
        } else if(object instanceof Map) {
            // 如果是map类型，就返回MapWrapper
            this.objectWrapper = new MapWrapper(this, (Map) object);
        } else if (object instanceof Collection) {
            // 如果合适Collection类型，就返回CollectionWrapper
            this.objectWrapper = new CollectionWrapper(this, (Collection) object);
        } else {
            // 除此之外，返回BeanWrapper
            this.objectWrapper = new BeanWrapper(this, object);
        }
    }

    public static MetaObject forObject(Object object, ObjectFactory objectFactory, ObjectWrapperFactory objectWrapperFactory) {
        if (object == null) {
            // 处理一下Null，将Null包装起来
            return SystemMetaObject.NULL_META_OBJECT;
        } else {
            return new MetaObject(object, objectFactory, objectWrapperFactory);
        }
    }

    public Object getOriginalObject() {
        return originalObject;
    }

    public void setOriginalObject(Object originalObject) {
        this.originalObject = originalObject;
    }

    public ObjectWrapper getObjectWrapper() {
        return objectWrapper;
    }

    public void setObjectWrapper(ObjectWrapper objectWrapper) {
        this.objectWrapper = objectWrapper;
    }

    public ObjectFactory getObjectFactory() {
        return objectFactory;
    }

    public void setObjectFactory(ObjectFactory objectFactory) {
        this.objectFactory = objectFactory;
    }

    public ObjectWrapperFactory getObjectWrapperFactory() {
        return objectWrapperFactory;
    }

    public void setObjectWrapperFactory(ObjectWrapperFactory objectWrapperFactory) {
        this.objectWrapperFactory = objectWrapperFactory;
    }

    /* --------以下方法都是委派给 ObjectWrapper------ */
    // 查找属性
    public String findProperty(String propName, boolean useCamelCaseMapping) {
        return objectWrapper.findProperty(propName, useCamelCaseMapping);
    }

    // 取得setter的名字列表
    public String[] getGetterNames() {
        return objectWrapper.getSetterNames();
    }

    // 取得setter的名字列表
    public String[] getSetterNames() {
        return objectWrapper.getSetterNames();
    }

    // 取得setter的类型列表
    public Class<?> getSetterType(String name) {
        return objectWrapper.getSetterType(name);
    }

    // 取得getter的类型列表
    public Class<?> getGetterType(String name) {
        return objectWrapper.getGetterType(name);
    }

    //是否有指定的setter
    public boolean hasSetter(String name) {
        return objectWrapper.hasSetter(name);
    }

    // 是否有指定的getter
    public boolean hasGetter(String name) {
        return objectWrapper.hasGetter(name);
    }

    public Object getValue(String name) {
        PropertyTokenizer prop = new PropertyTokenizer(name);
        if (prop.hasNext()) {
            MetaObject metaValue = metaObjectForProperty(prop.getIndexedName());
            if (metaValue == SystemMetaObject.NULL_META_OBJECT) {
                // 如果上层就是null了，那就结束，返回null
                return null;
            } else {
                // 否则继续看下一层，递归调用getValue
                return metaValue.getValue(prop.getChildren());
            }
        } else {
            return objectWrapper.get(prop);
        }
    }

    // 为属性生成元对象
    public MetaObject metaObjectForProperty(String indexedName) {
        // 实际上是递归调用
        Object value = getValue(indexedName);
        return MetaObject.forObject(value, objectFactory, objectWrapperFactory);
    }

    public void setValue(String name, Object value) {
        PropertyTokenizer prop = new PropertyTokenizer(name);
        if (prop.hasNext()) {
            MetaObject metaValue = metaObjectForProperty(prop.getIndexedName());
            if (metaValue == SystemMetaObject.NULL_META_OBJECT) {
                if (value == null && prop.getChildren() != null) {
                    // 如果上层就是 null 了，还得看有没有儿子，没有那就结束
                    return;
                } else{
                    metaValue = objectWrapper.instantiatePropertyValue(name, prop, objectFactory);
                }
            }
            metaValue.setValue(prop.getChildren(), value);
        } else {
            objectWrapper.set(prop, value);
        }
    }


    // 是否是集合
    public boolean isCollection() {
        return objectWrapper.isCollection();
    }

    // 添加属性
    public void add(Object element) {
        objectWrapper.add(element);
    }

    // 添加属性
    public <E> void addAll(List<E> list) {
        objectWrapper.addAll(list);
    }

}
