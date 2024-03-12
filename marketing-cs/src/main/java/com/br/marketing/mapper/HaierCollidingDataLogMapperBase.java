package com.br.marketing.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.br.marketing.entity.HaierCollidingDataLog;
import com.br.marketing.entity.HaierCollidingDataLogExample;

public interface HaierCollidingDataLogMapperBase {

    /**
     * count
     * @param example example
     * @return int 查询的数据量
     */
    int countByExample(HaierCollidingDataLogExample example);

    /**
     * 删除
     * @param example example
     * @return int 删除的数据量
     */
    int deleteByExample(HaierCollidingDataLogExample example);

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
    int insert(HaierCollidingDataLog record);

    /**
     * insert
     * @param record record
     * @return int 插入的数据量
     */
    int insertSelective(HaierCollidingDataLog record);

    /**
     * 查询
     * @param example example
     * @return java.util.List<com.br.marketing.entity.HaierCollidingDataLog> 查询到的结果
     */
    List<HaierCollidingDataLog> selectByExampleWithBLOBs(HaierCollidingDataLogExample example);

    /**
     * 查询
     * @param example example
     * @return java.util.List<com.br.marketing.entity.HaierCollidingDataLog> 查询到的结果
     */
    List<HaierCollidingDataLog> selectByExample(HaierCollidingDataLogExample example);

    /**
     * 查询数据
     * @param id id
     * @return com.br.marketing.entity.HaierCollidingDataLog 查询到的对象
     */
    HaierCollidingDataLog selectByPrimaryKey(Long id);

    /**
     * 更新
     * @param record record
     * @param example example
     * @return int 更新数量
     */
    int updateByExampleSelective(@Param("record") HaierCollidingDataLog record, @Param("example") HaierCollidingDataLogExample example);

    /**
     * 更新
     * @param record record
     * @param example example
     * @return int 更新数量
     */
    int updateByExampleWithBLOBs(@Param("record") HaierCollidingDataLog record, @Param("example") HaierCollidingDataLogExample example);

    /**
     * 更新
     * @param record record
     * @param example example
     * @return int 更新数量
     */
    int updateByExample(@Param("record") HaierCollidingDataLog record, @Param("example") HaierCollidingDataLogExample example);

    /**
     * 更新
     * @param record record
     * @return int 更新数量
     */
    int updateByPrimaryKeySelective(HaierCollidingDataLog record);

    /**
     * 更新
     * @param record record
     * @return int 更新数量
     */
    int updateByPrimaryKeyWithBLOBs(HaierCollidingDataLog record);

    /**
     * 更新
     * @param record record
     * @return int 更新数量
     */
    int updateByPrimaryKey(HaierCollidingDataLog record);
}