package com.br.marketing.mapper;

import com.br.marketing.entity.ZhongYuanAgent;
import com.br.marketing.entity.ZhongYuanAgentExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ZhongYuanAgentMapperBase {
    int countByExample(ZhongYuanAgentExample example);

    int deleteByExample(ZhongYuanAgentExample example);

    int deleteByPrimaryKey(Long id);

    int insert(ZhongYuanAgent record);

    int insertSelective(ZhongYuanAgent record);

    List<ZhongYuanAgent> selectByExample(ZhongYuanAgentExample example);

    ZhongYuanAgent selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") ZhongYuanAgent record, @Param("example") ZhongYuanAgentExample example);

    int updateByExample(@Param("record") ZhongYuanAgent record, @Param("example") ZhongYuanAgentExample example);

    int updateByPrimaryKeySelective(ZhongYuanAgent record);

    int updateByPrimaryKey(ZhongYuanAgent record);
}