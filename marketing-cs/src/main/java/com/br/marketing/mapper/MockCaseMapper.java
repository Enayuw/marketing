package com.br.marketing.mapper;

import com.br.marketing.entity.MockCase;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface MockCaseMapper extends MockCaseMapperBase{

    int batchInsert(@Param("list")List<MockCase> list);

}