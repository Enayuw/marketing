package com.br.marketing.mapper;

import com.br.marketing.entity.RequestInterfaceLog;
import com.br.marketing.entity.RequestInterfaceLogExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RequestInterfaceLogMapperBase {
    /**
     * 查询
     * @param example example
     * @return int 查询到的数据量
     */
    int countByExample(RequestInterfaceLogExample example);

    /**
     * 删除
     * @param example example
     * @return int 删除的数据量
     */
    int deleteByExample(RequestInterfaceLogExample example);

    /**
     * 删除
     * @param id id
     * @return int 删除的数据量
     */
    int deleteByPrimaryKey(Long id);

    /**
     * insert
     * @param record record
     * @return int 插入的数据量
     */
    int insert(RequestInterfaceLog record);

    /**
     * insert
     * @param record record
     * @return int 插入的数据量
     */
    int insertSelective(RequestInterfaceLog record);

    /**
     * 查询
     * @param example example
     * @return java.util.List<com.br.marketing.entity.RequestInterfaceLog> 查询到了RequestInterfaceLog集合
     */
    List<RequestInterfaceLog> selectByExample(RequestInterfaceLogExample example);

    /**
     * 查询
     * @param id id
     * @return com.br.marketing.entity.RequestInterfaceLog 查询到了RequestInterfaceLog对象
     */
    RequestInterfaceLog selectByPrimaryKey(Long id);

    /**
     * 更新
     * @param record record
     * @param example example
     * @return int 更新的数据量
     */
    int updateByExampleSelective(@Param("record") RequestInterfaceLog record, @Param("example") RequestInterfaceLogExample example);

    /**
     * 更新
     * @param record record
     * @param example example
     * @return int 更新的数据量
     */
    int updateByExample(@Param("record") RequestInterfaceLog record, @Param("example") RequestInterfaceLogExample example);

    /**
     * 更新
     * @param record record
     * @return int 更新的数据量
     */
    int updateByPrimaryKeySelective(RequestInterfaceLog record);

    /**
     * 更新
     * @param record record
     * @return int 更新的数据量
     */
    int updateByPrimaryKey(RequestInterfaceLog record);
}