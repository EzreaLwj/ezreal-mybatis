package com.ezreal.mybatis.mapping;

import com.ezreal.mybatis.executor.keygen.KeyGenerator;
import com.ezreal.mybatis.scripting.LanguageDriver;
import com.ezreal.mybatis.session.Configuration;
import com.ezreal.mybatis.cache.Cache;

import java.util.List;

/**
 * 语句映射类
 * @author Ezreal
 * @Date 2024/3/5
 */
public class MappedStatement {

    /**
     * 唯一id标识
     */
    private String id;

    /**
     * sql语句类型
     */
    private SqlCommandType sqlCommandType;

    /**
     * 配置信息
     */
    private Configuration configuration;

    private SqlSource sqlSource;

    Class<?> resultType;

    private LanguageDriver lang;

    private List<ResultMap> resultMaps;

    private KeyGenerator keyGenerator;

    private String[] keyProperties;

    private String[] keyColumns;

    private boolean flushCacheRequired;

    private Cache cache;

    private boolean useCache;

    public MappedStatement() {
    }

    public BoundSql getBoundSql(Object parameterObject) {
        return sqlSource.getBoundSql(parameterObject);
    }

    public static class Builder {

        private MappedStatement mappedStatement = new MappedStatement();

        public Builder(Configuration configuration, String id, SqlCommandType sqlCommandType, SqlSource sqlSource, Class<?> resultType) {
            mappedStatement.configuration = configuration;
            mappedStatement.id = id;
            mappedStatement.sqlCommandType = sqlCommandType;
            mappedStatement.sqlSource = sqlSource;
            mappedStatement.resultType = resultType;
            mappedStatement.lang = configuration.getDefaultScriptingLanguageInstance();
        }

        public MappedStatement build() {
            assert mappedStatement.configuration != null;
            assert mappedStatement.id != null;
            return mappedStatement;
        }

        public String id() {
            return mappedStatement.id;
        }

        public Builder resultMaps(List<ResultMap> resultMaps) {
            mappedStatement.resultMaps = resultMaps;
            return this;
        }

        public Builder keyGenerator(KeyGenerator keyGenerator) {
            mappedStatement.keyGenerator = keyGenerator;
            return this;
        }

        public Builder keyProperty(String keyProperty) {
            mappedStatement.keyProperties = delimitedStringToArray(keyProperty);
            return this;
        }

        public Builder cache(Cache cache) {
            mappedStatement.cache = cache;
            return this;
        }

        public Builder flushCacheRequired(boolean flushCacheRequired) {
            mappedStatement.flushCacheRequired = flushCacheRequired;
            return this;
        }

        public Builder useCache(boolean useCache) {
            mappedStatement.useCache = useCache;
            return this;
        }

    }
    private static String[] delimitedStringToArray(String in) {
        if (in == null || in.trim().length() == 0) {
            return null;
        } else {
            return in.split(",");
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public SqlCommandType getSqlCommandType() {
        return sqlCommandType;
    }

    public void setSqlCommandType(SqlCommandType sqlCommandType) {
        this.sqlCommandType = sqlCommandType;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public SqlSource getSqlSource() {
        return sqlSource;
    }

    public void setSqlSource(SqlSource sqlSource) {
        this.sqlSource = sqlSource;
    }

    public Class<?> getResultType() {
        return resultType;
    }

    public void setResultType(Class<?> resultType) {
        this.resultType = resultType;
    }

    public LanguageDriver getLang() {
        return lang;
    }

    public List<ResultMap> getResultMaps() {
        return resultMaps;
    }

    public KeyGenerator getKeyGenerator() {
        return keyGenerator;
    }

    public void setKeyGenerator(KeyGenerator keyGenerator) {
        this.keyGenerator = keyGenerator;
    }

    public String[] getKeyProperties() {
        return keyProperties;
    }

    public void setKeyProperties(String[] keyProperties) {
        this.keyProperties = keyProperties;
    }

    public String[] getKeyColumns() {
        return keyColumns;
    }

    public void setKeyColumns(String[] keyColumns) {
        this.keyColumns = keyColumns;
    }

    public boolean isFlushCacheRequired() {
        return flushCacheRequired;
    }

    public boolean isUseCache() {
        return useCache;
    }

    public Cache getCache() {
        return cache;
    }
}
