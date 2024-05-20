package com.br.marketing.mapper;

import com.br.marketing.dto.tongcheng.TongChengPushQueryQuantityDTO;
import com.br.marketing.entity.TongChengAgent;
import com.br.marketing.entity.TongChengAgentExample;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

public interface TongChengAgentMapper {
    int countByExample(TongChengAgentExample example);

    int deleteByExample(TongChengAgentExample example);

    int deleteByPrimaryKey(Long id);

    int insert(TongChengAgent record);

    int insertSelective(TongChengAgent record);

    List<TongChengAgent> selectByExample(TongChengAgentExample example);

    TongChengAgent selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") TongChengAgent record, @Param("example") TongChengAgentExample example);

    int updateByExample(@Param("record") TongChengAgent record, @Param("example") TongChengAgentExample example);

    int updateByPrimaryKeySelective(TongChengAgent record);

    int updateByPrimaryKey(TongChengAgent record);

    List<TongChengAgent> tongChengGroupOperationDataPage(@Param("minId") Long minId, @Param("apiCode") String apiCode, @Param("num") int num);

    List<Map<String, Object>> queryQuantityGroupByLocalId(TongChengPushQueryQuantityDTO params);
    List<Long> queryLocalFileIdList(TongChengPushQueryQuantityDTO params);

}