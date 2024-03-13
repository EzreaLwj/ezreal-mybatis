package com.ezreal.mybatis.mapping;

import com.ezreal.mybatis.session.Configuration;
import com.ezreal.mybatis.type.JdbcType;

/**
 * @author Ezreal
 * @Date 2024/3/13
 */
public class ParameterMapping {

    private Configuration configuration;

    // property
    private String property;

    // javaType = int
    private Class<?> javaType = Object.class;

    // jdbcType =numeric
    private JdbcType jdbcType;

    public ParameterMapping() {
    }

    public static class Builder {

        private ParameterMapping parameterMapping = new ParameterMapping();

        public Builder(Configuration configuration, String property, Class<?> javaType) {
            parameterMapping.configuration = configuration;
            parameterMapping.property = property;
            parameterMapping.javaType = javaType;
        }

        public Builder jdbcType(JdbcType jdbcType) {
            parameterMapping.jdbcType = jdbcType;
            return this;
        }

        public Builder javaType(Class<?> javaType) {
            parameterMapping.javaType = javaType;
            return this;
        }

        public ParameterMapping build() {
            return parameterMapping;
        }
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public String getProperty() {
        return property;
    }

    public Class<?> getJavaType() {
        return javaType;
    }

    public JdbcType getJdbcType() {
        return jdbcType;
    }
}
