package com.ezreal.mybatis.builder;

import com.ezreal.mybatis.mapping.*;
import com.ezreal.mybatis.reflection.MetaClass;
import com.ezreal.mybatis.scripting.LanguageDriver;
import com.ezreal.mybatis.session.Configuration;
import com.ezreal.mybatis.type.TypeHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * 构建器助手
 *
 * @author Ezreal
 * @Date 2024/3/14
 */
public class MapperBuilderAssistant extends BaseBuilder {

    private String currentNameSpace;

    private String resource;

    public MapperBuilderAssistant(Configuration configuration, String resource) {
        super(configuration);
        this.resource = resource;
    }

    public String getCurrentNameSpace() {
        return currentNameSpace;
    }

    public void setCurrentNameSpace(String currentNameSpace) {
        this.currentNameSpace = currentNameSpace;
    }

    public String applyCurrentNameSpace(String base, boolean isReference) {
        if (base == null) {
            return null;
        }
        if (isReference) {
            if (base.contains(".")) {
                return base;
            }
        } else {
            if (base.startsWith(currentNameSpace + ".")) {
                return base;
            }
            if (base.contains(".")) {
                throw new RuntimeException("Dots are not allowed in element names, please remove it from " + base);
            }
        }
        return currentNameSpace + "." + base;
    }

    /**
     * 添加映射器语句
     */
    public MappedStatement addMappedStatement(String id,
                                              SqlSource sqlSource,
                                              SqlCommandType sqlCommandType,
                                              Class<?> parameterType,
                                              String resultMap,
                                              Class<?> resultType,
                                              LanguageDriver lang) {
        // 添加namespace前缀
        id = applyCurrentNameSpace(id, false);
        MappedStatement.Builder statementBuilder = new MappedStatement.Builder(configuration, id, sqlCommandType, sqlSource, resultType);
        // 结果映射，给 MappedStatement#resultMaps
        setStatementResultMap(resultMap, resultType, statementBuilder);

        // 添加映射语句
        MappedStatement statement = statementBuilder.build();
        configuration.addMappedStatement(statement);
        return statement;
    }

    private void setStatementResultMap(String resultMap,
                                       Class<?> resultType,
                                       MappedStatement.Builder statementBuilder) {
        // 因为暂时还没有在 Mapper XML 中配置 Map 返回结果，所以这里返回的是 null
        resultMap = applyCurrentNameSpace(resultMap, true);
        List<ResultMap> resultMaps = new ArrayList<>();
        if (resultMap != null) {
            String[] resultMapNames = resultMap.split(",");
            for (String resultMapName : resultMapNames) {
                resultMaps.add(configuration.getResultMap(resultMapName.trim()));
            }
        } else if (resultType != null) {
            /*
             * 通常使用 resultType 即可满足大部分场景
             * <select id="queryUserInfoById" resultType="cn.bugstack.mybatis.test.po.User">
             * 使用 resultType 的情况下，Mybatis 会自动创建一个 ResultMap，基于属性名称映射列到 JavaBean 的属性上。
             */
            ResultMap.Builder inlineResultMapBuilder = new ResultMap.Builder(configuration, statementBuilder.id() + "-Inline", resultType, new ArrayList<>());
            resultMaps.add(inlineResultMapBuilder.build());
        }

        // 设置结果集映射
        statementBuilder.resultMaps(resultMaps);
    }

    public ResultMap addResultMap(String id, Class<?> type, List<ResultMapping> resultMappings) {
        // 补全ID全路径，如：cn.bugstack.mybatis.test.dao.IActivityDao + activityMap
        id = applyCurrentNamespace(id, false);
        ResultMap.Builder inlineResultMapBuilder = new ResultMap.Builder(
                configuration,
                id,
                type,
                resultMappings);

        ResultMap resultMap = inlineResultMapBuilder.build();
        configuration.addResultMap(resultMap);
        return resultMap;
    }

    public String applyCurrentNamespace(String base, boolean isReference) {
        if (base == null) {
            return null;
        }

        if (isReference) {
            if (base.contains(".")) return base;
        } else {
            if (base.startsWith(currentNameSpace + ".")) {
                return base;
            }
            if (base.contains(".")) {
                throw new RuntimeException("Dots are not allowed in element names, please remove it from " + base);
            }
        }

        return currentNameSpace + "." + base;
    }


    public ResultMapping buildResultMapping(Class<?> resultType, String property, String column, List<ResultFlag> flags) {
        Class<?> javaTypeClass = resolveResultJavaType(resultType, property, null);
        TypeHandler<?> typeHandler = resolveTypeHandler(javaTypeClass, null);

        ResultMapping.Builder builder = new ResultMapping.Builder(configuration, property, column, javaTypeClass);
        builder.typeHandler(typeHandler);
        builder.flags(flags);
        return builder.build();

    }

    private Class<?> resolveResultJavaType(Class<?> resultType, String property, Class<?> javaType) {
        if (javaType == null && property != null) {
            try {
                MetaClass metaResultType = MetaClass.forClass(resultType);
                javaType = metaResultType.getSetterType(property);
            } catch (Exception ignore) {
            }
        }
        if (javaType == null) {
            javaType = Object.class;
        }
        return javaType;
    }

    protected TypeHandler<?> resolveTypeHandler(Class<?> javaType, Class<? extends TypeHandler<?>> typeHandlerType) {
        if (typeHandlerType == null) {
            return null;
        }
        return typeHandlerRegistry.getMappingTypeHandler(typeHandlerType);
    }
}
