package com.br.marketing.mapper;

import com.br.marketing.entity.SftpUploadTask;
import com.br.marketing.entity.SftpUploadTaskExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface SftpUploadTaskMapperBase {
    long countByExample(SftpUploadTaskExample example);

    int deleteByExample(SftpUploadTaskExample example);

    int deleteByPrimaryKey(Long id);

    int insert(SftpUploadTask record);

    int insertSelective(SftpUploadTask record);

    List<SftpUploadTask> selectByExample(SftpUploadTaskExample example);

    SftpUploadTask selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") SftpUploadTask record, @Param("example") SftpUploadTaskExample example);

    int updateByExample(@Param("record") SftpUploadTask record, @Param("example") SftpUploadTaskExample example);

    int updateByPrimaryKeySelective(SftpUploadTask record);

    int updateByPrimaryKey(SftpUploadTask record);
}