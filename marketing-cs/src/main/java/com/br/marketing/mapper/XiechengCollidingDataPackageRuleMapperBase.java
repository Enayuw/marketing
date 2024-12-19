package com.br.marketing.mapper;

import com.br.marketing.entity.XiechengCollidingDataPackageRule;
import com.br.marketing.entity.XiechengCollidingDataPackageRuleExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface XiechengCollidingDataPackageRuleMapperBase {
    int countByExample(XiechengCollidingDataPackageRuleExample example);

    int deleteByExample(XiechengCollidingDataPackageRuleExample example);

    int deleteByPrimaryKey(Long id);

    int insert(XiechengCollidingDataPackageRule record);

    int insertSelective(XiechengCollidingDataPackageRule record);

    List<XiechengCollidingDataPackageRule> selectByExample(XiechengCollidingDataPackageRuleExample example);

    XiechengCollidingDataPackageRule selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") XiechengCollidingDataPackageRule record,
        @Param("example") XiechengCollidingDataPackageRuleExample example);

    int updateByExample(@Param("record") XiechengCollidingDataPackageRule record, @Param("example") XiechengCollidingDataPackageRuleExample example);

    int updateByPrimaryKeySelective(XiechengCollidingDataPackageRule record);

    int updateByPrimaryKey(XiechengCollidingDataPackageRule record);
}