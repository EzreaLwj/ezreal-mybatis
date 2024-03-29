package com.ezreal.mybatis.executor.result;

import com.ezreal.mybatis.session.ResultContext;

/**
 * 默认结果上下文
 * @author Ezreal
 * @Date 2024/3/14
 */
public class DefaultResultContext implements ResultContext {

    private Object resultObject;

    private int resultCount;

    public DefaultResultContext() {
        this.resultObject = null;
        this.resultCount = 0;
    }

    @Override
    public Object getResultObject() {
        return resultObject;
    }

    @Override
    public int getResultCount() {
        return resultCount;
    }

    public void nextResultObject(Object resultObject) {
        resultCount++;
        this.resultObject = resultObject;
    }
}
