package com.ezreal.mybatis.builder.xml;

import com.ezreal.mybatis.builder.BaseBuilder;
import com.ezreal.mybatis.datasource.DataSourceFactory;
import com.ezreal.mybatis.io.Resources;
import com.ezreal.mybatis.mapping.BoundSql;
import com.ezreal.mybatis.mapping.Environment;
import com.ezreal.mybatis.mapping.MappedStatement;
import com.ezreal.mybatis.mapping.SqlCommandType;
import com.ezreal.mybatis.session.Configuration;
import com.ezreal.mybatis.transaction.TransactionFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.InputSource;

import javax.sql.DataSource;
import java.io.InputStream;
import java.io.Reader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Ezreal
 * @Date 2024/3/5
 */
public class XmlConfigBuilder extends BaseBuilder {

    private Element root;

    public XmlConfigBuilder(Reader reader) {

        // 1.创建configuration
        super(new Configuration());

        // 2. 解析Xml文件
        SAXReader saxReader = new SAXReader();
        try {
            Document document = saxReader.read(new InputSource(reader));
            root = document.getRootElement();
        } catch (DocumentException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 解析配置：类型别名、插件、对象工厂、对象包装工厂、设置、环境、类型转换、映射器
     *
     * @return
     */
    public Configuration parse() {
        try {

            // 解析环境
            environmentElement(root.element("environments"));
            // 解析映射器
            mappedElement(root.element("mappers"));
            return configuration;
        } catch (Exception e) {
            throw new RuntimeException("Parse SQL Mapper Error:" + e.getMessage(), e);
        }
    }

    public void environmentElement(Element context) throws Exception {
        String environmentId = context.attributeValue("default");
        List<Element> environments = context.elements("environment");

        for (Element environment : environments) {

            // 选择指定的环境信息
            String id = environment.attributeValue("id");
            if (environmentId.equals(id)) {
                // 解析事务管理器
                Element transactionManagerElement = environment.element("transactionManager");
                TransactionFactory transactionFactory = (TransactionFactory) configuration.getTypeAliasRegistry().resolveAlias(transactionManagerElement.attributeValue("type")).newInstance();

                // 解析数据源配置信息
                Element dataSourceElement = environment.element("dataSource");
                String dataSourceType = dataSourceElement.attributeValue("type");
                DataSourceFactory dataSourceFactory = (DataSourceFactory) configuration.getTypeAliasRegistry().resolveAlias(dataSourceType).newInstance();

                Properties prop = new Properties();
                List<Element> properties = dataSourceElement.elements("property");
                for (Element property : properties) {
                    String name = property.attributeValue("name");
                    String value = property.attributeValue("value");
                    prop.put(name, value);
                }
                dataSourceFactory.setProperties(prop);
                DataSource dataSource = dataSourceFactory.getDataSource();

                // 配置环境信息
                Environment env = new Environment.Builder(id).dataSource(dataSource).transactionFactory(transactionFactory).build();
                configuration.setEnvironment(env);
            }
        }
    }

    public void mappedElement(Element mappers) throws Exception {
        List<Element> elements = mappers.elements("mapper");
        for (Element e : elements) {
            String resource = e.attributeValue("resource");
            InputStream inputStream = Resources.getResourceAsStream(resource);
            XMLMapperBuilder mapperBuilder = new XMLMapperBuilder(inputStream, configuration, resource);
            mapperBuilder.parse();
        }

    }

}
