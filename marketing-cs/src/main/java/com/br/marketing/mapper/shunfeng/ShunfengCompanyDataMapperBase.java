package com.br.marketing.mapper.shunfeng;

import com.br.marketing.entity.ShunfengCompanyData;
import com.br.marketing.entity.ShunfengCompanyDataExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ShunfengCompanyDataMapperBase {
    long countByExample(ShunfengCompanyDataExample example);

    int deleteByExample(ShunfengCompanyDataExample example);

    int deleteByPrimaryKey(Long id);

    int insert(ShunfengCompanyData record);

    int insertSelective(ShunfengCompanyData record);

    List<ShunfengCompanyData> selectByExample(ShunfengCompanyDataExample example);

    ShunfengCompanyData selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") ShunfengCompanyData record, @Param("example") ShunfengCompanyDataExample example);

    int updateByExample(@Param("record") ShunfengCompanyData record, @Param("example") ShunfengCompanyDataExample example);

    int updateByPrimaryKeySelective(ShunfengCompanyData record);

    int updateByPrimaryKey(ShunfengCompanyData record);
}