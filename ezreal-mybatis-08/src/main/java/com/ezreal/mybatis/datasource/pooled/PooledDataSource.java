package com.ezreal.mybatis.datasource.pooled;

import com.ezreal.mybatis.datasource.unpooled.UnpooledDataSource;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.sql.*;
import java.util.logging.Logger;

/**
 * @author Ezreal
 * @Date 2024/3/7
 */
public class PooledDataSource implements DataSource {

    private org.slf4j.Logger logger = LoggerFactory.getLogger(PooledDataSource.class);

    /**
     * 池状态
     */
    private final PooledState state = new PooledState(this);

    private final UnpooledDataSource dataSource;

    // 最大活跃链接数
    protected int poolMaximumActiveConnections = 10;
    // 最大空闲链接数
    protected int poolMaximumIdleConnections = 5;

    // 在被强制返回之前,池中连接被检查的时间
    protected int poolMaximumCheckoutTime = 20000;
    // 这是给连接池一个打印日志状态机会的低层次设置,还有重新尝试获得连接, 这些情况下往往需要很长时间 为了避免连接池没有配置时静默失败)。
    protected int poolTimeToWait = 20000;
    // 发送到数据的侦测查询,用来验证连接是否正常工作,并且准备 接受请求。默认是“NO PING QUERY SET” ,这会引起许多数据库驱动连接由一 个错误信息而导致失败
    protected String poolPingQuery = "NO PING QUERY SET";
    // 开启或禁用侦测查询
    protected boolean poolPingEnabled = false;
    // 用来配置 poolPingQuery 多次时间被用一次
    protected int poolPingConnectionsNotUsedFor = 0;

    private int expectedConnectionTypeCode;

    /**
     * 将链接放回连接池中
     *
     * @param connection
     */
    public void pushConnection(PooledConnection connection) throws SQLException {
        synchronized (state) {
            // 从活跃连接数之中移除
            state.activeConnections.remove(connection);
            if (connection.isValid()) {
                // 如果空闲连接数小于指定的阈值，就把真实的连接放入到空闲连接数
                if (state.idleConnections.size() < poolMaximumIdleConnections && expectedConnectionTypeCode == connection.getConnectionTypeCode()) {
                    state.accumulatedCheckoutTime += connection.getCheckoutTime();
                    if (!connection.getRealConnection().getAutoCommit()) {
                        connection.getRealConnection().rollback();
                    }

                    // 创建新的代理连接
                    PooledConnection newConnection = new PooledConnection(this, connection.getRealConnection());
                    state.idleConnections.add(newConnection);
                    newConnection.setCreatedTimestamp(System.currentTimeMillis());
                    newConnection.setLastUsedTimestamp(System.currentTimeMillis());
                    // 将该连接设置为无效
                    connection.invalidate();
                    logger.info("Returned connection " + newConnection.getRealHashCode() + " to pool.");
                    // 通知其他等待的线程来抢
                    state.notifyAll();
                } else {
                    // 否则空闲线程还比较充足
                    state.accumulatedCheckoutTime += connection.getCheckoutTimestamp();
                    if (!connection.getRealConnection().getAutoCommit()) {
                        connection.getRealConnection().rollback();
                    }
                    //关闭连接
                    connection.getRealConnection().close();
                    logger.info("Closed connection " + connection.getRealHashCode() + ".");
                    connection.invalidate();
                }
            } else {
                logger.info("A bad connection (" + connection.getRealHashCode() + ") attempted to return to the pool, discarding connection.");
                state.badConnectionCount++;
            }
        }
    }

