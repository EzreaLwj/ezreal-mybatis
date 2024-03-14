package com.ezreal.mybatis.type;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @author Ezreal
 * @Date 2024/3/14
 */
public class StringTypeHandler extends BaseTypeHandler<String>{
    @Override
    protected void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType) throws SQLException {
        ps.setString(i, parameter);
    }
}
