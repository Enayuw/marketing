package com.br.marketing.mapper;

import com.br.marketing.entity.TwosevenFile;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TwosevenFileMapper extends TwosevenFileMapperBase {

    List<TwosevenFile> getPushData(@Param("localId") Long localId, @Param("dataId")  Long dataId);
}