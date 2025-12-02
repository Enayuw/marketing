package com.br.marketing.mapper;

import com.br.marketing.entity.DxmSftpConfig;
import com.br.marketing.entity.DxmSftpConfigExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface DxmSftpConfigMapperBase {
    int countByExample(DxmSftpConfigExample example);

    int deleteByExample(DxmSftpConfigExample example);

    int deleteByPrimaryKey(Long id);

    int insert(DxmSftpConfig record);

    int insertSelective(DxmSftpConfig record);

    List<DxmSftpConfig> selectByExample(DxmSftpConfigExample example);

    DxmSftpConfig selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") DxmSftpConfig record, @Param("example") DxmSftpConfigExample example);

    int updateByExample(@Param("record") DxmSftpConfig record, @Param("example") DxmSftpConfigExample example);

    int updateByPrimaryKeySelective(DxmSftpConfig record);

    int updateByPrimaryKey(DxmSftpConfig record);
}