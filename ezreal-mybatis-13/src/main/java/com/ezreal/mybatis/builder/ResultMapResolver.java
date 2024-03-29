package com.ezreal.mybatis.builder;

import com.ezreal.mybatis.mapping.ResultMap;
import com.ezreal.mybatis.mapping.ResultMapping;

import java.util.List;

/**
 * @author Ezreal
 * @Date 2024/3/29
 */
public class ResultMapResolver {

    private final MapperBuilderAssistant assistant;

    private String id;

    private Class<?> type;

    private List<ResultMapping> resultMappings;

    public ResultMapResolver(MapperBuilderAssistant assistant, String id, Class<?> type, List<ResultMapping> resultMappings) {
        this.assistant = assistant;
        this.id = id;
        this.type = type;
        this.resultMappings = resultMappings;
    }

    public ResultMap resolve() {
        return assistant.addResultMap(this.id, this.type, this.resultMappings);
    }
}
