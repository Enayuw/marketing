package com.br.marketing.mapper;


import com.br.marketing.entity.TaikangDingDingTransferDetail;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TaikangDingDingTransferDetailMapper extends TaikangDingDingTransferDetailMapperBase{

    List<TaikangDingDingTransferDetail> selectDetailList(@Param("searchId") Long searchId,
                                                         @Param("searchSize") Integer searchSize,
                                                         @Param("pushStatus") Integer pushStatus);
}