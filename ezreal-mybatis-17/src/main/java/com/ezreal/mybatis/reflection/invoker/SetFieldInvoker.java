package com.ezreal.mybatis.reflection.invoker;

import java.lang.reflect.Field;

/**
 * set字段反射调用
 *
 * @author Ezreal
 * @Date 2024/3/10
 */
public class SetFieldInvoker implements Invoker {

    private Field field;

    public SetFieldInvoker(Field field) {
        this.field = field;
    }

    @Override
    public Object invoke(Object target, Object[] args) throws Exception {
        field.set(target, args[0]);
        return null;
    }

    @Override
    public Class<?> getType() {
        return field.getType();
    }
}
