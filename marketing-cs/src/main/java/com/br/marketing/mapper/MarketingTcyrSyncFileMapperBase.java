package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingTcyrSyncFile;
import com.br.marketing.entity.MarketingTcyrSyncFileExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingTcyrSyncFileMapperBase {
    int countByExample(MarketingTcyrSyncFileExample example);

    int deleteByExample(MarketingTcyrSyncFileExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingTcyrSyncFile record);

    int insertSelective(MarketingTcyrSyncFile record);

    List<MarketingTcyrSyncFile> selectByExample(MarketingTcyrSyncFileExample example);

    MarketingTcyrSyncFile selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingTcyrSyncFile record, @Param("example") MarketingTcyrSyncFileExample example);

    int updateByExample(@Param("record") MarketingTcyrSyncFile record, @Param("example") MarketingTcyrSyncFileExample example);

    int updateByPrimaryKeySelective(MarketingTcyrSyncFile record);

    int updateByPrimaryKey(MarketingTcyrSyncFile record);
}