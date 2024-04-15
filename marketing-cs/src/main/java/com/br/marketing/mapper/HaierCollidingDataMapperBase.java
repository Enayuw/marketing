package com.br.marketing.mapper;

import java.util.List;

import com.br.marketing.entity.HaierCollidingData;
import com.br.marketing.entity.HaierCollidingDataExample;
import org.apache.ibatis.annotations.Param;

public interface HaierCollidingDataMapperBase {
    /**
     * 查询数据量
     * @param example example
     * @return int 查询数据量
     */
    int countByExample(HaierCollidingDataExample example);

    /**
     * 删除
     * @param example example
     * @return int 删除数据量
     */
    int deleteByExample(HaierCollidingDataExample example);

    /**
     * 删除
     * @param id id
     * @return int 删除数据量
     */
    int deleteByPrimaryKey(Long id);

    /**
     * insert
     * @param record record
     * @return int 插入数据量
     */
    int insert(HaierCollidingData record);

    /**
     * insert
     * @param record record
     * @return int 插入数据量
     */
    int insertSelective(HaierCollidingData record);

    /**
     * 查询
     * @param example example
     * @return java.util.List<com.br.marketing.entity.HaierCollidingData> 查询到的数据
     */
    List<HaierCollidingData> selectByExampleWithBLOBs(HaierCollidingDataExample example);

    /**
     * 查询
     * @param example example
     * @return java.util.List<com.br.marketing.entity.HaierCollidingData> 查询到的数据
     */
    List<HaierCollidingData> selectByExample(HaierCollidingDataExample example);

    /**
     * 查询
     * @param id id
     * @return com.br.marketing.entity.HaierCollidingData 查询到的对象
     */
    HaierCollidingData selectByPrimaryKey(Long id);

    /**
     * 更新
     * @param record record
     * @param example example
     * @return int 更新数据量
     */
    int updateByExampleSelective(@Param("record") HaierCollidingData record, @Param("example") HaierCollidingDataExample example);

    /**
     * 更新
     * @param record record
     * @param example example
     * @return int 更新数据量
     */
    int updateByExampleWithBLOBs(@Param("record") HaierCollidingData record, @Param("example") HaierCollidingDataExample example);

    /**
     * 更新
     * @param record record
     * @param example example
     * @return int 更新数据量
     */
    int updateByExample(@Param("record") HaierCollidingData record, @Param("example") HaierCollidingDataExample example);

    /**
     * 更新
     * @param record record
     * @return int 更新数据量
     */
    int updateByPrimaryKeySelective(HaierCollidingData record);

    /**
     * 更新
     * @param record record
     * @return int 更新数据量
     */
    int updateByPrimaryKeyWithBLOBs(HaierCollidingData record);

    /**
     * 更新
     * @param record record
     * @return int 更新数据量
     */
    int updateByPrimaryKey(HaierCollidingData record);
}