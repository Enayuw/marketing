package com.br.marketing.mapper;

import com.br.marketing.entity.XieChengCollidingDataPackage;
import com.br.marketing.entity.xieChengCollidingDataPackageExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface XieChengCollidingDataPackageMapper {
    int countByExample(xieChengCollidingDataPackageExample example);

    int deleteByExample(xieChengCollidingDataPackageExample example);

    int deleteByPrimaryKey(Long id);

    int insert(XieChengCollidingDataPackage record);

    int insertSelective(XieChengCollidingDataPackage record);

    List<XieChengCollidingDataPackage> selectByExampleWithBLOBs(xieChengCollidingDataPackageExample example);

    List<XieChengCollidingDataPackage> selectByExample(xieChengCollidingDataPackageExample example);

    XieChengCollidingDataPackage selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") XieChengCollidingDataPackage record, @Param("example") xieChengCollidingDataPackageExample example);

    int updateByExampleWithBLOBs(@Param("record") XieChengCollidingDataPackage record, @Param("example") xieChengCollidingDataPackageExample example);

    int updateByExample(@Param("record") XieChengCollidingDataPackage record, @Param("example") xieChengCollidingDataPackageExample example);

    int updateByPrimaryKeySelective(XieChengCollidingDataPackage record);

    int updateByPrimaryKeyWithBLOBs(XieChengCollidingDataPackage record);

    int updateByPrimaryKey(XieChengCollidingDataPackage record);
}