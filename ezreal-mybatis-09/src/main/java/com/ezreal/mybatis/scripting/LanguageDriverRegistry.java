package com.ezreal.mybatis.scripting;

import java.util.HashMap;
import java.util.Map;

/**
 * 脚本语言注册器
 *
 * @author Ezreal
 * @Date 2024/3/12
 */
public class LanguageDriverRegistry  {

    private final Map<Class<?>, LanguageDriver> LANGUAGE_DRIVER_MAP = new HashMap<>();

    private Class<?> defaultDriverClass = null;

    public void register(Class<?> cls) {

        if (cls == null) {
            throw new IllegalArgumentException("null is not a valid Language Driver");
        }
        if (!LanguageDriver.class.isAssignableFrom(cls)) {
            throw new RuntimeException(cls.getName() + " does not implements " + LanguageDriver.class.getName());
        }

        LanguageDriver languageDriver = LANGUAGE_DRIVER_MAP.get(cls);
        if (languageDriver == null) {
            try {
                languageDriver = (LanguageDriver) cls.newInstance();
                LANGUAGE_DRIVER_MAP.put(cls, languageDriver);
            } catch (Exception ex) {
                throw new RuntimeException("Failed to load language driver for " + cls.getName(), ex);
            }
        }
    }

    public LanguageDriver getDriver(Class<?> cls) {
        return LANGUAGE_DRIVER_MAP.get(cls);
    }

    public Class<?> getDefaultDriverClass() {
        return defaultDriverClass;
    }

    public void setDefaultDriverClass(Class<?> defaultDriverClass) {
        this.defaultDriverClass = defaultDriverClass;
        register(defaultDriverClass);
    }
}
