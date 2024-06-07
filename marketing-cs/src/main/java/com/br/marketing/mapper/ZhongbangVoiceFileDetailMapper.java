package com.br.marketing.mapper;

import com.br.marketing.entity.ZhongbangVoiceFileDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ZhongbangVoiceFileDetailMapper extends ZhongbangVoiceFileDetailMapperBase{

    Integer selectPushStatus0Count();

    Integer selectPushStatusCount(@Param("pushStatus") int pushStatus, @Param("fileId") Long fileId);

    List<Long> selectDistinctLocalIdtikv_();

    List<ZhongbangVoiceFileDetail> selectByPushStatus1(@Param("detailId") Long detailId);
    int updateBatchByIds(@Param("idList") List<Long> idList, @Param("pushStatus") int pushStatus
            , @Param("pushDate")String pushDate);
}