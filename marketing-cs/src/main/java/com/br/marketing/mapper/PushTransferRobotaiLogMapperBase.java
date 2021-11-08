package com.br.marketing.mapper;

import com.br.marketing.entity.PushTransferRobotaiLog;
import com.br.marketing.entity.PushTransferRobotaiLogExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PushTransferRobotaiLogMapperBase {
    int countByExample(PushTransferRobotaiLogExample example);

    int deleteByExample(PushTransferRobotaiLogExample example);

    int deleteByPrimaryKey(Long id);

    int insert(PushTransferRobotaiLog record);

    int insertSelective(PushTransferRobotaiLog record);

    List<PushTransferRobotaiLog> selectByExample(PushTransferRobotaiLogExample example);

    PushTransferRobotaiLog selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") PushTransferRobotaiLog record, @Param("example") PushTransferRobotaiLogExample example);

    int updateByExample(@Param("record") PushTransferRobotaiLog record, @Param("example") PushTransferRobotaiLogExample example);

    int updateByPrimaryKeySelective(PushTransferRobotaiLog record);

    int updateByPrimaryKey(PushTransferRobotaiLog record);
}