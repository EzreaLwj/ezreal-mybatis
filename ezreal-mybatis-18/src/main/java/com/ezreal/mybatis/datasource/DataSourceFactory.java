package com.ezreal.mybatis.datasource;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * @author Ezreal
 * @Date 2024/3/6
 */
public interface DataSourceFactory {

    void setProperties(Properties properties);

    DataSource getDataSource();
}
