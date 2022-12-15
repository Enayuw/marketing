package com.br.marketing.mapper;

import com.br.marketing.entity.ZhonganMarketingBan;
import com.br.marketing.entity.ZhonganMarketingBanExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ZhonganMarketingBanMapperBase {
    int countByExample(ZhonganMarketingBanExample example);

    int deleteByExample(ZhonganMarketingBanExample example);

    int deleteByPrimaryKey(Long id);

    int insert(ZhonganMarketingBan record);

    int insertSelective(ZhonganMarketingBan record);

    List<ZhonganMarketingBan> selectByExample(ZhonganMarketingBanExample example);

    ZhonganMarketingBan selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") ZhonganMarketingBan record, @Param("example") ZhonganMarketingBanExample example);

    int updateByExample(@Param("record") ZhonganMarketingBan record, @Param("example") ZhonganMarketingBanExample example);

    int updateByPrimaryKeySelective(ZhonganMarketingBan record);

    int updateByPrimaryKey(ZhonganMarketingBan record);
}