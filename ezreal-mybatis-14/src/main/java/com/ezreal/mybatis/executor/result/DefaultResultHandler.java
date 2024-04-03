package com.ezreal.mybatis.executor.result;

import com.ezreal.mybatis.reflection.factory.ObjectFactory;
import com.ezreal.mybatis.session.ResultContext;
import com.ezreal.mybatis.session.ResultHandler;

import java.util.List;

/**
 * @author Ezreal
 * @Date 2024/3/14
 */
public class DefaultResultHandler implements ResultHandler {

    private List<Object> list;

    @SuppressWarnings("unchecked")
    public DefaultResultHandler(ObjectFactory objectFactory) {
        list = objectFactory.create(List.class);
    }

    @Override
    public void handleResult(ResultContext context) {
        list.add(context.getResultObject());
    }

    public List<Object> getResultList() {
        return list;
    }
}
