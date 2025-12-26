package com.br.marketing.mapper;

import com.br.marketing.entity.XieChengBlackList;
import com.br.marketing.entity.XieChengBlackListExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface XieChengBlackListMapper {
    int countByExample(XieChengBlackListExample example);

    int deleteByExample(XieChengBlackListExample example);

    int deleteByPrimaryKey(Long id);

    int insert(XieChengBlackList record);

    int insertSelective(XieChengBlackList record);

    List<XieChengBlackList> selectByExample(XieChengBlackListExample example);

    XieChengBlackList selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") XieChengBlackList record, @Param("example") XieChengBlackListExample example);

    int updateByExample(@Param("record") XieChengBlackList record, @Param("example") XieChengBlackListExample example);

    int updateByPrimaryKeySelective(XieChengBlackList record);

    int updateByPrimaryKey(XieChengBlackList record);

    List<XieChengBlackList> selectByPage(@Param("minId") Long minId, @Param("pageSize") Integer pageSize);

    List<XieChengBlackList> selectCellsByPage(@Param("minId") Long minId, @Param("pageSize") Integer pageSize);

    List<String> selectByBlackListIdsFromScoreFiletikv_(@Param("querySql") String querySql);

    List<Long> selectIdsByBatchNumberAndCondition(@Param("delTableName") String delTableName,
                                                       @Param("labelType") Integer labelType,
                                                       @Param("batchNumber") String batchNumber,
                                                       @Param("condition") String condition,
                                                       @Param("minId") Long minId);

    int updateIsDeleteByIds(@Param("delTableName") String delTableName, @Param("ids") List<Long> ids, @Param("extend") String extend);

}