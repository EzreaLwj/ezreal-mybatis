package com.ezreal.mybatis.builder;

import com.ezreal.mybatis.session.Configuration;

/**
 * 构建器的基类，建造者模式
 * @author Ezreal
 * @Date 2024/3/5
 */
public class BaseBuilder {

    protected Configuration configuration;

    public BaseBuilder(Configuration configuration) {
        this.configuration = configuration;
    }

    public Configuration getConfiguration() {
        return configuration;
    }
}
