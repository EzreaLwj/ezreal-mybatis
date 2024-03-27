package com.ezreal.mybaits.test.dao;

import com.ezreal.mybaits.test.po.User;

public interface IUserDao {

    User queryUserInfoById(Long uId);

    int updateUserInfo(User req);

    void insertUserInfo(User req);

    int deleteUserInfoByUserId(String userId);

}
