package com.br.marketing.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.br.marketing.entity.XiechengCollidingDataPackageRule;
import com.br.marketing.vo.xiecheng.XiechengCollidingRuleVO;
import com.br.marketing.vo.xiecheng.param.CollidingRuleListParam;

@Mapper
public interface XiechengCollidingDataPackageRuleMapper extends XiechengCollidingDataPackageRuleMapperBase {
    List<XiechengCollidingRuleVO> getCollidingRuleFalseList(@Param("listParam") CollidingRuleListParam listParam,
        @Param("orderByClause") String orderByClause);

    XiechengCollidingRuleVO getPackageRuleDetail(@Param("dprId") Long dprId);

    List<XiechengCollidingDataPackageRule> listByIds(@Param("ids") List<Long> ids);

    void deleteByIds(@Param("ids") List<Long> ids);

    List<XiechengCollidingDataPackageRule> getCollidingPackageRules();

    List<XiechengCollidingDataPackageRule> getMaxEndTimeGroupByPackageId(@Param("packageIds") List<Long> packageIds);
}