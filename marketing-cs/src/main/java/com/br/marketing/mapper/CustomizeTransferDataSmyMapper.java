package com.br.marketing.mapper;

import com.br.marketing.entity.CustomizeTransferDataSmy;
import org.apache.ibatis.annotations.Param;

public interface CustomizeTransferDataSmyMapper {
    void createCustomizeTransferDataTable(@Param("tCid") String tCid);

    int insertSelective(CustomizeTransferDataSmy customizeTransferDataSmy);
}