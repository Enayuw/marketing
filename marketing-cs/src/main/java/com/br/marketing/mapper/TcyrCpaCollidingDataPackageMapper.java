package com.br.marketing.mapper;

import com.br.marketing.entity.TcyrCpaCollidingDataPackage;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface TcyrCpaCollidingDataPackageMapper extends TcyrCpaCollidingDataPackageMapperBase{

    List<TcyrCpaCollidingDataPackage> queryPackageInfo();

    Integer batchUpdatePackageMagnitude(@Param("updPkgs")List<TcyrCpaCollidingDataPackage> updPkgs);

    List<Long> queryPackageIdstikv_();
}