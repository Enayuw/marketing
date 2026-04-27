package com.br.marketing.mapper;

import com.br.marketing.entity.SftpVirtualHeaderScriptConfig;
import com.br.marketing.entity.SftpVirtualHeaderScriptConfigExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SftpVirtualHeaderScriptConfigMapperBase {
    int countByExample(SftpVirtualHeaderScriptConfigExample example);

    int deleteByExample(SftpVirtualHeaderScriptConfigExample example);

    int deleteByPrimaryKey(Long id);

    int insert(SftpVirtualHeaderScriptConfig record);

    int insertSelective(SftpVirtualHeaderScriptConfig record);

    List<SftpVirtualHeaderScriptConfig> selectByExample(SftpVirtualHeaderScriptConfigExample example);

    SftpVirtualHeaderScriptConfig selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") SftpVirtualHeaderScriptConfig record,
                                 @Param("example") SftpVirtualHeaderScriptConfigExample example);

    int updateByExample(@Param("record") SftpVirtualHeaderScriptConfig record, @Param("example") SftpVirtualHeaderScriptConfigExample example);

    int updateByPrimaryKeySelective(SftpVirtualHeaderScriptConfig record);

    int updateByPrimaryKey(SftpVirtualHeaderScriptConfig record);
}
