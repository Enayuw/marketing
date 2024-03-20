package com.br.marketing.mapper;

import com.br.marketing.entity.DewuCollidingDataLog;
import com.br.marketing.entity.ZhongyouFileData;

import java.util.List;

public interface DewuCollidingDataLogMapper extends DewuCollidingDataLogMapperBase{

    int saveBatch(List<DewuCollidingDataLog> dewuCollidingDataLogList);

}