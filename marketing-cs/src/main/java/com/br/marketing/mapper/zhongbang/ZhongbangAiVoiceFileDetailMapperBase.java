package com.br.marketing.mapper.zhongbang;

import com.br.marketing.entity.zhongbang.ZhongbangAiVoiceFileDetail;
import com.br.marketing.entity.zhongbang.ZhongbangAiVoiceFileDetailExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ZhongbangAiVoiceFileDetailMapperBase {
    int countByExample(ZhongbangAiVoiceFileDetailExample example);

    int deleteByExample(ZhongbangAiVoiceFileDetailExample example);

    int deleteByPrimaryKey(Long id);

    int insert(ZhongbangAiVoiceFileDetail record);

    int insertSelective(ZhongbangAiVoiceFileDetail record);

    List<ZhongbangAiVoiceFileDetail> selectByExample(ZhongbangAiVoiceFileDetailExample example);

    ZhongbangAiVoiceFileDetail selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") ZhongbangAiVoiceFileDetail record, @Param("example") ZhongbangAiVoiceFileDetailExample example);

    int updateByExample(@Param("record") ZhongbangAiVoiceFileDetail record, @Param("example") ZhongbangAiVoiceFileDetailExample example);

    int updateByPrimaryKeySelective(ZhongbangAiVoiceFileDetail record);

    int updateByPrimaryKey(ZhongbangAiVoiceFileDetail record);
}