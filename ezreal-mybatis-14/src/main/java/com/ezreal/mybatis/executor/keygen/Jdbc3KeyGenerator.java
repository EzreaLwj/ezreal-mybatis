package com.ezreal.mybatis.executor.keygen;

import com.ezreal.mybatis.executor.Executor;
import com.ezreal.mybatis.mapping.MappedStatement;

import java.sql.Statement;

/**
 * @author Ezreal
 * @Date 2024/4/3
 */
public class Jdbc3KeyGenerator implements KeyGenerator{

    @Override
    public void processBefore(Executor executor, MappedStatement ms, Statement stmt, Object parameter) {

    }

    @Override
    public void processAfter(Executor executor, MappedStatement ms, Statement stmt, Object parameter) {

    }
}
