package com.br.marketing.mapper;

import com.br.marketing.vo.xiecheng.XiechengCollidingStagingRuleVO;
import com.br.marketing.vo.xiecheng.param.CollidingRuleConfirmParam;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface XiechengCollidingDataPackageRuleStagingMapper extends XiechengCollidingDataPackageRuleStagingMapperBase{

    List<XiechengCollidingStagingRuleVO> getCollidingRuleStagingList();

    int updateStagingRule(CollidingRuleConfirmParam stagingRule);
}