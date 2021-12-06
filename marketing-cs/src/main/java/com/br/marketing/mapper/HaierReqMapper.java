package com.br.marketing.mapper;


import com.br.marketing.entity.HaierReq;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface HaierReqMapper extends HaierReqMapperBase {

    List<HaierReq> getDataWithStatus(@Param("minId") Long minId);
}