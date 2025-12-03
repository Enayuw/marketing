package com.br.marketing.mapper;

import com.br.marketing.entity.TcyrCpaCollidingDataPackage;
import com.br.marketing.entity.TcyrCpaCollidingDataPackageExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TcyrCpaCollidingDataPackageMapperBase {
    int countByExample(TcyrCpaCollidingDataPackageExample example);

    int deleteByExample(TcyrCpaCollidingDataPackageExample example);

    int deleteByPrimaryKey(Long id);

    int insert(TcyrCpaCollidingDataPackage record);

    int insertSelective(TcyrCpaCollidingDataPackage record);

    List<TcyrCpaCollidingDataPackage> selectByExample(TcyrCpaCollidingDataPackageExample example);

    TcyrCpaCollidingDataPackage selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") TcyrCpaCollidingDataPackage record, @Param("example") TcyrCpaCollidingDataPackageExample example);

    int updateByExample(@Param("record") TcyrCpaCollidingDataPackage record, @Param("example") TcyrCpaCollidingDataPackageExample example);

    int updateByPrimaryKeySelective(TcyrCpaCollidingDataPackage record);

    int updateByPrimaryKey(TcyrCpaCollidingDataPackage record);
}