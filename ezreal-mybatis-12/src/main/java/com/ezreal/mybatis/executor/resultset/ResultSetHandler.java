package com.ezreal.mybatis.executor.resultset;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * 结果处理器接口
 * @author Ezreal
 * @Date 2024/3/8
 */
public interface ResultSetHandler {

    <T> List<T> handleResultSets(Statement statement) throws SQLException;
}
