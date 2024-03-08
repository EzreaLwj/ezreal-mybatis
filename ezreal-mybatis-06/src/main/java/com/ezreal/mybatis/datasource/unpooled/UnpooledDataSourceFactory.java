package com.ezreal.mybatis.datasource.unpooled;

import com.ezreal.mybatis.datasource.DataSourceFactory;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * @author Ezreal
 * @Date 2024/3/7
 */
public class UnpooledDataSourceFactory implements DataSourceFactory {

    protected Properties properties;


    @Override
    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    @Override
    public DataSource getDataSource() {
        UnpooledDataSource unpooledDataSource = new UnpooledDataSource();
        unpooledDataSource.setUsername(properties.getProperty("username"));
        unpooledDataSource.setPassword(properties.getProperty("password"));
        unpooledDataSource.setUrl(properties.getProperty("url"));
        unpooledDataSource.setDriver(properties.getProperty("driver"));
        return unpooledDataSource;
    }

}
