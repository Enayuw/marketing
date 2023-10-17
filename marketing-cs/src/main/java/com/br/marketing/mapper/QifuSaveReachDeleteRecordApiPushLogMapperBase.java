package com.br.marketing.mapper;

import com.br.marketing.entity.QifuSaveReachDeleteRecordApiPushLog;
import com.br.marketing.entity.QifuSaveReachDeleteRecordApiPushLogExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface QifuSaveReachDeleteRecordApiPushLogMapperBase {
    int countByExample(QifuSaveReachDeleteRecordApiPushLogExample example);

    int deleteByExample(QifuSaveReachDeleteRecordApiPushLogExample example);

    int deleteByPrimaryKey(Long id);

    int insert(QifuSaveReachDeleteRecordApiPushLog record);

    int insertSelective(QifuSaveReachDeleteRecordApiPushLog record);

    List<QifuSaveReachDeleteRecordApiPushLog> selectByExample(QifuSaveReachDeleteRecordApiPushLogExample example);

    QifuSaveReachDeleteRecordApiPushLog selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") QifuSaveReachDeleteRecordApiPushLog record, @Param("example") QifuSaveReachDeleteRecordApiPushLogExample example);

    int updateByExample(@Param("record") QifuSaveReachDeleteRecordApiPushLog record, @Param("example") QifuSaveReachDeleteRecordApiPushLogExample example);

    int updateByPrimaryKeySelective(QifuSaveReachDeleteRecordApiPushLog record);

    int updateByPrimaryKey(QifuSaveReachDeleteRecordApiPushLog record);
}