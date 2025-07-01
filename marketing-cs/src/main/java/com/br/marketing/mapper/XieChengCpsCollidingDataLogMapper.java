package com.br.marketing.mapper;

import com.br.marketing.entity.XieChengCpsCollidingDataLog;
import org.apache.ibatis.annotations.Param;


public interface XieChengCpsCollidingDataLogMapper extends XieChengCpsCollidingDataLogMapperBase {
    XieChengCpsCollidingDataLog selectLatestCpsLog(@Param("sha256Tel") String sha256Tel);
}