package com.ezreal.mybatis.session.defaults;

import com.ezreal.mybatis.executor.Executor;
import com.ezreal.mybatis.mapping.MappedStatement;
import com.ezreal.mybatis.session.Configuration;
import com.ezreal.mybatis.session.RowBounds;
import com.ezreal.mybatis.session.SqlSession;

import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Ezreal
 * @Date 2024/3/4
 */
public class DefaultSqlSession implements SqlSession {

    private Configuration configuration;

    private Executor executor;

    public DefaultSqlSession(Configuration configuration) {
        this.configuration = configuration;
    }

    public DefaultSqlSession(Configuration configuration, Executor executor) {
        this.configuration = configuration;
        this.executor = executor;
    }

    @Override
    public <T> T selectOne(String statement) {
        return (T) ("你被代理了！" + statement);
    }

    @Override
    public <T> T selectOne(String statement, Object parameter) {

        try {
            MappedStatement mappedStatement = configuration.getMappedStatement(statement);
            List<T> result = executor.query(mappedStatement, parameter, RowBounds.DEFAULT, Executor.NO_RESULT_HANDLER, mappedStatement.getSqlSource().getBoundSql(parameter));
            return result.get(0);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public <E> List<E> selectList(String statement, Object parameter) {
        MappedStatement mappedStatement = configuration.getMappedStatement(statement);
        try {
            return executor.query(mappedStatement, parameter, RowBounds.DEFAULT, Executor.NO_RESULT_HANDLER, mappedStatement.getSqlSource().getBoundSql(parameter));
        } catch (Exception e) {
            throw new RuntimeException("Error querying database.  Cause: " + e);
        }
    }

    @Override
    public int insert(String statement, Object parameter) {
        return update(statement, parameter);
    }

    @Override
    public int update(String statement, Object parameter) {
        MappedStatement mappedStatement = configuration.getMappedStatement(statement);
        try {
            return executor.update(mappedStatement, parameter);
        } catch (SQLException e) {
            throw new RuntimeException("Error updating database.  Cause: " + e);
        }
    }

    @Override
    public Object delete(String statement, Object parameter) {
        return update(statement, parameter);
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


    @Override
    public <T> T getMapper(Class<T> type) {
        return configuration.getMapper(type, this);
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public void commit() {
        try {
            executor.commit(true);
        } catch (SQLException e) {
            throw new RuntimeException("Error committing transaction.  Cause: " + e);
        }
    }
}
