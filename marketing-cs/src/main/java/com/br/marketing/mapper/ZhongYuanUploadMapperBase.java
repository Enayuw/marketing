package com.br.marketing.mapper;

import com.br.marketing.entity.ZhongYuanUpload;
import com.br.marketing.entity.ZhongYuanUploadExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ZhongYuanUploadMapperBase {
    int countByExample(ZhongYuanUploadExample example);

    int deleteByExample(ZhongYuanUploadExample example);

    int deleteByPrimaryKey(Long id);

    int insert(ZhongYuanUpload record);

    int insertSelective(ZhongYuanUpload record);

    List<ZhongYuanUpload> selectByExample(ZhongYuanUploadExample example);

    ZhongYuanUpload selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") ZhongYuanUpload record, @Param("example") ZhongYuanUploadExample example);

    int updateByExample(@Param("record") ZhongYuanUpload record, @Param("example") ZhongYuanUploadExample example);

    int updateByPrimaryKeySelective(ZhongYuanUpload record);

    int updateByPrimaryKey(ZhongYuanUpload record);
}