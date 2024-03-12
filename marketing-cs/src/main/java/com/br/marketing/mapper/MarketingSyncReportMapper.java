package com.br.marketing.mapper;

import com.br.marketing.dto.SyncUserTypeNumDTO;
import com.br.marketing.entity.MarketingDataValidConfig;
import com.br.marketing.entity.MarketingSyncReport;
import com.br.marketing.entity.MarketingSyncReportExample;
import com.br.marketing.mysqlInterceptor.AddDataAuth;
import com.br.marketing.vo.MarketingSyncReportNumVO;
import com.br.marketing.vo.MarketingSyncReportVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface MarketingSyncReportMapper {
    int uploadSyncCounttiflash_(@Param("apiCode") String apiCode, @Param("userType") String userType,
                        @Param("uploadDate") String uploadDate, @Param("status") Integer status);

    String uploadSyncMinAppletTimetiflash_(@Param("apiCode") String apiCode, @Param("userType") String userType,
                                           @Param("uploadDate") String uploadDate);

    String uploadSyncMaxAppletTimetiflash_(@Param("apiCode") String apiCode, @Param("userType") String userType,
                                           @Param("uploadDate") String uploadDate);

    List<String> getAppletDatetikv_(@Param("apiCode") String apiCode, @Param("userType") String userType,
                                    @Param("startDate") String startDate, @Param("endDate") String endDate, @Param("appletDateStart") String appletDateStart);

    @AddDataAuth
    List<MarketingSyncReportVO> selectList(Map<String, Object> params);

    @AddDataAuth
    List<MarketingSyncReportNumVO> getReportListTotaltiflash_(Map<String, Object> params);

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

    int deleteByAppletDate(@Param("apiCode") String apiCode,@Param("appletDate") String appletDate);

    List<SyncUserTypeNumDTO> uploadSyncCount(@Param("apiCode") String apiCode,@Param("appletDate") String appletDate);

    /**
     * 根据ID查找有效期记录
     * @param id
     * @return
     */
    MarketingSyncReportVO selectById(@Param("id") Long id);


    /**
     * 获取有效期数据
     * @param apiCode
     * @param userType
     * @param appletDate
     * @return
     */
    MarketingDataValidConfig selectValidData(@Param("apiCode")String apiCode ,@Param("userType")String userType ,@Param("appletDate")String appletDate);

    /**
     * 修改有效期记录数据
     * @param config
     * @return
     */
    Integer updateById(@Param("config")MarketingDataValidConfig config);
}