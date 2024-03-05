package com.ezreal.mybatis.builder.xml;

import com.ezreal.mybatis.builder.BaseBuilder;
import com.ezreal.mybatis.io.Resources;
import com.ezreal.mybatis.mapping.MappedStatement;
import com.ezreal.mybatis.mapping.SqlCommandType;
import com.ezreal.mybatis.session.Configuration;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.InputSource;

import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
            // 解析映射器
            mappedElement(root.element("mappers"));
            return configuration;
        } catch (Exception e) {
            throw new RuntimeException("Parse SQL Mapper Error:" + e.getMessage(), e);
        }
    }

    public void mappedElement(Element mappers) throws Exception {
        List<Element> mapperList = mappers.elements("mapper");
        for (Element mapper : mapperList) {
            // 获取mapper文件的位置
            String resource = mapper.attributeValue("resource");

            // 获取mapper配置文件的信息
            SAXReader saxReader = new SAXReader();
            Document document = saxReader.read(new InputSource(Resources.getResourceAsReader(resource)));
            Element root = document.getRootElement();

            // 解析信息
            String namespace = root.attributeValue("namespace");

            // 解析select语句
            List<Element> selectList = root.elements("select");
            for (Element selectElement : selectList) {
                String id = selectElement.attributeValue("id");
                String parameterType = selectElement.attributeValue("parameterType");
                String resultType = selectElement.attributeValue("resultType");
                String sql = selectElement.getText();

                // 通过正则封装参数
                Map<Integer, String> parameter = new HashMap<>();
                Pattern pattern = Pattern.compile("(#\\{(.*?)})");
                Matcher matcher = pattern.matcher(sql);
                for (int i = 1; matcher.find(); i++) {
                    String g1 = matcher.group(1);
                    String g2 = matcher.group(2);
                    parameter.put(i, g2);
                    sql = sql.replace(g1, "?");
                }

                // id的唯一标识为类路径+方法名
                id = namespace + "." + id;
                String name = selectElement.getName();
                SqlCommandType sqlCommandType = SqlCommandType.valueOf(name.toUpperCase(Locale.ENGLISH));

                MappedStatement mappedStatement = new MappedStatement.Builder(configuration, id, sqlCommandType, parameterType, resultType, sql, parameter).build();
                // 添加解析 SQL
                configuration.addMappedStatement(mappedStatement);
            }

            // 添加mapper映射
            configuration.addMapper(namespace);

        }
    }

}
