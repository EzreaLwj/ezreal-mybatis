package com.ezreal.mybatis.scripting.xmltags;

import com.ezreal.mybatis.parsing.GenericTokenParser;
import com.ezreal.mybatis.parsing.TokenHandler;
import com.ezreal.mybatis.type.SimpleTypeRegistry;

import java.util.regex.Pattern;

/**
 * 文本SQL节点(CDATA|TEXT)
 *
 * @author Ezreal
 * @Date 2024/4/4
 */
public class TextSqlNode implements SqlNode {

    private String text;

    private Pattern injectionFilter;

    public TextSqlNode(String text) {
        this(text, null);
    }

    public TextSqlNode(String text, Pattern injectionFilter) {
        this.text = text;
        this.injectionFilter = injectionFilter;
    }

    public boolean isDynamic() {
        DynamicCheckerTokenParser checker = new DynamicCheckerTokenParser();
        GenericTokenParser parser = createParser(checker);
        parser.parse(text);
        return checker.isDynamic();
    }

    @Override
    public boolean apply(DynamicContext context) {
        GenericTokenParser parser = createParser(new BindingTokenParser(context, injectionFilter));
        context.appendSql(parser.parse(text));
        return true;
    }

    private GenericTokenParser createParser(TokenHandler handler) {
        return new GenericTokenParser("${", "}", handler);
    }

    //绑定记号解析器
    private static class BindingTokenParser implements TokenHandler {

        private DynamicContext context;

        private Pattern injectionFilter;

        public BindingTokenParser(DynamicContext context, Pattern injectionFilter) {
            this.context = context;
            this.injectionFilter = injectionFilter;
        }

        @Override
        public String handleToken(String content) {
            Object parameter = context.getBindings().get("_parameter");
            if (parameter == null) {
                context.getBindings().put("value", null);
            } else if (SimpleTypeRegistry.isSimpleType(parameter.getClass())) {
                context.getBindings().put("value", parameter);
            }

            Object value = OgnlCache.getValue(content, context.getBindings());
            String srtValue = (value == null ? "" : String.valueOf(value)); // issue #274 return "" instead of "null"
            checkInjection(srtValue);
            return srtValue;
        }

        private void checkInjection(String value) {
            if (injectionFilter != null && !injectionFilter.matcher(value).matches()) {
                throw new RuntimeException("Invalid input. Please conform to regex" + injectionFilter.pattern());
            }
        }
    }

    private static class DynamicCheckerTokenParser implements TokenHandler {

        private boolean isDynamic;

        public DynamicCheckerTokenParser() {
        }

        public boolean isDynamic() {
            return isDynamic;
        }

        @Override
        public String handleToken(String content) {
            // 设置 isDynamic 为 true，即调用了这个类就必定是动态 SQL
            this.isDynamic = true;
            return null;
        }
    }

}
