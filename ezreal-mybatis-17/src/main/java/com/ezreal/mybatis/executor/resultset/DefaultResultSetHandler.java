package com.ezreal.mybatis.executor.resultset;

import com.ezreal.mybatis.executor.Executor;
import com.ezreal.mybatis.executor.result.DefaultResultContext;
import com.ezreal.mybatis.executor.result.DefaultResultHandler;
import com.ezreal.mybatis.mapping.BoundSql;
import com.ezreal.mybatis.mapping.MappedStatement;
import com.ezreal.mybatis.mapping.ResultMap;
import com.ezreal.mybatis.mapping.ResultMapping;
import com.ezreal.mybatis.reflection.MetaClass;
import com.ezreal.mybatis.reflection.MetaObject;
import com.ezreal.mybatis.reflection.factory.ObjectFactory;
import com.ezreal.mybatis.session.Configuration;
import com.ezreal.mybatis.session.ResultHandler;
import com.ezreal.mybatis.session.RowBounds;
import com.ezreal.mybatis.type.TypeHandler;
import com.ezreal.mybatis.type.TypeHandlerRegistry;

import java.lang.reflect.Method;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * @author Ezreal
 * @Date 2024/3/8
 */
public class DefaultResultSetHandler implements ResultSetHandler {

    private Configuration configuration;

    private final BoundSql boundSql;

    private MappedStatement mappedStatement;

    private final ObjectFactory objectFactory;

    private RowBounds rowBounds;

    private ResultHandler resultHandler;

    private TypeHandlerRegistry typeHandlerRegistry;

