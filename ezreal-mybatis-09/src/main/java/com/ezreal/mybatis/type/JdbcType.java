package com.ezreal.mybatis.type;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

/**
 * jdbc类型枚举
 *
 * @author Ezreal
 * @Date 2024/3/12
 */
public enum JdbcType {

    INTERGE(Types.INTEGER),
    FLOAT(Types.FLOAT),
    DOUBLE(Types.DOUBLE),
    DECIMAL(Types.DECIMAL),
    VARCHAR(Types.VARCHAR),
    TIMESTAMP(Types.TIMESTAMP),

    CHAR(Types.CHAR);

    public final int TYPE_CODE;

    JdbcType(int code) {
        this.TYPE_CODE = code;
    }

    private static Map<Integer, JdbcType> codeLookup = new HashMap<>();

    static {
        for (JdbcType type : JdbcType.values()) {
            codeLookup.put(type.TYPE_CODE, type);
        }
    }

    public static JdbcType forCode(int code) {
        return codeLookup.get(code);
    }
}
