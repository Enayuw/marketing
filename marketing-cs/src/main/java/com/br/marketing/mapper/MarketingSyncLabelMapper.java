package com.br.marketing.mapper;

import com.br.marketing.entity.MarketingSyncLabel;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface MarketingSyncLabelMapper {

    int batchInsert(@Param("apiCode") String apiCode,@Param("list") List<MarketingSyncLabel> list);



    List<Map<String,Object>> getLabelNum(@Param("labelId") Long labelId,@Param("apiCode") String apiCode);


    int singleInsert(@Param("apiCode") String apiCode,@Param("syncLabel") MarketingSyncLabel syncLabel);

    List<Long> getSyncIdByLabelId(@Param("apiCode") String apiCode
            , @Param("whereStr") String whereStr, @Param("syncId") Long syncId, @Param("pageSize") Integer pageSize, @Param("labelId") Long labelId,
                                  @Param("minUnCompleteId") Long minUnCompleteId,@Param("maxId") Long maxId);
}
