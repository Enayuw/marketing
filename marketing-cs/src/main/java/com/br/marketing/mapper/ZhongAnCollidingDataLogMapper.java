package com.br.marketing.mapper;

import com.br.marketing.entity.ZhongAnCollidingDataLog;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ZhongAnCollidingDataLogMapper extends ZhongAnCollidingDataLogMapperBase{

    int countSmsSendSuccess(@Param("cellMd5") String cellMd5, @Param("bizDate") String bizDate);

    int countCallConnectSuccess(@Param("cellMd5")String cellMd5,  @Param("bizDate")String bizDate);

    int countConnectByDay(@Param("cellMd5")String cellMd5,  @Param("bizDate")String bizDate);

    int countSmsSendByDay(@Param("cellMd5")String cellMd5,  @Param("bizDate")String bizDate);

    int countConnectByMonth(@Param("cellMd5")String cellMd5,  @Param("bizDate")String bizDate);

    int countSmsSendByMonth(@Param("cellMd5")String cellMd5,  @Param("bizDate")String bizDate);

    int batchInsert(@Param("collidingDataLogList") List<ZhongAnCollidingDataLog> collidingDataLogList);
}