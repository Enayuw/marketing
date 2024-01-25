package com.br.marketing.mapper;

import com.br.marketing.entity.TongChengUndoData;
import com.br.marketing.entity.TongchengAgent;
import com.br.marketing.entity.TongchengAgentExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface TongchengAgentMapper {
    int countByExample(TongchengAgentExample example);

    int deleteByExample(TongchengAgentExample example);

    int deleteByPrimaryKey(Long id);

    int insert(TongchengAgent record);

    int insertSelective(TongchengAgent record);

    List<TongchengAgent> selectByExampleWithBLOBs(TongchengAgentExample example);

    List<TongchengAgent> selectByExample(TongchengAgentExample example);

    TongchengAgent selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") TongchengAgent record, @Param("example") TongchengAgentExample example);

    int updateByExampleWithBLOBs(@Param("record") TongchengAgent record, @Param("example") TongchengAgentExample example);

    int updateByExample(@Param("record") TongchengAgent record, @Param("example") TongchengAgentExample example);

    int updateByPrimaryKeySelective(TongchengAgent record);

    int updateByPrimaryKeyWithBLOBs(TongchengAgent record);

    int updateByPrimaryKey(TongchengAgent record);

    List<TongchengAgent> tongChengGroupOperationDataPage(@Param("localId") Long localId, @Param("minId") Long minId);

}