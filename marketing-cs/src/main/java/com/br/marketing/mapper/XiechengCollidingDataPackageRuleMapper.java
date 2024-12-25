package com.br.marketing.mapper;

import com.br.marketing.entity.XiechengCollidingDataPackageRule;
import com.br.marketing.vo.xiecheng.XiechengCollidingRuleVO;
import com.br.marketing.vo.xiecheng.param.CollidingRuleListParam;
import com.br.marketing.vo.xiecheng.param.UpdateCollidingRuleParam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface XiechengCollidingDataPackageRuleMapper extends XiechengCollidingDataPackageRuleMapperBase {
    List<XiechengCollidingRuleVO> getCollidingRuleFalseList(@Param("listParam") CollidingRuleListParam listParam,
        @Param("orderByClause") String orderByClause);

    XiechengCollidingRuleVO getPackageRuleDetail(@Param("dprId") Long dprId);

    List<XiechengCollidingDataPackageRule> listByIds(@Param("ids") List<Long> ids);

    void deleteByIds(@Param("ids") List<Long> ids);

    List<XiechengCollidingDataPackageRule> getCollidingPackageRules();

    List<XiechengCollidingDataPackageRule> getMaxEndTimeGroupByPackageId(@Param("packageIds") List<Long> packageIds);

    List<XiechengCollidingDataPackageRule> getPackageRuleByPackageId(@Param("packageId") Long packageId);

    int updateCollidingRule(UpdateCollidingRuleParam param);


}