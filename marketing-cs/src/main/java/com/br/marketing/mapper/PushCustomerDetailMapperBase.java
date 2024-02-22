package com.br.marketing.mapper;

import com.br.marketing.entity.PushCustomerDetail;
import com.br.marketing.entity.PushCustomerDetailExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PushCustomerDetailMapperBase {
    /**
     * 查询到的总量
     * @param example example
     * @return int 查询到的总量
     */
    int countByExample(PushCustomerDetailExample example);

    /**
     * 删除
     * @param example example
     * @return int 删除的总量
     */
    int deleteByExample(PushCustomerDetailExample example);

    /**
     * 删除
     * @param id id
     * @return int 删除的总量
     */
    int deleteByPrimaryKey(Long id);

    /**
     * insert
     * @param record record
     * @return int 插入的总量
     */
    int insert(PushCustomerDetail record);

    /**
     * insert
     * @param record record
     * @return int 插入的总量
     */
    int insertSelective(PushCustomerDetail record);

    /**
     * 查询
     * @param example example
     * @return java.util.List<com.br.marketing.entity.PushCustomerDetail> 查询到的结果
     */
    List<PushCustomerDetail> selectByExample(PushCustomerDetailExample example);

    /**
     * 查询
     * @param id id
     * @return com.br.marketing.entity.PushCustomerDetail 查询到的PushCustomerDetail对象
     */
    PushCustomerDetail selectByPrimaryKey(Long id);

    /**
     * 更新
     * @param record record
     * @param example example
     * @return int 更新的结果
     */
    int updateByExampleSelective(@Param("record") PushCustomerDetail record, @Param("example") PushCustomerDetailExample example);

    /**
     * 更新
     * @param record record
     * @param example example
     * @return int 更新的结果
     */
    int updateByExample(@Param("record") PushCustomerDetail record, @Param("example") PushCustomerDetailExample example);

    /**
     * 更新
     * @param record record
     * @return int 更新的结果
     */
    int updateByPrimaryKeySelective(PushCustomerDetail record);

    /**
     * 更新
     * @param record record
     * @return int 更新的结果
     */
    int updateByPrimaryKey(PushCustomerDetail record);
}