package com.ezreal.mybaits.test.dao;


import com.ezreal.mybaits.test.po.Activity;

public interface IActivityDao {

    Activity queryActivityById(Long activityId);

    Integer insert(Activity activity);

}
