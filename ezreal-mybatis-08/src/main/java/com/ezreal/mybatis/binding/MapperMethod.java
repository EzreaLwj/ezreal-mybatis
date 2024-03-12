package com.ezreal.mybatis.binding;

import com.ezreal.mybatis.mapping.MappedStatement;
import com.ezreal.mybatis.mapping.SqlCommandType;
import com.ezreal.mybatis.session.Configuration;
import com.ezreal.mybatis.session.SqlSession;

import java.lang.reflect.Method;

/**
 * 方法映射
 * @author Ezreal
 * @Date 2024/3/5
 */
public class MapperMethod {

    private SqlCommand sqlCommand;

    public MapperMethod(Configuration configuration, Class<?> mapperInterface, Method method) {
        this.sqlCommand = new SqlCommand(configuration, mapperInterface, method);
    }

    public Object execute(SqlSession sqlSession, Object args) {
        Object result = null;
        SqlCommandType type = sqlCommand.getType();
        switch (type) {

            case DELETE:
                break;
            case INSERT:
                break;
            case UPDATE:
                break;
            case SELECT:
                result = sqlSession.selectOne(sqlCommand.getName(), args);
                break;
            default:
                throw new RuntimeException("Unknown execution method for: " + sqlCommand.getName());
        }

        return result;
    }

    public static class SqlCommand {

        private final String name;

        private final SqlCommandType type;

        public SqlCommand(Configuration configuration, Class<?> mapperInterface, Method method) {
            String statementName = mapperInterface.getName() + "." + method.getName();
            MappedStatement mappedStatement = configuration.getMappedStatement(statementName);
            name = mappedStatement.getId();
            type = mappedStatement.getSqlCommandType();
        }

        public String getName() {
            return name;
        }

        public SqlCommandType getType() {
            return type;
        }
    }

}
