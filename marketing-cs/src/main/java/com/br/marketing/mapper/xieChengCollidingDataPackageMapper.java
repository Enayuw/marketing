package com.br.marketing.mapper;

import com.br.marketing.entity.xieChengCollidingDataPackage;
import com.br.marketing.entity.xieChengCollidingDataPackageExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface xieChengCollidingDataPackageMapper {
    int countByExample(xieChengCollidingDataPackageExample example);

    int deleteByExample(xieChengCollidingDataPackageExample example);

    int deleteByPrimaryKey(Long id);

    int insert(xieChengCollidingDataPackage record);

    int insertSelective(xieChengCollidingDataPackage record);

    List<xieChengCollidingDataPackage> selectByExampleWithBLOBs(xieChengCollidingDataPackageExample example);

    List<xieChengCollidingDataPackage> selectByExample(xieChengCollidingDataPackageExample example);

    xieChengCollidingDataPackage selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") xieChengCollidingDataPackage record, @Param("example") xieChengCollidingDataPackageExample example);

    int updateByExampleWithBLOBs(@Param("record") xieChengCollidingDataPackage record, @Param("example") xieChengCollidingDataPackageExample example);

    int updateByExample(@Param("record") xieChengCollidingDataPackage record, @Param("example") xieChengCollidingDataPackageExample example);

    int updateByPrimaryKeySelective(xieChengCollidingDataPackage record);

    int updateByPrimaryKeyWithBLOBs(xieChengCollidingDataPackage record);

    int updateByPrimaryKey(xieChengCollidingDataPackage record);
}