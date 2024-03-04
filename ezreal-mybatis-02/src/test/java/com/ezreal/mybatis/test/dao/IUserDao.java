package com.ezreal.mybatis.test.dao;

/**
 * @author Ezreal
 * @Date 2024/3/4
 */
public interface IUserDao {

    String queryUserName(String uid);

    Integer queryUserAge(String uid);
}
