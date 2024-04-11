package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingSyncUserExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface MarketingSyncUserMapper {
    int countByExample(MarketingSyncUserExample example);

    int deleteByExample(MarketingSyncUserExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingSyncUser record);

    int insertSelective(MarketingSyncUser record);

    List<MarketingSyncUser> selectByExampleWithBLOBs(MarketingSyncUserExample example);

    List<MarketingSyncUser> selectByExample(MarketingSyncUserExample example);

    MarketingSyncUser selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingSyncUser record, @Param("example") MarketingSyncUserExample example);

    int updateByExampleWithBLOBs(@Param("record") MarketingSyncUser record, @Param("example") MarketingSyncUserExample example);

    int updateByExample(@Param("record") MarketingSyncUser record, @Param("example") MarketingSyncUserExample example);

    int updateByPrimaryKeySelective(MarketingSyncUser record);

    int updateByPrimaryKeyWithBLOBs(MarketingSyncUser record);

    int updateByPrimaryKey(MarketingSyncUser record);
}