package com.br.marketing.mapper;

import com.br.marketing.entity.XieChengCollidingDataPackage;
import com.br.marketing.entity.XieChengCollidingDataPackageExample;
import java.util.List;
import org.apache.ibatis.annotations.Param;

public interface XieChengCollidingDataPackageMapperBase {
    long countByExample(XieChengCollidingDataPackageExample example);

    int deleteByExample(XieChengCollidingDataPackageExample example);

    int deleteByPrimaryKey(Long id);

    int insert(XieChengCollidingDataPackage record);

    int insertSelective(XieChengCollidingDataPackage record);

    List<XieChengCollidingDataPackage> selectByExample(XieChengCollidingDataPackageExample example);

    XieChengCollidingDataPackage selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") XieChengCollidingDataPackage record, @Param("example") XieChengCollidingDataPackageExample example);

    int updateByExample(@Param("record") XieChengCollidingDataPackage record, @Param("example") XieChengCollidingDataPackageExample example);

    int updateByPrimaryKeySelective(XieChengCollidingDataPackage record);

    int updateByPrimaryKey(XieChengCollidingDataPackage record);
}