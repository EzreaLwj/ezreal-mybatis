package com.ezreal.mybatis.datasource.pooled;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * 池化链接
 *
 * @author Ezreal
 * @Date 2024/3/7
 */
public class PooledConnection implements InvocationHandler {

    private static final String CLOSE = "close";

    private static final Class<?>[] IFACES = new Class<?>[]{Connection.class};

    private int hashcode = 0;

    private PooledDataSource dataSource;

    // 真实的链接
    private Connection realConnection;

    // 代理的链接
    private Connection proxyConnection;

    private long checkoutTimestamp;
    private long createdTimestamp;
    private long lastUsedTimestamp;
    private int connectionTypeCode;
    private boolean valid;

    public PooledConnection(PooledDataSource dataSource, Connection realConnection) {
        this.hashcode = realConnection.hashCode();
        this.dataSource = dataSource;
        this.realConnection = realConnection;
        this.createdTimestamp = System.currentTimeMillis();
        this.lastUsedTimestamp = System.currentTimeMillis();
        // 设置为有效链接
        this.valid = true;
        // 创建代理连接
        this.proxyConnection = (Connection) Proxy.newProxyInstance(Connection.class.getClassLoader(), IFACES, this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        if (CLOSE.hashCode() == methodName.hashCode() && methodName.equals(CLOSE)) {
            dataSource.pushConnection(this);
            return null;
        } else {
            if (!Object.class.equals(method.getDeclaringClass())) {
                // 处理toString方法，其他方法在调用时都要检查连接是否有效
                checkConnection();
            }
            // 然后执行原来的方法
            return method.invoke(realConnection, args);
        }
    }

    private void checkConnection() throws SQLException {
        if (!valid) {
            throw new SQLException("Error accessing PooledConnection. Connection is invalid.");
        }
    }

    public void invalidate() {
        valid = false;
    }

    public boolean isValid() {
        return valid && realConnection != null
                && dataSource.pingConnection(this);
    }

    public Connection getRealConnection() {
        return realConnection;
    }

    public Connection getProxyConnection() {
        return proxyConnection;
    }

    public int getRealHashCode() {
        return realConnection == null ? 0 : realConnection.hashCode();
    }

    public int getConnectionTypeCode() {
        return connectionTypeCode;
    }

    public void setConnectionTypeCode(int connectionTypeCode) {
        this.connectionTypeCode = connectionTypeCode;
    }

    public long getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(long createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public long getLastUsedTimestamp() {
        return lastUsedTimestamp;
    }

    public void setLastUsedTimestamp(long lastUsedTimestamp) {
        this.lastUsedTimestamp = lastUsedTimestamp;
    }

    public long getTimeElapsedSinceLastUse() {
        return System.currentTimeMillis() - lastUsedTimestamp;
    }

    public long getAge() {
        return System.currentTimeMillis() - createdTimestamp;
    }

    public long getCheckoutTimestamp() {
        return checkoutTimestamp;
    }

    public void setCheckoutTimestamp(long timestamp) {
        this.checkoutTimestamp = timestamp;
    }

    public long getCheckoutTime() {
        return System.currentTimeMillis() - checkoutTimestamp;
    }

    @Override
    public int hashCode() {
        return hashcode;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PooledConnection) {
            return realConnection.hashCode() == (((PooledConnection) obj).realConnection.hashCode());
        } else if (obj instanceof Connection) {
            return hashcode == obj.hashCode();
        } else {
            return false;
        }
    }

}
