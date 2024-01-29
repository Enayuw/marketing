package com.br.marketing.mapper;

import com.br.marketing.entity.TongChengAgent;
import com.br.marketing.entity.TongChengAgentExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TongChengAgentMapper {
    int countByExample(TongChengAgentExample example);

    int deleteByExample(TongChengAgentExample example);

    int deleteByPrimaryKey(Long id);

    int insert(TongChengAgent record);

    int insertSelective(TongChengAgent record);

    List<TongChengAgent> selectByExampleWithBLOBs(TongChengAgentExample example);

    List<TongChengAgent> selectByExample(TongChengAgentExample example);

    TongChengAgent selectByPrimaryKey(Long id);

    TongChengAgent selectByMobileMd5(String mobileMd5);

    int updateByExampleSelective(@Param("record") TongChengAgent record, @Param("example") TongChengAgentExample example);

    int updateByExampleWithBLOBs(@Param("record") TongChengAgent record, @Param("example") TongChengAgentExample example);

    int updateByExample(@Param("record") TongChengAgent record, @Param("example") TongChengAgentExample example);

    int updateByPrimaryKeySelective(TongChengAgent record);

    int updateByPrimaryKeyWithBLOBs(TongChengAgent record);

    int updateByPrimaryKey(TongChengAgent record);

    List<TongChengAgent> tongChengGroupOperationDataPage(@Param("localId") Long localId, @Param("minId") Long minId);

}