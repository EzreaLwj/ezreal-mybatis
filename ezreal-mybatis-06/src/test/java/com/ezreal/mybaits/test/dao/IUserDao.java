package com.ezreal.mybaits.test.dao;

import com.ezreal.mybaits.test.po.User;

public interface IUserDao {

    User queryUserInfoById(Long uId);

}
