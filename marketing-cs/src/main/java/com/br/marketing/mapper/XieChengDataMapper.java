package com.br.marketing.mapper;

import com.alibaba.fastjson.JSONArray;
import com.br.marketing.client.xiecheng.intput.AdReqDTO;
import com.br.marketing.entity.XieChengData;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface XieChengDataMapper extends XieChengDataMapperBase{



    List<XieChengData> selectByLocalId(@Param("localId") Long localId);

    List<String> selectLocalIdByNotSend();

    List<XieChengData> getByCellToday(@Param("cell") String cell,@Param("apiCodes") JSONArray apiCodes);

    List<XieChengData> getByCellTodayAndLocalId(@Param("createDate")Integer createDate, @Param("minlocalId") Long minlocalId);

    List<XieChengData> selectXieChengCall(@Param("createTime") String createTime, @Param("id") Long id);

    /**
     * 携程百万量级转化统计报表数据获取 上报相关量级
     * @param cid cid
     * @param apiCode apiCode
     * @param requestData T-1
     * @param endData T
     * @param convType convType
     * @return Integer
     */
    Integer getXieChengDataCounttikv_(@Param("cid")Long cid, @Param("apiCode")String apiCode,
                                      @Param("requestData") String requestData, @Param("endData") String endData,
                                      @Param("convType") String convType);
    /**
     * 携程百万量级转化统计报表数据获取 上报相关量级
     * @param cid cid
     * @param apiCode apiCode
     * @param requestData T-1
     * @param convType convType
     * @return Integer
     */
    Integer getUploadCounttikv_(@Param("cid")Long cid, @Param("apiCode")String apiCode,
                                @Param("requestData") String requestData, @Param("endData") String endData,
                                @Param("convType") String convType);

    List<Integer> getReportPushStatusInPeriod(@Param("cell") String cell, @Param("apiCode") String apiCode);

    List<AdReqDTO> executeBackSql(@Param("sqlParameter") String sqlParameter,@Param("minId") Long minId);


}