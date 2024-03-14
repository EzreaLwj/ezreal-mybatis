package com.ezreal.mybatis.scripting.xmltags;

/**
 * SQL节点
 * @author Ezreal
 * @Date 2024/3/13
 */
public interface SqlNode {

    boolean apply(DynamicContext context);
}
