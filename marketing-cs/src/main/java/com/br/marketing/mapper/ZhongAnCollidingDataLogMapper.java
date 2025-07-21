package com.br.marketing.mapper;

import com.br.marketing.entity.ZhongAnCollidingDataLog;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface ZhongAnCollidingDataLogMapper extends ZhongAnCollidingDataLogMapperBase{

    int countSmsSendSuccess(@Param("cellMd5")String cellMd5);

    int batchInsert(@Param("collidingDataLogList") List<ZhongAnCollidingDataLog> collidingDataLogList);
}