package com.ezreal.mybatis.builder.xml;

import com.ezreal.mybatis.builder.BaseBuilder;
import com.ezreal.mybatis.builder.MapperBuilderAssistant;
import com.ezreal.mybatis.builder.ResultMapResolver;
import com.ezreal.mybatis.cache.Cache;
import com.ezreal.mybatis.io.Resources;
import com.ezreal.mybatis.mapping.ResultFlag;
import com.ezreal.mybatis.mapping.ResultMap;
import com.ezreal.mybatis.mapping.ResultMapping;
import com.ezreal.mybatis.session.Configuration;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * XML映射构建器，即一个XML映射构建器处理一个XML文件
 *
 * @author Ezreal
 * @Date 2024/3/12
 */
public class XMLMapperBuilder extends BaseBuilder {

    private Element element;

    private String resource;

    private String currentNameSpace;

    private MapperBuilderAssistant builderAssistant;

    public XMLMapperBuilder(InputStream inputStream, Configuration configuration, String resource) throws DocumentException {
        this(new SAXReader().read(inputStream), configuration, resource);
    }

    public XMLMapperBuilder(Document document, Configuration configuration, String resource) {
        super(configuration);
        this.element = document.getRootElement();
        this.builderAssistant = new MapperBuilderAssistant(configuration, resource);
        this.resource = resource;
    }

    public void parse() throws Exception {
        // 判断资源是否已经被解析过
        if (!configuration.isResourceLoaded(currentNameSpace)) {
            configurationElement(element);
            // 标记一下，已经加载过了
            configuration.addLoadedResource(currentNameSpace);
            // 绑定映射器到namespace
            configuration.addMapper(Resources.classForName(currentNameSpace));
        }
    }

    // 配置mapper元素
    // <mapper namespace="org.mybatis.example.BlogMapper">
    //   <select id="selectBlog" parameterType="int" resultType="Blog">
    //    select * from Blog where id = #{id}
    //   </select>
    // </mapper>
    private void configurationElement(Element element) {
        //1.配置namespace
        currentNameSpace = element.attributeValue("namespace");
        if (currentNameSpace.equals("")) {
            throw new RuntimeException("Mapper's namespace cannot be empty");
        }

        builderAssistant.setCurrentNameSpace(currentNameSpace);

        cacheElement(element.element("cache"));

        // 2. 解析ResultMap
        resultMapElements(element.elements("resultMap"));

        // 3.配置select|insert|update|delete
        buildStatementFromContext(element.elements("select"),
                element.elements("insert"),
                element.elements("update"),
                element.elements("delete"));
    }

    private void cacheElement(Element context) {
        if (context == null) {
            return;
        }
        //基础配置信息
        String type = context.attributeValue("type", "PERPETUAL");
        Class<? extends Cache> typeClass = typeAliasRegistry.resolveAlias(type);
        //缓存队列FIFO
        String eviction = context.attributeValue("eviction", "FIFO");
        Class<? extends Cache> evictionClass = typeAliasRegistry.resolveAlias(eviction);

        Long flushInterval = Long.valueOf(context.attributeValue("flushInterval"));
        Integer size = Integer.valueOf(context.attributeValue("size"));
        boolean readWrite = !Boolean.parseBoolean(context.attributeValue("readOnly", "false"));
        boolean blocking = !Boolean.parseBoolean(context.attributeValue("blocking", "false"));

        // 解析额外属性信息；<property name="cacheFile" value="/tmp/xxx-cache.tmp"/>
        List<Element> elements = context.elements();
        Properties prop = new Properties();
        for (Element element : elements) {
            prop.setProperty(element.attributeValue("name"), element.attributeValue("value"));
        }

        // 构建缓存
        builderAssistant.useNewCache(typeClass, evictionClass, flushInterval, size, readWrite, blocking, prop);

    }

    private void resultMapElements(List<Element> list) {
        for (Element element : list) {
            try {
                resultMapElement(element, Collections.emptyList());
            } catch (Exception ignore) {
            }
        }
    }

    /**
     * <resultMap id="activityMap" type="cn.bugstack.mybatis.test.po.Activity">
     * <id column="id" property="id"/>
     * <result column="activity_id" property="activityId"/>
     * <result column="activity_name" property="activityName"/>
     * <result column="activity_desc" property="activityDesc"/>
     * <result column="create_time" property="createTime"/>
     * <result column="update_time" property="updateTime"/>
     * </resultMap>
     */
    private ResultMap resultMapElement(Element resultMapNode, List<ResultMapping> additionalResultMappings) {
        String id = resultMapNode.attributeValue("id");
        String type = resultMapNode.attributeValue("type");
        Class<?> resolveClass = resolveClass(type);

        List<Element> resultList = resultMapNode.elements();

        List<ResultMapping> resultMappings = new ArrayList<>();
        resultMappings.addAll(additionalResultMappings);
        for (Element result : resultList) {
            String name = result.getName();
            List<ResultFlag> flags = new ArrayList<>();
            if ("id".equals(name)) {
                flags.add(ResultFlag.ID);
            }

            resultMappings.add(buildResultMappingFromContext(result, resolveClass, flags));
        }
        // 创建结果映射解析器
        ResultMapResolver resultMapResolver = new ResultMapResolver(builderAssistant, id, resolveClass, resultMappings);
        return resultMapResolver.resolve();

    }

    private ResultMapping buildResultMappingFromContext(Element context, Class<?> resultType, List<ResultFlag> flags) {

        String property = context.attributeValue("property");
        String column = context.attributeValue("column");
        return builderAssistant.buildResultMapping(resultType, property, column, flags);
    }

    // 根据别名解析 Class 类型别名注册/事务管理器别名
    protected Class<?> resolveClass(String alias) {
        if (alias == null) {
            return null;
        }
        try {
            return resolveAlias(alias);
        } catch (Exception e) {
            throw new RuntimeException("Error resolving class. Cause: " + e, e);
        }
    }

    protected Class<?> resolveAlias(String alias) {
        return typeAliasRegistry.resolveAlias(alias);
    }

    /**
     * 解析配置中的SQL标签
     *
     * @param lists
     */
    @SafeVarargs
    private final void buildStatementFromContext(List<Element>... lists) {
        for (List<Element> list : lists) {
            for (Element element : list) {
                XMLStatementBuilder statementBuilder = new XMLStatementBuilder(configuration, builderAssistant, element);
                statementBuilder.parseStatementNode();
            }
        }
    }
}
