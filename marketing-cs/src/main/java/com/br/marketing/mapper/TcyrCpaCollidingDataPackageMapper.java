package com.br.marketing.mapper;

import com.br.marketing.entity.TcyrCpaCollidingDataPackage;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface TcyrCpaCollidingDataPackageMapper extends TcyrCpaCollidingDataPackageMapperBase{

    List<TcyrCpaCollidingDataPackage> queryPackageInfo();

    Integer batchUpdatePackageMagnitude(@Param("updPkgs")List<TcyrCpaCollidingDataPackage> updPkgs);

    List<Long> queryPackageIdstikv_();

    List<TcyrCpaCollidingDataPackage> selectByCondition(@Param("packageName") String packageName, @Param("enabled") Integer enabled);

    String queryPackageNamesByIds(@Param("packageIds") List<Long> packageIds);

    List<Long> orderByPriority(@Param("packageIds") List<Long> packageIds);

}