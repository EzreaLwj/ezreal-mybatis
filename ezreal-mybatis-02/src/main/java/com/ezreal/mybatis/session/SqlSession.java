package com.ezreal.mybatis.session;

/**
 * @author Ezreal
 * @Date 2024/3/4
 */
public interface SqlSession {

    /**
     * 根据指定的SqlID获取一条记录的封装对象
     *
     * @param statement sqlId
     * @param <T>       the returned object type 封装之后的对象类型
     * @return Mapped object 封装之后的对象
     */
    <T> T selectOne(String statement);


    /**
     * 根据指定的SqlID获取一条记录的封装对象
     *
     * @param statement sqlId
     * @param parameter 参数
     * @param <T>       the returned object type 封装之后的对象类型
     * @return Mapped object 封装之后的对象
     */
    <T> T selectOne(String statement, Object parameter);

    /**
     * Retrieves a mapper.
     * 得到映射器，这个巧妙的使用了泛型，使得类型安全
     *
     * @param <T>  the mapper type
     * @param type Mapper interface class
     * @return a mapper bound to this SqlSession
     */
    <T> T getMapper(Class<T> type);

}
