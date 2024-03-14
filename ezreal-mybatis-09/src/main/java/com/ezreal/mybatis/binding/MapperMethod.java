package com.ezreal.mybatis.binding;

import com.ezreal.mybatis.mapping.MappedStatement;
import com.ezreal.mybatis.mapping.SqlCommandType;
import com.ezreal.mybatis.session.Configuration;
import com.ezreal.mybatis.session.SqlSession;

import java.lang.reflect.Method;
import java.util.*;

/**
 * 方法映射
 *
 * @author Ezreal
 * @Date 2024/3/5
 */
public class MapperMethod {

    private SqlCommand sqlCommand;

    private MethodSignature method;

    public MapperMethod(Configuration configuration, Class<?> mapperInterface, Method method) {
        this.sqlCommand = new SqlCommand(configuration, mapperInterface, method);
        this.method = new MethodSignature(configuration, method);
    }

    public Object execute(SqlSession sqlSession, Object[] args) {
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
                Object param = method.convertArgsToSqlCommandParam(args);
                result = sqlSession.selectOne(sqlCommand.getName(), param);
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


    public static class MethodSignature {
        private final SortedMap<Integer, String> params;

        public MethodSignature(Configuration configuration, Method method) {
            this.params = Collections.unmodifiableSortedMap(getParams(method));
        }

        // 对传入的参数进行校验，从sortedmap中与args数组进行比较
        public Object convertArgsToSqlCommandParam(Object[] args) {
            final int paramCount = params.size();
            if (args == null || paramCount == 0) {
                return null;
            } else if (paramCount == 1) {
                return args[params.keySet().iterator().next().intValue()];
            } else {
                final Map<String, Object> param = new ParamMap<Object>();
                int i = 0;
                for (Map.Entry<Integer, String> entry : params.entrySet()) {
                    param.put(entry.getValue(), args[entry.getKey().intValue()]);

                    final String str = "param" + (i + 1);
                    if (!param.containsKey(str)) {
                        param.put(str, args[entry.getKey()]);
                    }
                    i++;
                }
                return param;
            }
        }

        // 将参数类型封装到sortedmap中
        private SortedMap<Integer, String> getParams(Method method) {

            final SortedMap<Integer, String> params = new TreeMap<>();
            final Class<?>[] argTypes = method.getParameterTypes();
            for (int i = 0; i < argTypes.length; i++) {
                String paramName = String.valueOf(params.size());
                params.put(i, paramName);
            }
            return params;
        }

    }

    /**
     * 参数map，没有相应的key就会报错
     * @param <V>
     */
    public static class ParamMap<V> extends HashMap<String, V> {

        private static final long serialVersionUID = 7937370882336239375L;

        @Override
        public V get(Object key) {
            if (!super.containsKey(key)) {
                throw new RuntimeException("Parameter '" + key + "' not found. Available parameters are " + keySet());
            }
            return super.get(key);
        }
    }

}
