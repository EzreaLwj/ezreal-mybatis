package com.ezreal.mybatis.type;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author Ezreal
 * @Date 2024/3/6
 */
public class TypeAliasRegistry {

    private final Map<String, Class<?>> TYPE_ALIASES = new HashMap<>();

    public TypeAliasRegistry() {

        // 构造函数中注册系统内置的类型别名
        TYPE_ALIASES.put("string", String.class);
        // 基本数据类型
        TYPE_ALIASES.put("byte", Byte.class);
        TYPE_ALIASES.put("int", Integer.class);
        TYPE_ALIASES.put("integer", Integer.class);
        TYPE_ALIASES.put("short", Short.class);
        TYPE_ALIASES.put("long", Long.class);
        TYPE_ALIASES.put("boolean", Boolean.class);
        TYPE_ALIASES.put("double", Double.class);
        TYPE_ALIASES.put("float", Float.class);
    }

    public void registerAlias(String alias, Class<?> value) {
        alias = alias.toLowerCase(Locale.ENGLISH);
        TYPE_ALIASES.put(alias, value);
    }

    public <T> Class<T> resolveAlias(String alias) {
        alias = alias.toLowerCase(Locale.ENGLISH);
        return (Class<T>) TYPE_ALIASES.get(alias);
    }
}
