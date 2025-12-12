package com.br.marketing.mapper;

import com.br.marketing.entity.LineAccountLogNormal;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface LineAccountLogNormalMapper extends LineAccountLogNormalMapperBase{

    List<LineAccountLogNormal> getLineAccountLogs(@Param("groupId") Long groupId);
}