package com.br.marketing.mapper;

import com.br.marketing.entity.ZhongbangVoiceFileDetail;
import com.br.marketing.entity.ZhongbangVoiceFileDetailExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ZhongbangVoiceFileDetailMapperBase {
    int countByExample(ZhongbangVoiceFileDetailExample example);

    int deleteByExample(ZhongbangVoiceFileDetailExample example);

    int deleteByPrimaryKey(Long id);

    int insert(ZhongbangVoiceFileDetail record);

    int insertSelective(ZhongbangVoiceFileDetail record);

    List<ZhongbangVoiceFileDetail> selectByExample(ZhongbangVoiceFileDetailExample example);

    ZhongbangVoiceFileDetail selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") ZhongbangVoiceFileDetail record, @Param("example") ZhongbangVoiceFileDetailExample example);

    int updateByExample(@Param("record") ZhongbangVoiceFileDetail record, @Param("example") ZhongbangVoiceFileDetailExample example);

    int updateByPrimaryKeySelective(ZhongbangVoiceFileDetail record);

    int updateByPrimaryKey(ZhongbangVoiceFileDetail record);
}