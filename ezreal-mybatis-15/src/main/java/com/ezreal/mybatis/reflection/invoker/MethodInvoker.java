package com.ezreal.mybatis.reflection.invoker;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * 方法反射调用
 *
 * @author Ezreal
 * @Date 2024/3/10
 */
public class MethodInvoker implements Invoker {

    private Method method;

    private Class<?> type;

    public MethodInvoker(Method method) {
        this.method = method;
        Parameter[] parameters = method.getParameters();

        // 如果只有一个参数，返回参数类型，否则返回return类型
        if (parameters.length == 1) {
            type = parameters[0].getType();
        } else {
            type = method.getReturnType();
        }
    }

    @Override
    public Object invoke(Object target, Object[] args) throws Exception {
        return method.invoke(target, args);
    }

    @Override
    public Class<?> getType() {
        return type;
    }

}
