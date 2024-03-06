package com.ezreal.mybatis.session;

/**
 * @author Ezreal
 * @Date 2024/3/4
 */
public interface SqlSessionFactory {

    /**
     * 开启SqlSession
     *
     * @return SqlSession
     */
    SqlSession openSession();
}
