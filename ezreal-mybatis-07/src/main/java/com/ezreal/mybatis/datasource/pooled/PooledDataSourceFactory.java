package com.ezreal.mybatis.datasource.pooled;

import com.ezreal.mybatis.datasource.unpooled.UnpooledDataSourceFactory;

import javax.sql.DataSource;

/**
 * @author Ezreal
 * @Date 2024/3/8
 */
public class PooledDataSourceFactory extends UnpooledDataSourceFactory {
    public PooledDataSourceFactory() {
        this.dataSource = new PooledDataSource();
    }
}