    /**
     * 从连接池中获取连接
     *
     * @return
     */
    public PooledConnection popConnection(String username, String password) throws SQLException {

        PooledConnection connection = null;
        boolean countedWait = false;
        long t = System.currentTimeMillis();
        int localBadConnectionCount = 0;

        while (connection == null) {
            synchronized (state) {

                // 如果存在空闲连接
                if (!state.idleConnections.isEmpty()) {
                    connection = state.idleConnections.remove(0);
                    logger.info("Checked out connection " + connection.getRealHashCode() + " from pool.");
                } else {

                    // 如果不存在空闲连接
                    // 活跃连接数小于最大指定值
                    if (state.activeConnections.size() < poolMaximumActiveConnections) {
                        connection = new PooledConnection(this, dataSource.getConnection());
                        logger.info("Created connection " + connection.getRealHashCode() + ".");
                    } else {

                        // 活跃连接数已满
                        // 获取最老的活跃连接
                        PooledConnection oldestPooledConnection = state.activeConnections.get(0);
                        long longestCheckoutTime = oldestPooledConnection.getCheckoutTime();
                        // 如果checkout时间过长，则这个链接标记为过期
                        if (longestCheckoutTime > poolMaximumCheckoutTime) {
                            state.claimedOverdueConnectionCount++;
                            state.accumulatedCheckoutTimeOfOverdueConnections += longestCheckoutTime;
                            state.accumulatedCheckoutTime += longestCheckoutTime;
                            state.activeConnections.remove(oldestPooledConnection);
                            if (!oldestPooledConnection.getRealConnection().getAutoCommit()) {
                                oldestPooledConnection.getRealConnection().rollback();
                            }

                            connection = new PooledConnection(this, oldestPooledConnection.getRealConnection());
                            oldestPooledConnection.invalidate();
                            logger.info("Claimed overdue connection " + connection.getRealHashCode() + ".");
                        } else {
                            // 如果checkout的时间不长，就让当前请求去等待
                            try {
                                if (!countedWait) {
                                    state.hadToWaitCount++;
                                    countedWait = true;
                                }
                                logger.info("Waiting as long as " + poolTimeToWait + " milliseconds for connection.");
                                long wt = System.currentTimeMillis();
                                state.wait(poolTimeToWait);
                                state.accumulatedWaitTime += System.currentTimeMillis() - wt;
                            } catch (Exception e) {
                                break;
                            }
                        }
                    }
                }

                // 获得到连接
                if (connection != null) {
                    if (connection.isValid()) {
                        if (!connection.getRealConnection().getAutoCommit()) {
                            connection.getRealConnection().rollback();
                        }
                        connection.setConnectionTypeCode(assembleConnectionTypeCode(dataSource.getUrl(), username, password));
                        // 记录checkout时间
                        connection.setCheckoutTimestamp(System.currentTimeMillis());
                        connection.setLastUsedTimestamp(System.currentTimeMillis());
                        state.activeConnections.add(connection);
                        state.requestCount++;
                        state.accumulatedRequestTime += System.currentTimeMillis() - t;

                    } else {
                        logger.info("A bad connection (" + connection.getRealHashCode() + ") was returned from the pool, getting another connection.");
                        // 如果没拿到，统计信息：失败链接 +1
                        state.badConnectionCount++;
                        localBadConnectionCount++;
                        connection = null;
                        // 失败次数较多，抛异常
                        if (localBadConnectionCount > (poolMaximumIdleConnections + 3)) {
                            logger.debug("PooledDataSource: Could not get a good connection to the database.");
                            throw new SQLException("PooledDataSource: Could not get a good connection to the database.");
                        }
                    }
                }
            }
        }

        if (connection == null) {
            logger.debug("PooledDataSource: Unknown severe error condition.  The connection pool returned a null connection.");
            throw new SQLException("PooledDataSource: Unknown severe error condition.  The connection pool returned a null connection.");
        }

        return connection;
    }

    /**
     * 强行关闭所有连接
     */
    public void forceCloseAll() {
        synchronized (state) {
            for (int i = state.activeConnections.size() - 1; i >= 0; i--) {
                try {
                    PooledConnection pooledConnection = state.activeConnections.remove(i);
                    pooledConnection.invalidate();
                    if (!pooledConnection.getRealConnection().getAutoCommit()) {
                        pooledConnection.getRealConnection().rollback();
                    }
                    pooledConnection.getRealConnection().close();
                } catch (SQLException ignore) {

                }
            }

            for (int i = state.idleConnections.size() - 1; i >= 0; i--) {
                try {
                    PooledConnection pooledConnection = state.activeConnections.remove(i);
                    pooledConnection.invalidate();

                    if (pooledConnection.getRealConnection().getAutoCommit()) {
                        pooledConnection.getRealConnection().rollback();
                    }
                    pooledConnection.getRealConnection().close();
                } catch (SQLException ignore) {

                }
            }
        }
    }



    private int assembleConnectionTypeCode(String url, String username, String password) {
        return ("" + url + username + password).hashCode();
    }

