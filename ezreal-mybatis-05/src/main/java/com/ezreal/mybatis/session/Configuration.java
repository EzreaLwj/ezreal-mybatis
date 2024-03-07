package com.ezreal.mybatis.session;

import com.ezreal.mybatis.binding.MapperRegistry;
import com.ezreal.mybatis.datasource.druid.DruidDataSourceFactory;
import com.ezreal.mybatis.datasource.unpooled.UnpooledDataSourceFactory;
import com.ezreal.mybatis.mapping.Environment;
import com.ezreal.mybatis.mapping.MappedStatement;
import com.ezreal.mybatis.transaction.jdbc.JdbcTransactionFactory;
import com.ezreal.mybatis.type.TypeAliasRegistry;

import java.util.HashMap;
import java.util.Map;

/**
 * Mybatis配置类
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
     * Mapper语句映射 key: 类路径+方法名称
     */
    protected Map<String, MappedStatement> mappedStatements = new HashMap<>();

    public Configuration() {
        typeAliasRegistry.registerAlias("JDBC", JdbcTransactionFactory.class);
        typeAliasRegistry.registerAlias("DRUID", DruidDataSourceFactory.class);
        typeAliasRegistry.registerAlias("UNPOOLED", UnpooledDataSourceFactory.class);
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
}
