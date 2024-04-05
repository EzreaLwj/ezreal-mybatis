package com.ezreal.mybatis.builder;

import com.ezreal.mybatis.mapping.ParameterMapping;
import com.ezreal.mybatis.mapping.SqlSource;
import com.ezreal.mybatis.parsing.GenericTokenParser;
import com.ezreal.mybatis.parsing.TokenHandler;
import com.ezreal.mybatis.reflection.MetaClass;
import com.ezreal.mybatis.reflection.MetaObject;
import com.ezreal.mybatis.session.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * SQL源码构建器
 *
 * @author Ezreal
 * @Date 2024/3/13
 */
public class SqlSourceBuilder extends BaseBuilder {

    private static final String parameterProperties = "javaType,jdbcType,mode,numericScale,resultMap,typeHandler,jdbcTypeName";

    public SqlSourceBuilder(Configuration configuration) {
        super(configuration);
    }


    public SqlSource parse(String originalSql, Class<?> parameterType, Map<String, Object> additionalParameters) {
        ParameterMappingTokenHandler handler = new ParameterMappingTokenHandler(configuration, parameterType, additionalParameters);
        GenericTokenParser parse = new GenericTokenParser("#{", "}", handler);
        // 将参数中的 #{id} 替换为 ?
        String sql = parse.parse(originalSql);
        return new StaticSqlSource(sql, handler.getParameterMappings(), configuration);
    }

    private static class ParameterMappingTokenHandler extends BaseBuilder implements TokenHandler {

        private Logger logger = LoggerFactory.getLogger(SqlSourceBuilder.class);

        private List<ParameterMapping> parameterMappings = new ArrayList<>();

        private Class<?> parameterType;

        private MetaObject metaParameters;

        public ParameterMappingTokenHandler(Configuration configuration, Class<?> parameterType, Map<String, Object> additionalParameters) {
            super(configuration);
            this.metaParameters = configuration.newMetaObject(additionalParameters);
            this.parameterType = parameterType;
        }

        public List<ParameterMapping> getParameterMappings() {
            return parameterMappings;
        }

        @Override
        public String handleToken(String content) {
            parameterMappings.add(buildParameterMapping(content));
            return "?";
        }

        private ParameterMapping buildParameterMapping(String content) {
            // 先解析参数映射,就是转化成一个 HashMap | #{favouriteSection,jdbcType=VARCHAR}
            Map<String, String> propertiesMap = new ParameterExpression(content);
            // 解析favouriteSection
            String property = propertiesMap.get("property");
            Class<?> propertyType;
            if (typeHandlerRegistry.hasTypeHandler(parameterType)) {
                propertyType = parameterType;
            } else if (property != null) {
                MetaClass metaClass = MetaClass.forClass(parameterType);
                if (metaClass.hasSetter(property)) {
                    propertyType = metaClass.getGetterType(property);
                } else {
                    propertyType = Object.class;
                }
            } else {
                propertyType = Object.class;
            }
            logger.info("构建参数映射 property：{} propertyType：{}", property, propertyType);
            ParameterMapping.Builder builder = new ParameterMapping.Builder(configuration, property, propertyType);
            return builder.build();
        }
    }
}