    protected boolean pingConnection(PooledConnection conn) {
        boolean result = true;
        try {
            result = !conn.getRealConnection().isClosed();
        } catch (SQLException e) {
            logger.info("Connection " + conn.getRealHashCode() + " is BAD: " + e.getMessage());
            result = false;
        }

        if (result) {
            if (poolPingEnabled) {
                if (poolPingConnectionsNotUsedFor >= 0 && conn.getTimeElapsedSinceLastUse() > poolPingConnectionsNotUsedFor) {
                    try {
                        Connection realConnection = conn.getRealConnection();
                        Statement statement = realConnection.createStatement();
                        ResultSet resultSet = statement.executeQuery(poolPingQuery);
                        resultSet.close();
                        if (!realConnection.getAutoCommit()) {
                            realConnection.rollback();
                        }
                        result = true;
                    } catch (SQLException e) {
                        logger.info("Execution of ping query '" + poolPingQuery + "' failed: " + e.getMessage());
                        try {
                            conn.getRealConnection().close();
                        } catch (SQLException ignore) {
                        }
                        result = false;
                        logger.info("Connection " + conn.getRealHashCode() + " is BAD: " + e.getMessage());
                    }
                }
            }
        }

        return result;
    }

    public static Connection unwrapConnection(Connection conn) {
        if (Proxy.isProxyClass(conn.getClass())) {
            InvocationHandler handler = Proxy.getInvocationHandler(conn);
            if (handler instanceof PooledConnection) {
                return ((PooledConnection) handler).getRealConnection();
            }
        }
        return conn;
    }

    public PooledDataSource() {
        this.dataSource = new UnpooledDataSource();
    }

    @Override
    public Connection getConnection() throws SQLException {
        return popConnection(dataSource.getUsername(), dataSource.getPassword()).getProxyConnection();
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return popConnection(username, password).getProxyConnection();
    }

    @Override
    protected void finalize() throws Throwable {
        forceCloseAll();
        super.finalize();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new SQLException(getClass().getName() + " is not a wrapper.");
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return DriverManager.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        DriverManager.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        DriverManager.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return DriverManager.getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    }

    public void setDriver(String driver) {
        dataSource.setDriver(driver);
        forceCloseAll();
    }

    public void setUrl(String url) {
        dataSource.setUrl(url);
        forceCloseAll();
    }

    public void setUsername(String username) {
        dataSource.setUsername(username);
        forceCloseAll();
    }

    public void setPassword(String password) {
        dataSource.setPassword(password);
        forceCloseAll();
    }

    public void setDefaultAutoCommit(boolean defaultAutoCommit) {
        dataSource.setAutoCommit(defaultAutoCommit);
        forceCloseAll();
    }

    public int getPoolMaximumActiveConnections() {
        return poolMaximumActiveConnections;
    }

    public void setPoolMaximumActiveConnections(int poolMaximumActiveConnections) {
        this.poolMaximumActiveConnections = poolMaximumActiveConnections;
    }

    public int getPoolMaximumIdleConnections() {
        return poolMaximumIdleConnections;
    }

    public void setPoolMaximumIdleConnections(int poolMaximumIdleConnections) {
        this.poolMaximumIdleConnections = poolMaximumIdleConnections;
    }

    public int getPoolMaximumCheckoutTime() {
        return poolMaximumCheckoutTime;
    }

    public void setPoolMaximumCheckoutTime(int poolMaximumCheckoutTime) {
        this.poolMaximumCheckoutTime = poolMaximumCheckoutTime;
    }

    public int getPoolTimeToWait() {
        return poolTimeToWait;
    }

    public void setPoolTimeToWait(int poolTimeToWait) {
        this.poolTimeToWait = poolTimeToWait;
    }

    public String getPoolPingQuery() {
        return poolPingQuery;
    }

    public void setPoolPingQuery(String poolPingQuery) {
        this.poolPingQuery = poolPingQuery;
    }

    public boolean isPoolPingEnabled() {
        return poolPingEnabled;
    }

    public void setPoolPingEnabled(boolean poolPingEnabled) {
        this.poolPingEnabled = poolPingEnabled;
    }

    public int getPoolPingConnectionsNotUsedFor() {
        return poolPingConnectionsNotUsedFor;
    }

    public void setPoolPingConnectionsNotUsedFor(int poolPingConnectionsNotUsedFor) {
        this.poolPingConnectionsNotUsedFor = poolPingConnectionsNotUsedFor;
    }

    public int getExpectedConnectionTypeCode() {
        return expectedConnectionTypeCode;
    }

    public void setExpectedConnectionTypeCode(int expectedConnectionTypeCode) {
        this.expectedConnectionTypeCode = expectedConnectionTypeCode;
    }
}
