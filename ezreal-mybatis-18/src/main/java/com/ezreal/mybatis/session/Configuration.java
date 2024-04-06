package com.ezreal.mybatis.session;

import com.ezreal.mybatis.binding.MapperRegistry;
import com.ezreal.mybatis.cache.Cache;
import com.ezreal.mybatis.cache.decorators.FifoCache;
import com.ezreal.mybatis.cache.impl.PerpetualCache;
import com.ezreal.mybatis.datasource.druid.DruidDataSourceFactory;
import com.ezreal.mybatis.datasource.pooled.PooledDataSourceFactory;
import com.ezreal.mybatis.datasource.unpooled.UnpooledDataSourceFactory;
import com.ezreal.mybatis.executor.CachingExecutor;
import com.ezreal.mybatis.executor.Executor;
import com.ezreal.mybatis.executor.SimpleExecutor;
import com.ezreal.mybatis.executor.keygen.KeyGenerator;
import com.ezreal.mybatis.executor.parameter.ParameterHandler;
import com.ezreal.mybatis.executor.resultset.DefaultResultSetHandler;
import com.ezreal.mybatis.executor.resultset.ResultSetHandler;
import com.ezreal.mybatis.executor.statement.PreparedStatementHandler;
import com.ezreal.mybatis.executor.statement.StatementHandler;
import com.ezreal.mybatis.mapping.BoundSql;
import com.ezreal.mybatis.mapping.Environment;
import com.ezreal.mybatis.mapping.MappedStatement;
import com.ezreal.mybatis.mapping.ResultMap;
import com.ezreal.mybatis.plugin.Interceptor;
import com.ezreal.mybatis.plugin.InterceptorChain;
import com.ezreal.mybatis.reflection.MetaObject;
import com.ezreal.mybatis.reflection.factory.DefaultObjectFactory;
import com.ezreal.mybatis.reflection.factory.ObjectFactory;
import com.ezreal.mybatis.reflection.wrapper.DefaultObjectWrapperFactory;
import com.ezreal.mybatis.reflection.wrapper.ObjectWrapperFactory;
import com.ezreal.mybatis.scripting.LanguageDriver;
import com.ezreal.mybatis.scripting.LanguageDriverRegistry;
import com.ezreal.mybatis.scripting.xmltags.XMLLanguageDriver;
import com.ezreal.mybatis.transaction.Transaction;
import com.ezreal.mybatis.transaction.jdbc.JdbcTransactionFactory;
import com.ezreal.mybatis.type.TypeAliasRegistry;
import com.ezreal.mybatis.type.TypeHandlerRegistry;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Mybatis配置类
 *
 * @author Ezreal
 * @Date 2024/3/5
 */
public class Configuration {

    /**
     * 事务环境配置
     */
    protected Environment environment;

    protected boolean useGeneratedKeys = false;

    // 默认启用缓存，cacheEnabled = true/false
    protected boolean cacheEnabled = true;

    // 缓存机制，默认不配置的情况是 SESSION
    protected LocalCacheScope localCacheScope = LocalCacheScope.SESSION;

    /**
     * Mapper注册器
     */
    protected MapperRegistry mapperRegistry = new MapperRegistry(this);

    /**
     * 类型别名注册器
     */
    protected TypeAliasRegistry typeAliasRegistry = new TypeAliasRegistry();

    /**
     * 类型处理注册机
     */
    protected TypeHandlerRegistry typeHandlerRegistry = new TypeHandlerRegistry();

    /**
     * Mapper语句映射 key: 类路径+方法名称
     */
    protected Map<String, MappedStatement> mappedStatements = new HashMap<>();

    protected final LanguageDriverRegistry languageRegistry = new LanguageDriverRegistry();

    // 对象工厂和对象包装器工厂
    protected ObjectFactory objectFactory = new DefaultObjectFactory();

    protected ObjectWrapperFactory objectWrapperFactory = new DefaultObjectWrapperFactory();

    protected final Set<String> loadedResources = new HashSet<>();

    protected String databaseId;

    // 结果映射，存在Map里
    protected final Map<String, ResultMap> resultMaps = new HashMap<>();

    protected final Map<String, KeyGenerator> keyGenerators = new HashMap<>();

    // 插件拦截器链
    protected final InterceptorChain interceptorChain = new InterceptorChain();

    // 缓存,存在Map里
    protected final Map<String, Cache> caches = new HashMap<>();


    public Configuration() {
        typeAliasRegistry.registerAlias("JDBC", JdbcTransactionFactory.class);
        typeAliasRegistry.registerAlias("DRUID", DruidDataSourceFactory.class);
        typeAliasRegistry.registerAlias("UNPOOLED", UnpooledDataSourceFactory.class);
        typeAliasRegistry.registerAlias("POOLED", PooledDataSourceFactory.class);
        typeAliasRegistry.registerAlias("PERPETUAL", PerpetualCache.class);
        typeAliasRegistry.registerAlias("FIFO", FifoCache.class);

        languageRegistry.setDefaultDriverClass(XMLLanguageDriver.class);
    }

    public void addMapper(String packageName) {
        mapperRegistry.addMapper(packageName);
    }

