package com.br.marketing.mapper;

import com.br.marketing.dto.CarClueReportDTO;
import com.br.marketing.entity.CarClueInfo;
import com.br.marketing.vo.CarClueInfoVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface CarClueInfoMapper extends CarClueInfoMapperBase {

    List<CarClueInfoVo> selectList(Map<String, Object> params);

    /**
     * 批量插入 b_car_clue_info 数据
     *
     * @param carClueInfoList 待插入的数据列表
     * @return 插入的记录数
     */
    int batchInsert(@Param("carClueInfoList") List<CarClueInfo> carClueInfoList);


    List<String> queryApiCodes(@Param("pushStatus")Integer pushStatus);

    List<CarClueInfo> selectCarClueByMinId(@Param("apiCodeList")List<String> apiCodeList, @Param("status")Integer status, @Param("minId")Long minId);

    List<CarClueInfo> queryList(@Param("carClueReportDTO")CarClueReportDTO carClueReportDTO, @Param("minId")Long minId);
}