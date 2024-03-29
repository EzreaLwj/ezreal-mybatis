package com.ezreal.mybaits.test.dao;

import com.ezreal.mybaits.test.po.User;
import com.ezreal.mybatis.annotations.Insert;
import com.ezreal.mybatis.annotations.Select;
import com.ezreal.mybatis.annotations.Update;

import java.util.List;

public interface IUserDao {

    @Select("SELECT id, userId, userName, userHead\n" +
            "FROM user\n" +
            "where id = #{id}")
    User queryUserInfoById(Long uId);

    @Select("SELECT id, userId, userName, userHead\n" +
            "        FROM user\n" +
            "        where id = #{id}")
    User queryUserInfo(User req);

    @Select("SELECT id, userId, userName, userHead\n" +
            "FROM user")
    List<User> queryUserInfoList();

    @Update("UPDATE user\n" +
            "SET userName = #{userName}\n" +
            "WHERE id = #{id}")
    int updateUserInfo(User req);

    @Insert("INSERT INTO user\n" +
            "(userId, userName, userHead, createTime, updateTime)\n" +
            "VALUES (#{userId}, #{userName}, #{userHead}, now(), now())")
    void insertUserInfo(User req);

    @Insert("DELETE FROM user WHERE userId = #{userId}")
    int deleteUserInfoByUserId(String userId);

}
