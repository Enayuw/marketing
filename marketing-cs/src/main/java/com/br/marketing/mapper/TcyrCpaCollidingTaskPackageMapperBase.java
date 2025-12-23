package com.br.marketing.mapper;

import com.br.marketing.entity.TcyrCpaCollidingTaskPackage;
import com.br.marketing.entity.TcyrCpaCollidingTaskPackageExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TcyrCpaCollidingTaskPackageMapperBase {
    int countByExample(TcyrCpaCollidingTaskPackageExample example);

    int deleteByExample(TcyrCpaCollidingTaskPackageExample example);

    int deleteByPrimaryKey(Long id);

    int insert(TcyrCpaCollidingTaskPackage record);

    int insertSelective(TcyrCpaCollidingTaskPackage record);

    List<TcyrCpaCollidingTaskPackage> selectByExample(TcyrCpaCollidingTaskPackageExample example);

    TcyrCpaCollidingTaskPackage selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") TcyrCpaCollidingTaskPackage record, @Param("example") TcyrCpaCollidingTaskPackageExample example);

    int updateByExample(@Param("record") TcyrCpaCollidingTaskPackage record, @Param("example") TcyrCpaCollidingTaskPackageExample example);

    int updateByPrimaryKeySelective(TcyrCpaCollidingTaskPackage record);

    int updateByPrimaryKey(TcyrCpaCollidingTaskPackage record);
}