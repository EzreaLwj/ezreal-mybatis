package com.ezreal.mybatis.builder.xml;

import com.ezreal.mybatis.builder.BaseBuilder;
import com.ezreal.mybatis.builder.MapperBuilderAssistant;
import com.ezreal.mybatis.io.Resources;
import com.ezreal.mybatis.session.Configuration;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.InputStream;
import java.util.List;

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
        currentNameSpace = element.attributeValue("namespace");
        if (currentNameSpace.equals("")) {
            throw new RuntimeException("Mapper's namespace cannot be empty");
        }

        builderAssistant.setCurrentNameSpace(currentNameSpace);
        // 2.配置select|insert|update|delete
        buildStatementFromContext(element.elements("select"),
                element.elements("insert"),
                element.elements("update"),
                element.elements("delete"));
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
