package com.br.marketing.mapper;

import com.br.marketing.entity.CustomizeTransferDataSmy;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TcSearchMapper {

    Object search(@Param("custList") List<String> custList);
}