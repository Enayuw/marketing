package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingSyncReport;
import com.br.marketing.entity.MarketingSyncReportExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MarketingSyncReportMapper {
    int uploadSyncCount(@Param("apiCode") String apiCode, @Param("userType") String userType,
                        @Param("uploadDate") String uploadDate, @Param("status") Integer status);

    String uploadSyncMinAppletTime(@Param("apiCode") String apiCode, @Param("userType") String userType,
                                   @Param("uploadDate") String uploadDate);

    String uploadSyncMaxAppletTime(@Param("apiCode") String apiCode, @Param("userType") String userType,
                                   @Param("uploadDate") String uploadDate);

    int modifyReportById(MarketingSyncReport record);

    int countByExample(MarketingSyncReportExample example);

    int deleteByExample(MarketingSyncReportExample example);

    int deleteByPrimaryKey(Long id);

    int insert(MarketingSyncReport record);

    int insertSelective(MarketingSyncReport record);

    List<MarketingSyncReport> selectByExample(MarketingSyncReportExample example);

    MarketingSyncReport selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") MarketingSyncReport record, @Param("example") MarketingSyncReportExample example);

    int updateByExample(@Param("record") MarketingSyncReport record, @Param("example") MarketingSyncReportExample example);

    int updateByPrimaryKeySelective(MarketingSyncReport record);

    int updateByPrimaryKey(MarketingSyncReport record);
}