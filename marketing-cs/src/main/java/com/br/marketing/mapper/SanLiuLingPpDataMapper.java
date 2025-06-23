package com.br.marketing.mapper;

import com.br.marketing.entity.SanLiuLingPpData;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SanLiuLingPpDataMapper extends SanLiuLingPpDataMapperBase {

    List<SanLiuLingPpData> getTrafficData(@Param("localId") Long localId, @Param("minId") Long minId);

    void batchUpdatePushStatusAndResult(@Param("list") List<SanLiuLingPpData> list);

}
