package com.br.marketing.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.br.marketing.vo.xiecheng.PackageRuleListParam;
import com.br.marketing.vo.xiecheng.XiechengCollidingRuleVO;

@Mapper
public interface XiechengCollidingDataPackageRuleMapper extends XiechengCollidingDataPackageRuleMapperBase {
    List<XiechengCollidingRuleVO> getPackageRuleList(@Param("listParam") PackageRuleListParam listParam);
}