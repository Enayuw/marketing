package com.br.marketing.mapper;

import com.br.marketing.entity.ThirdPartnerUploadDataCleanFront;
import com.br.marketing.entity.ThirdPartnerUploadDataCleanFrontExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ThirdPartnerUploadDataCleanFrontMapper {
    int countByExample(ThirdPartnerUploadDataCleanFrontExample example);

    int deleteByExample(ThirdPartnerUploadDataCleanFrontExample example);

    int deleteByPrimaryKey(Long id);

    int insert(ThirdPartnerUploadDataCleanFront record);

    int insertSelective(ThirdPartnerUploadDataCleanFront record);

    List<ThirdPartnerUploadDataCleanFront> selectByExample(ThirdPartnerUploadDataCleanFrontExample example);

    ThirdPartnerUploadDataCleanFront selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") ThirdPartnerUploadDataCleanFront record, @Param("example") ThirdPartnerUploadDataCleanFrontExample example);

    int updateByExample(@Param("record") ThirdPartnerUploadDataCleanFront record, @Param("example") ThirdPartnerUploadDataCleanFrontExample example);

    int updateByPrimaryKeySelective(ThirdPartnerUploadDataCleanFront record);

    int updateByPrimaryKey(ThirdPartnerUploadDataCleanFront record);
}