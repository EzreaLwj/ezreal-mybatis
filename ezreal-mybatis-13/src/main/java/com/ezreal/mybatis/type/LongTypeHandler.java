package com.ezreal.mybatis.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * long类型处理器
 * @author Ezreal
 * @Date 2024/3/14
 */
public class LongTypeHandler extends BaseTypeHandler<Long>{

    @Override
    protected void setNonNullParameter(PreparedStatement ps, int i, Long parameter, JdbcType jdbcType) throws SQLException {
        ps.setLong(i, parameter);
    }

    @Override
    protected Long getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return rs.getLong(columnName);
    }
}
