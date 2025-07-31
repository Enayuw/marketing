package com.br.marketing.mapper;

import com.br.marketing.entity.ZhongAnCollidingDataLog;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ZhongAnCollidingDataLogMapper extends ZhongAnCollidingDataLogMapperBase {

    int countSmsSendSuccess(@Param("cellMd5") String cellMd5, @Param("userType") String userType, @Param("bizDate") String bizDate);

    int countCallConnectSuccess(@Param("cellMd5") String cellMd5, @Param("userType") String userType, @Param("bizDate") String bizDate);

    List<Long> getConnectIdsByDay(@Param("cellMd5") String cellMd5, @Param("userType") String userType, @Param("bizDate") String bizDate);

    List<Long> getSmsIdsByDay(@Param("cellMd5") String cellMd5, @Param("userType") String userType, @Param("bizDate") String bizDate);

    List<Long> getConnectIdsByMonth(@Param("cellMd5") String cellMd5, @Param("userType") String userType, @Param("bizDate") String bizDate);

    List<Long> getSmsIdsByMonth(@Param("cellMd5") String cellMd5, @Param("userType") String userType, @Param("bizDate") String bizDate);

    int countMobilePerDay(@Param("cellMd5") String cellMd5, @Param("userType") String userType, @Param("bizDate") String bizDate);

    int batchInsert(@Param("collidingDataLogList") List<ZhongAnCollidingDataLog> collidingDataLogList);
}