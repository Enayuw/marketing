package com.br.marketing.mapper;

import com.br.marketing.entity.XiechengCollidingDataPackageRuleStaging;
import com.br.marketing.entity.XiechengCollidingDataPackageRuleStagingExample;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface XiechengCollidingDataPackageRuleStagingMapperBase {
    int countByExample(XiechengCollidingDataPackageRuleStagingExample example);

    int deleteByExample(XiechengCollidingDataPackageRuleStagingExample example);

    int deleteByPrimaryKey(Long id);

    int insert(XiechengCollidingDataPackageRuleStaging record);

    int insertSelective(XiechengCollidingDataPackageRuleStaging record);

    List<XiechengCollidingDataPackageRuleStaging> selectByExample(XiechengCollidingDataPackageRuleStagingExample example);

    XiechengCollidingDataPackageRuleStaging selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") XiechengCollidingDataPackageRuleStaging record,
                                 @Param("example") XiechengCollidingDataPackageRuleStagingExample example);

    int updateByExample(@Param("record") XiechengCollidingDataPackageRuleStaging record,
                        @Param("example") XiechengCollidingDataPackageRuleStagingExample example);

    int updateByPrimaryKeySelective(XiechengCollidingDataPackageRuleStaging record);

    int updateByPrimaryKey(XiechengCollidingDataPackageRuleStaging record);
}