package com.br.marketing.mapper;

import com.br.marketing.entity.SuccessFileUploadConfig;
import com.br.marketing.entity.SuccessFileUploadConfigExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SuccessFileUploadConfigMapperBase {
    int countByExample(SuccessFileUploadConfigExample example);

    int deleteByExample(SuccessFileUploadConfigExample example);

    int deleteByPrimaryKey(Long id);

    int insert(SuccessFileUploadConfig record);

    int insertSelective(SuccessFileUploadConfig record);

    List<SuccessFileUploadConfig> selectByExample(SuccessFileUploadConfigExample example);

    SuccessFileUploadConfig selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") SuccessFileUploadConfig record, @Param("example") SuccessFileUploadConfigExample example);

    int updateByExample(@Param("record") SuccessFileUploadConfig record, @Param("example") SuccessFileUploadConfigExample example);

    int updateByPrimaryKeySelective(SuccessFileUploadConfig record);

    int updateByPrimaryKey(SuccessFileUploadConfig record);
}