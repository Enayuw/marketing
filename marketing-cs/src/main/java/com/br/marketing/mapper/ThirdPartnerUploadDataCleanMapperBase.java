package com.br.marketing.mapper;

import com.br.marketing.entity.ThirdPartnerUploadDataClean;
import com.br.marketing.entity.ThirdPartnerUploadDataCleanExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ThirdPartnerUploadDataCleanMapperBase {
    int countByExample(ThirdPartnerUploadDataCleanExample example);

    int deleteByExample(ThirdPartnerUploadDataCleanExample example);

    int deleteByPrimaryKey(Long id);

    int insert(ThirdPartnerUploadDataClean record);

    int insertSelective(ThirdPartnerUploadDataClean record);

    List<ThirdPartnerUploadDataClean> selectByExample(ThirdPartnerUploadDataCleanExample example);

    ThirdPartnerUploadDataClean selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") ThirdPartnerUploadDataClean record, @Param("example") ThirdPartnerUploadDataCleanExample example);

    int updateByExample(@Param("record") ThirdPartnerUploadDataClean record, @Param("example") ThirdPartnerUploadDataCleanExample example);

    int updateByPrimaryKeySelective(ThirdPartnerUploadDataClean record);

    int updateByPrimaryKey(ThirdPartnerUploadDataClean record);
}