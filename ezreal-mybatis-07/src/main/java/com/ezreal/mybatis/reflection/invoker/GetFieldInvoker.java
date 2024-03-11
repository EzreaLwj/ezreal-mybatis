package com.ezreal.mybatis.reflection.invoker;

import java.lang.reflect.Field;

/**
 * get字段反射调用
 *
 * @author Ezreal
 * @Date 2024/3/10
 */
public class GetFieldInvoker implements Invoker {

    private Field field;

    public GetFieldInvoker(Field field) {
        this.field = field;
    }

    @Override
    public Object invoke(Object target, Object[] args) throws Exception {
        return field.get(target);
    }

    @Override
    public Class<?> getType() {
        return field.getType();
    }
}
