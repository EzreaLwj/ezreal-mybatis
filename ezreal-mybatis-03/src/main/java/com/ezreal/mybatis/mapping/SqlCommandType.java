package com.ezreal.mybatis.mapping;

/**
 * SQL语句类型
 * @author Ezreal
 * @Date 2024/3/5
 */
public enum SqlCommandType {

    /**
     * 未知
     */
    UNKNOWN,
    /**
     * 插入
     */
    INSERT,
    /**
     * 更新
     */
    UPDATE,
    /**
     * 删除
     */
    DELETE,
    /**
     * 查找
     */
    SELECT;
}
