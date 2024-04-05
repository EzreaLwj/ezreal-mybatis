package com.ezreal.mybatis.type;

import com.ezreal.mybatis.io.Resources;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 类型别名注册机
 *
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
        try {
            if (alias == null) {
                return null;
            }
            String key = alias.toLowerCase(Locale.ENGLISH);
            Class<T> value;
            if (TYPE_ALIASES.containsKey(key)) {
                value = (Class<T>) TYPE_ALIASES.get(key);
            } else {
                value = (Class<T>) Resources.classForName(alias);
            }
            return value;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Could not resolve type alias '" + alias + "'.  Cause: " + e, e);
        }

    }
}
