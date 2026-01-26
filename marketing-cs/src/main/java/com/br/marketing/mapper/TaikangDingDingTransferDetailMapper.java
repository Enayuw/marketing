package com.br.marketing.mapper;

import org.apache.ibatis.annotations.Param;
import com.br.marketing.entity.TaikangDingDingTransferDetail;

import java.util.List;

public interface TaikangDingDingTransferDetailMapper extends TaikangDingDingTransferDetailMapperBase{


    void batchInsert(@Param("detailList")List<TaikangDingDingTransferDetail> transferDetailList);

    void updatePushStatusByIds(@Param("idList") List<Long> idList, @Param("pushStatus") Integer pushStatus);

    List<TaikangDingDingTransferDetail> selectDetailList(@Param("searchId") Long searchId,
                                                         @Param("searchSize") Integer searchSize,
                                                         @Param("pushStatus") Integer pushStatus);

}