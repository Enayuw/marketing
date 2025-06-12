package com.br.marketing.mapper;


import com.br.marketing.entity.QifuActuation;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface QifuActuationMapper extends QifuActuationMapperBase {

    int batchInsert(@Param("list") List<QifuActuation> list);

}