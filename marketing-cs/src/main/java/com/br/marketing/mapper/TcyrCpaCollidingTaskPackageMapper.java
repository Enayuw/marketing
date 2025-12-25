package com.br.marketing.mapper;

import com.br.marketing.entity.TcyrCpaCollidingTaskPackage;
import com.br.marketing.entity.TcyrCpaCollidingTaskPackageExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TcyrCpaCollidingTaskPackageMapper extends TcyrCpaCollidingTaskPackageMapperBase {

    void insertBatch(@Param("list") List<TcyrCpaCollidingTaskPackage> list);
}