    public DefaultResultSetHandler(Executor executor, MappedStatement mappedStatement, ResultHandler resultHandler, RowBounds rowBounds, BoundSql boundSql) {
        this.boundSql = boundSql;
        this.mappedStatement = mappedStatement;
        this.configuration = mappedStatement.getConfiguration();
        objectFactory = configuration.getObjectFactory();
        this.rowBounds = rowBounds;
        this.resultHandler = resultHandler;
        this.typeHandlerRegistry = configuration.getTypeHandlerRegistry();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Object> handleResultSets(Statement statement) throws SQLException {

        final List<Object> multipleResults = new ArrayList<>();
        int resultSetCount = 0;
        ResultSetWrapper rsw = new ResultSetWrapper(statement.getResultSet(), configuration);

        // 获取语句的返回值配置
        List<ResultMap> resultMaps = mappedStatement.getResultMaps();
        while (rsw != null && resultMaps.size() > resultSetCount) {
            // 遍历所有的resultMap
            ResultMap resultMap = resultMaps.get(resultSetCount);
            handleResultSet(rsw, resultMap, multipleResults, null);
            rsw = getNextResultSet(statement);
            resultSetCount++;
        }
        return multipleResults.size() == 1 ? (List<Object>) multipleResults.get(0) : multipleResults;

    }

    private ResultSetWrapper getNextResultSet(Statement stmt) throws SQLException {
        // Making this method tolerant of bad JDBC drivers
        try {
            if (stmt.getConnection().getMetaData().supportsMultipleResultSets()) {
                // Crazy Standard JDBC way of determining if there are more results
                if (!((!stmt.getMoreResults()) && (stmt.getUpdateCount() == -1))) {
                    ResultSet rs = stmt.getResultSet();
                    return rs != null ? new ResultSetWrapper(rs, configuration) : null;
                }
            }
        } catch (Exception ignore) {
            // Intentionally ignored.
        }
        return null;
    }

    private void handleResultSet(ResultSetWrapper rsw, ResultMap resultMap, List<Object> multipleResults, ResultMapping resultMapping) throws SQLException {
        if (resultHandler == null) {
            // 1.新创建结果处理器
            DefaultResultHandler defaultResultHandler = new DefaultResultHandler(objectFactory);
            // 2.封装数据
            handleRowValuesForSimpleResultMap(rsw, resultMap, defaultResultHandler, rowBounds, null);
            // 3.保存结果
            multipleResults.add(defaultResultHandler.getResultList());

        }
    }

    private void handleRowValuesForSimpleResultMap(ResultSetWrapper rsw, ResultMap resultMap, DefaultResultHandler resultHandler, RowBounds rowBounds, ResultMapping parentMapping) throws SQLException {
        DefaultResultContext resultContext = new DefaultResultContext();
        while (resultContext.getResultCount() < rowBounds.getLimit() && rsw.getResultSet().next()) {
            Object rowValue = getRowValue(rsw, resultMap);
            callResultHandler(resultHandler, resultContext, rowValue);
        }
    }

    private void callResultHandler(ResultHandler resultHandler, DefaultResultContext resultContext, Object rowValue) {
        resultContext.nextResultObject(rowValue);
        resultHandler.handleResult(resultContext);
    }

    /**
     * 获取一行的值
     */
    private Object getRowValue(ResultSetWrapper rsw, ResultMap resultMap) throws SQLException {
        // 根据返回类型，实例化对象
        Object resultObject = createResultObject(rsw, resultMap, null);
        if (resultObject != null && !typeHandlerRegistry.hasTypeHandler(resultMap.getType())) {
            // 获取该对象的反射信息
            final MetaObject metaObject = configuration.newMetaObject(resultObject);
            // 对该对象进行赋值操作
            applyAutomaticMappings(rsw, resultMap, metaObject, null);
            // Map映射：根据映射类型赋值到字段
            applyPropertyMappings(rsw, resultMap, metaObject, null);
        }
        return resultObject;
    }

    private boolean applyPropertyMappings(ResultSetWrapper rsw, ResultMap resultMap, MetaObject metaObject, String columnPrefix) throws SQLException {
        List<String> mappedColumnNames = rsw.getMappedColumnNames(resultMap, columnPrefix);
        boolean foundValues = false;
        List<ResultMapping> propertyMappings = resultMap.getResultMappings();
        for (ResultMapping propertyMapping : propertyMappings) {
            final String column = propertyMapping.getColumn();
            if (column != null && mappedColumnNames.contains(column.toUpperCase(Locale.ENGLISH))) {
                final TypeHandler<?> typeHandler =  propertyMapping.getTypeHandler();
                Object value = typeHandler.getResult(rsw.getResultSet(), column);

                final String property = propertyMapping.getProperty();
                if (value != NO_VALUE && property != null && value != null) {
                    // 通过反射工具类设置属性值
                    metaObject.setValue(property, value);
                    foundValues = true;
                }
            }

        }
        return foundValues;
    }

    private static final Object NO_VALUE = new Object();

    private boolean applyAutomaticMappings(ResultSetWrapper rsw, ResultMap resultMap, MetaObject metaObject, String columnPrefix) throws SQLException {
        final List<String> unmappedColumnNames = rsw.getUnMappedColumnNames(resultMap, columnPrefix);
        boolean foundValues = false;
        for (String columnName : unmappedColumnNames) {
            String propertyName = columnName;
            if (columnPrefix != null && !columnPrefix.isEmpty()) {
                // When columnPrefix is specified,ignore columns without the prefix.
                if (columnName.toUpperCase(Locale.ENGLISH).startsWith(columnPrefix)) {
                    propertyName = columnName.substring(columnPrefix.length());
                } else {
                    continue;
                }
            }
            final String property = metaObject.findProperty(propertyName, false);
            if (property != null && metaObject.hasSetter(property)) {
                final Class<?> propertyType = metaObject.getSetterType(property);
                if (typeHandlerRegistry.hasTypeHandler(propertyType)) {
                    final TypeHandler<?> typeHandler = rsw.getTypeHandler(propertyType, columnName);
                    // 使用 TypeHandler 取得结果
                    final Object value = typeHandler.getResult(rsw.getResultSet(), columnName);
                    if (value != null) {
                        foundValues = true;
                    }
                    if (value != null || !propertyType.isPrimitive()) {
                        // 通过反射工具类设置属性值
                        metaObject.setValue(property, value);
                    }
                }
            }
        }
        return foundValues;
    }

    private Object createResultObject(ResultSetWrapper rsw, ResultMap resultMap, String columnPrefix) throws SQLException {
        final List<Class<?>> constructorArgTypes = new ArrayList<>();
        final List<Object> constructorArgs = new ArrayList<>();
        return createResultObject(rsw, resultMap, constructorArgTypes, constructorArgs, columnPrefix);
    }

    /**
     * 创建结果
     */
    private Object createResultObject(ResultSetWrapper rsw, ResultMap resultMap, List<Class<?>> constructorArgTypes, List<Object> constructorArgs, String columnPrefix) throws SQLException {
        final Class<?> resultType = resultMap.getType();
        final MetaClass metaType = MetaClass.forClass(resultType);
        if (resultType.isInterface() || metaType.hasDefaultConstructor()) {
            // 普通的Bean对象类型
            return objectFactory.create(resultType);
        } else if (typeHandlerRegistry.hasTypeHandler(resultType)) {
            return createPrimitiveResultObject(rsw, resultMap, columnPrefix);
        }
        throw new RuntimeException("Do not know how to create an instance of " + resultType);
    }

    // 简单类型创建
    private Object createPrimitiveResultObject(ResultSetWrapper rsw, ResultMap resultMap, String columnPrefix) throws SQLException {
        final Class<?> resultType = resultMap.getType();
        final String columnName;
        if (!resultMap.getResultMappings().isEmpty()) {
            final List<ResultMapping> resultMappingList = resultMap.getResultMappings();
            final ResultMapping mapping = resultMappingList.get(0);
            columnName = prependPrefix(mapping.getColumn(), columnPrefix);
        } else {
            columnName = rsw.getColumnNames().get(0);
        }
        final TypeHandler<?> typeHandler = rsw.getTypeHandler(resultType, columnName);
        return typeHandler.getResult(rsw.getResultSet(), columnName);
    }

    private String prependPrefix(String columnName, String prefix) {
        if (columnName == null || columnName.length() == 0 || prefix == null || prefix.length() == 0) {
            return columnName;
        }
        return prefix + columnName;
    }

    private <T> List<T> resultSet2Obj(ResultSet resultSet, Class<?> clazz) {
        List<T> list = new ArrayList<>();
        try {
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            // 每次遍历行值
            while (resultSet.next()) {
                T obj = (T) clazz.newInstance();
                for (int i = 1; i <= columnCount; i++) {
                    Object value = resultSet.getObject(i);
                    String columnName = metaData.getColumnName(i);
                    String setMethod = "set" + columnName.substring(0, 1).toUpperCase() + columnName.substring(1);
                    Method method;
                    if (value instanceof Timestamp) {
                        method = clazz.getMethod(setMethod, Date.class);
                    } else {
                        method = clazz.getMethod(setMethod, value.getClass());
                    }
                    method.invoke(obj, value);
                }
                list.add(obj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
