package com.br.marketing.mapper;

import com.br.marketing.dto.SyncOperateTypeDTO;
import com.br.marketing.dto.SyncUserTypeNumDTO;
import com.br.marketing.dto.autocheck.CheckUploadSyncDataDto;
import com.br.marketing.entity.MarketingDataValidConfig;
import com.br.marketing.entity.MarketingSyncReport;
import com.br.marketing.entity.MarketingSyncReportExample;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.mysqlInterceptor.AddDataAuth;
import com.br.marketing.vo.MarketingSyncReportNumVO;
import com.br.marketing.vo.MarketingSyncReportVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface MarketingSyncReportMapper extends MarketingSyncReportMapperBase {
    int uploadSyncCounttiflash_(@Param("apiCode") String apiCode, @Param("userType") String userType,
                        @Param("uploadDate") String uploadDate, @Param("status") Integer status);

    String uploadSyncMinAppletTimetiflash_(@Param("apiCode") String apiCode, @Param("userType") String userType,
                                           @Param("uploadDate") String uploadDate);

    String uploadSyncMaxAppletTimetiflash_(@Param("apiCode") String apiCode, @Param("userType") String userType,
                                           @Param("uploadDate") String uploadDate);

    List<String> getAppletDate(@Param("apiCode") String apiCode, @Param("userType") String userType,
                               @Param("startDate") String startDate, @Param("endDate") String endDate,
                               @Param("appletDateStart") String appletDateStart);

    @AddDataAuth
    List<MarketingSyncReportVO> selectList(Map<String, Object> params);

    @AddDataAuth
    List<MarketingSyncReportVO> selectExportDataList(Map<String, Object> params);

    @AddDataAuth
    List<MarketingSyncReportNumVO> getReportListTotaltiflash_(Map<String, Object> params);

    int modifyReportById(MarketingSyncReport record);

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
    MarketingDataValidConfig selectValidData(@Param("apiCode") String apiCode, @Param("userType") String userType, @Param("appletDate") String appletDate);

    /**
     * 修改有效期记录数据
     *
     * @param config
     * @return
     */
    Integer updateById(@Param("config") MarketingDataValidConfig config);

    /**
     * 2024-03-08 9:29
     * 获取数据量级
     *
     * @param example 条件
     * @return list
     */
    List<MarketingSyncReport> selectNumberByExample(MarketingSyncReportExample example);

    /**
     * 查询
     *
     * @param apiCode
     *  @param userType
     *       *  @param userType
     * @return
     */
    List<String> selectUploadExtendKeystikv_(@Param("apiCode")String apiCode, @Param("userType")String userType, @Param("appletDate")String appletDate);


    List<Map<String, Object>> selectGroupCounttikv_(@Param("sql")String toString);

    List<MarketingSyncUser> selectGroupData(@Param("apiCode")String apiCode, @Param("list") List<String> appletDates, @Param("userType")String userType,
                                            @Param("extend")String extend, @Param("indexId")Long indexId, @Param("pageSize")Integer pageSize);

    int updateBatchGroupData(@Param("updateSql")String update);

    List<Map<String, Object>> selectGroupUploadNumtikv_(@Param("apiCode")String apiCode, @Param("list")List<MarketingSyncReport> reportList,
                                                   @Param("field")String field,@Param("extendField")String extendField);

    List<MarketingSyncUser> selectGroupDataByReport(@Param("apiCode")String apiCode,@Param("list")List<MarketingSyncReport> reportList,@Param("extend")String extend,
                                                    @Param("indexId")Long indexId,  @Param("pageSize")Integer pageSize);

    Map<String, Long> selectGroupMaxMinId(@Param("apiCode")String apiCode, @Param("indexId")Long indexId,
                                                   @Param("groupNum")Integer groupNum,@Param("sqlCondition")String sqlCondition);

    /**
     * 统计上传记录关键信息：正常入库条数，去重后条数，上传开始时间，上传结束时间
     * @param apiCode
     * @param userType
     * @param appletDate
     * @return
     */
    Map<String,Object> selectUploadMagnStatInfotiflash_(@Param("apiCode") String apiCode,
                                                @Param("userType") String userType,
                                                @Param("appletDate") String appletDate);

    /**
     * 获取近一个月有数据的日期集合
     *
     * @param apiCode API编码
     * @return 日期列表，格式：yyyy-MM-dd
     */
    List<String> getLastMonthDataDates(@Param("apiCode") String apiCode);

    /**
     * 查询指定id列表对应的数据
     */
    List<MarketingSyncReportVO> selectByIdList(@Param("idList")List<Long> selectIdList);


    List<SyncOperateTypeDTO> selectOperateTypeGroup(@Param("apiCode") String apiCode,
                                                    @Param("syncReportList") List<MarketingSyncReport> syncReportList);

    List<MarketingSyncReport> selectByIds(@Param("ids") List<Long> ids);
}