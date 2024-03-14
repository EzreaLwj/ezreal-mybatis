package com.ezreal.mybatis.session;

import com.ezreal.mybatis.binding.MapperRegistry;
import com.ezreal.mybatis.datasource.druid.DruidDataSourceFactory;
import com.ezreal.mybatis.datasource.pooled.PooledDataSourceFactory;
import com.ezreal.mybatis.datasource.unpooled.UnpooledDataSourceFactory;
import com.ezreal.mybatis.executor.Executor;
import com.ezreal.mybatis.executor.SimpleExecutor;
import com.ezreal.mybatis.executor.parameter.ParameterHandler;
import com.ezreal.mybatis.executor.resultset.DefaultResultSetHandler;
import com.ezreal.mybatis.executor.resultset.ResultSetHandler;
import com.ezreal.mybatis.executor.statement.PreparedStatementHandler;
import com.ezreal.mybatis.executor.statement.StatementHandler;
import com.ezreal.mybatis.mapping.BoundSql;
import com.ezreal.mybatis.mapping.Environment;
import com.ezreal.mybatis.mapping.MappedStatement;
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

    public Configuration() {
        typeAliasRegistry.registerAlias("JDBC", JdbcTransactionFactory.class);
        typeAliasRegistry.registerAlias("DRUID", DruidDataSourceFactory.class);
        typeAliasRegistry.registerAlias("UNPOOLED", UnpooledDataSourceFactory.class);
        typeAliasRegistry.registerAlias("POOLED", PooledDataSourceFactory.class);

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
        return new SimpleExecutor(this, transaction);
    }

    /**
     * 创建结果集处理器
     *
     * @param executor        执行器
     * @param mappedStatement 语句映射
     * @param boundSql        绑定SQL
     * @return
     */
    public ResultSetHandler newResultSetHandler(Executor executor, MappedStatement mappedStatement, BoundSql boundSql) {
        return new DefaultResultSetHandler(executor, mappedStatement, boundSql);
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
    public StatementHandler newStatementHandler(Executor executor, MappedStatement mappedStatement, Object parameter, ResultHandler resultHandler, BoundSql boundSql) {
        return new PreparedStatementHandler(executor, mappedStatement, parameter, resultHandler, boundSql);
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
}
