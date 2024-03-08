package com.ezreal.mybatis.datasource.druid;

import com.alibaba.druid.pool.DruidDataSource;
import com.ezreal.mybatis.datasource.DataSourceFactory;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * @author Ezreal
 * @Date 2024/3/6
 */
public class DruidDataSourceFactory implements DataSourceFactory {

    private Properties properties;

    @Override
    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    @Override
    public DataSource getDataSource() {

        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setUsername(properties.getProperty("username"));
        dataSource.setPassword(properties.getProperty("password"));
        dataSource.setDriverClassName(properties.getProperty("driver"));
        dataSource.setUrl(properties.getProperty("url"));
        return dataSource;
    }
}
