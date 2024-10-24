package com.br.marketing.mapper;

import com.br.marketing.entity.SushangTransferData;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface SushangTransferDataMapper extends SushangTransferDataMapperBase{


    List<SushangTransferData> getMinOrderDateDatatikv_(@Param("local_id") Long transferLocalId,@Param("indexId") Long indexId,
                                                       @Param("pageSize") Integer pageSize);


    int countByLocalId(Long localId);
}
