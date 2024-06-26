package com.ezreal.mybatis.scripting.xmltags;

/**
 * IF SQL 节点
 * @author Ezreal
 * @Date 2024/4/4
 */
public class IfSqlNode implements SqlNode {

    private ExpressionEvaluator evaluator;

    private String test;

    private SqlNode contents;

    public IfSqlNode(SqlNode contents, String test) {
        this.test = test;
        this.contents = contents;
        this.evaluator = new ExpressionEvaluator();
    }

    @Override
    public boolean apply(DynamicContext context) {
        if (evaluator.evaluateBoolean(test, context.getBindings())) {
            contents.apply(context);
            return true;
        }
        return false;
    }
}
