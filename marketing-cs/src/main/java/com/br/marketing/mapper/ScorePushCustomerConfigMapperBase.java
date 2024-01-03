package com.br.marketing.mapper;

import com.br.marketing.entity.ScorePushCustomerConfig;
import com.br.marketing.entity.ScorePushCustomerConfigExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ScorePushCustomerConfigMapperBase {
    /**
     * 查询
     * @param example example
     * @return int 查询的数据量
     */
    int countByExample(ScorePushCustomerConfigExample example);

    /**
     * 删除
     * @param example example
     * @return int 删除的数据量
     */
    int deleteByExample(ScorePushCustomerConfigExample example);

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
    int insert(ScorePushCustomerConfig record);

    /**
     * insert
     * @param record record
     * @return int 插入的数据量
     */
    int insertSelective(ScorePushCustomerConfig record);

    /**
     * 查询
     * @param example example
     * @return java.util.List<com.br.marketing.entity.ScorePushCustomerConfig> 查询到了ScorePushCustomerConfig集合
     */
    List<ScorePushCustomerConfig> selectByExample(ScorePushCustomerConfigExample example);

    /**
     * 查询
     * @param id id
     * @return com.br.marketing.entity.ScorePushCustomerConfig 查询到了ScorePushCustomerConfig对象
     */
    ScorePushCustomerConfig selectByPrimaryKey(Long id);

    /**
     * 更新
     * @param record record
     * @param example example
     * @return int 更新的数据量
     */
    int updateByExampleSelective(@Param("record") ScorePushCustomerConfig record, @Param("example") ScorePushCustomerConfigExample example);

    /**
     * 更新
     * @param record record
     * @param example example
     * @return int 更新的数据量
     */
    int updateByExample(@Param("record") ScorePushCustomerConfig record, @Param("example") ScorePushCustomerConfigExample example);

    /**
     * 更新
     * @param record record
     * @return int 更新的数据量
     */
    int updateByPrimaryKeySelective(ScorePushCustomerConfig record);

    /**
     * 更新
     * @param record record
     * @return int 更新的数据量
     */
    int updateByPrimaryKey(ScorePushCustomerConfig record);
}