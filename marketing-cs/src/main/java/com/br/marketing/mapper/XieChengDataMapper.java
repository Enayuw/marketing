package com.br.marketing.mapper;

import com.alibaba.fastjson.JSONArray;
import com.br.marketing.entity.XieChengData;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface XieChengDataMapper extends XieChengDataMapperBase{



    List<XieChengData> selectByLocalId(@Param("localId") Long localId,@Param("minId") Long minId);

    List<String> selectLocalIdByNotSend();

    List<XieChengData> getByCellToday(@Param("cell") String cell,@Param("apiCodes") JSONArray apiCodes);

    List<XieChengData> getByCellTodayAndLocalId(@Param("createDate")Integer createDate, @Param("minlocalId") Long minlocalId);

    List<XieChengData> selectXieChengCall(@Param("createTime") String createTime, @Param("id") Long id);

    /**
     * 携程百万量级转化统计报表数据获取 上报相关量级
     * @param cid cid
     * @param requestData requestData
     * @param convType convType
     * @return Integer
     */
    Integer getUploadCounttikv_(@Param("cid")Long cid, @Param("requestData") String requestData, @Param("convType") String convType);
}