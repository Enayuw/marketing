package com.br.marketing.mapper;

import com.br.marketing.entity.TcyrCpaCollidingDataPackage;

import java.util.List;

public interface TcyrCpaCollidingDataPackageMapper extends TcyrCpaCollidingDataPackageMapperBase{

    List<TcyrCpaCollidingDataPackage> queryPackageInfo();

}