package com.br.marketing.mapper;


import com.br.marketing.entity.DataDistributeDetailLog;
import com.br.marketing.entity.PhoneSaleTransferInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

public interface DataDistributeDetailLogMapper extends DataDistributeDetailLogMapperBase {

    Set<String> getToDataDistributeInfoList(@Param("apiCode") String apiCode, @Param("cells") Set<String> cells);

    void insertBatch(@Param("list") List<DataDistributeDetailLog> list);

}