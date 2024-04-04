package com.ezreal.mybatis.mapping;

import com.ezreal.mybatis.session.Configuration;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 结果映射
 * @author Ezreal
 * @Date 2024/3/14
 */
public class ResultMap {

    // id
    private String id;
    // 对象映射类型
    private Class<?> type;
    // 映射列
    private Set<String> mappedColumns;
    // 具体映射信息
    private List<ResultMapping> resultMappings;

    public ResultMap() {
    }

    public static class Builder {

        private ResultMap resultMap = new ResultMap();

        public Builder(Configuration configuration, String id, Class<?> type, List<ResultMapping> resultMappings) {
            resultMap.id = id;
            resultMap.type = type;
            resultMap.resultMappings = resultMappings;
        }

        public ResultMap build() {
            resultMap.mappedColumns = new HashSet<>();
            for (ResultMapping resultMapping : resultMap.resultMappings) {
                final String column = resultMapping.getColumn();
                if (column != null) {
                    resultMap.mappedColumns.add(column.toUpperCase(Locale.ENGLISH));
                }
            }
            return resultMap;
        }
    }

    public String getId() {
        return id;
    }

    public Set<String> getMappedColumns() {
        return mappedColumns;
    }

    public Class<?> getType() {
        return type;
    }

    public List<ResultMapping> getResultMappings() {
        return resultMappings;
    }
}