    public <T> void addMapper(Class<T> type) {
        mapperRegistry.addMapper(type);
    }

    public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
        return mapperRegistry.getMapper(type, sqlSession);
    }

    public boolean hasMapper(Class<?> type) {
        return mapperRegistry.hasMapper(type);
    }

    public void addMappedStatement(MappedStatement mappedStatement) {
        mappedStatements.put(mappedStatement.getId(), mappedStatement);
    }

    public MappedStatement getMappedStatement(String id) {
        return mappedStatements.get(id);
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public MapperRegistry getMapperRegistry() {
        return mapperRegistry;
    }

    public void setMapperRegistry(MapperRegistry mapperRegistry) {
        this.mapperRegistry = mapperRegistry;
    }

    public TypeAliasRegistry getTypeAliasRegistry() {
        return typeAliasRegistry;
    }

    public void setTypeAliasRegistry(TypeAliasRegistry typeAliasRegistry) {
        this.typeAliasRegistry = typeAliasRegistry;
    }

    public Map<String, MappedStatement> getMappedStatements() {
        return mappedStatements;
    }

    public void setMappedStatements(Map<String, MappedStatement> mappedStatements) {
        this.mappedStatements = mappedStatements;
    }

    /**
     * 创建执行器
     *
     * @param transaction 事务执行器
     * @return
     */
    public Executor newExecutor(Transaction transaction) {
        Executor executor = new SimpleExecutor(this, transaction);
        // 配置开启缓存，创建 CachingExecutor(默认就是有缓存)装饰者模式
        if (cacheEnabled) {
            executor = new CachingExecutor(executor);
        }
        return executor;
    }

    /**
     * 创建结果集处理器
     */
    public ResultSetHandler newResultSetHandler(Executor executor, MappedStatement mappedStatement, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
        return new DefaultResultSetHandler(executor, mappedStatement, resultHandler, rowBounds, boundSql);
    }


    /**
     * 创建语句映射处理器
     *
     * @param executor
     * @param mappedStatement
     * @param parameter
     * @param resultHandler
     * @param boundSql
     * @return
     */
    public StatementHandler newStatementHandler(Executor executor, MappedStatement mappedStatement, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) {
        // 创建语句处理器，Mybatis 这里加了路由 STATEMENT、PREPARED、CALLABLE 我们默认只根据预处理进行实例化
        StatementHandler statementHandler = new PreparedStatementHandler(executor, mappedStatement, parameter, rowBounds, resultHandler, boundSql);
        // 嵌入插件，代理对象
        statementHandler = (StatementHandler) interceptorChain.pluginAll(statementHandler);
        return statementHandler;
    }

    public MetaObject newMetaObject(Object object) {
        return MetaObject.forObject(object, objectFactory, objectWrapperFactory);
    }

    public boolean isResourceLoaded(String resource) {
        return loadedResources.contains(resource);
    }

    public void addLoadedResource(String resource) {
        loadedResources.add(resource);
    }

    // 类型处理器注册机
    public TypeHandlerRegistry getTypeHandlerRegistry() {
        return typeHandlerRegistry;
    }

    public String getDatabaseId() {
        return databaseId;
    }

    public LanguageDriverRegistry getLanguageRegistry() {
        return languageRegistry;
    }

    public ParameterHandler newParameterHandler(MappedStatement mappedStatement, Object parameterObject, BoundSql boundSql) {
        ParameterHandler parameterHandler = mappedStatement.getLang().createParameterHandler(mappedStatement, parameterObject, boundSql);
        return parameterHandler;
    }

    public LanguageDriver getDefaultScriptingLanguageInstance() {
        return languageRegistry.getDefaultDriver();
    }

    public ObjectFactory getObjectFactory() {
        return objectFactory;
    }

    public void addResultMap(ResultMap resultMap) {
        resultMaps.put(resultMap.getId(), resultMap);
    }

    public ResultMap getResultMap(String id) {
        return resultMaps.get(id);
    }

    public void addKeyGenerator(String id, KeyGenerator keyGenerator) {
        keyGenerators.put(id, keyGenerator);
    }

    public KeyGenerator getKeyGenerator(String id) {
        return keyGenerators.get(id);
    }

    public boolean hasKeyGenerator(String id) {
        return keyGenerators.containsKey(id);
    }

    public boolean isUseGeneratedKeys() {
        return useGeneratedKeys;
    }

    public void setUseGeneratedKeys(boolean useGeneratedKeys) {
        this.useGeneratedKeys = useGeneratedKeys;
    }

    public void addInterceptor(Interceptor interceptorInstance) {
        interceptorChain.addInterceptor(interceptorInstance);
    }

    public void setLocalCacheScope(LocalCacheScope localCacheScope) {
        this.localCacheScope = localCacheScope;
    }

    public LocalCacheScope getLocalCacheScope() {
        return localCacheScope;
    }

    public boolean isCacheEnabled() {
        return cacheEnabled;
    }

    public void setCacheEnabled(boolean cacheEnabled) {
        this.cacheEnabled = cacheEnabled;
    }

    public void addCache(Cache cache) {
        caches.put(cache.getId(), cache);
    }
}
