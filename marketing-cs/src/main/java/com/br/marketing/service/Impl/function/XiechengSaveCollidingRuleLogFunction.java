package com.br.marketing.service.Impl.function;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.br.marketing.entity.XieChengCollidingDataPackage;
import com.br.marketing.entity.XiechengCollidingDataPackageRuleStaging;
import com.br.marketing.entity.XiechengCollidingDataPackageRuleStagingExample;
import com.br.marketing.mapper.XieChengCollidingDataPackageMapper;
import com.br.marketing.mapper.XiechengCollidingDataPackageRuleStagingMapper;
import com.br.marketing.service.CustomFunction;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;

/**
 * 携程保存撞库规则日志Function
 *
 * @author senyang.zheng
 * @date 2024/04/24
 */
@Component
public class XiechengSaveCollidingRuleLogFunction implements CustomFunction {

    @Resource
    private XiechengCollidingDataPackageRuleStagingMapper stagingMapper;
    @Resource
    private XieChengCollidingDataPackageMapper packageMapper;

    /**
     * 自定义函数名
     *
     * @return 自定义函数名
     */
    @Override
    public String functionName() {
        return "saveCollidingRuleLog";
    }

    /**
     * 最终执行的方法
     *
     * @param param 参数
     * @return 执行结果
     */
    @Override
    public String apply(Object param) {
        StringBuilder res = new StringBuilder();
        XiechengCollidingDataPackageRuleStagingExample example = new XiechengCollidingDataPackageRuleStagingExample();
        example.createCriteria().andIsDeleteEqualTo(0);
        List<XiechengCollidingDataPackageRuleStaging> stagingRuleList = stagingMapper.selectByExample(example);
        if (CollectionUtil.isEmpty(stagingRuleList)) {
            res.append("请确认数据包规则是否已确认！");
            return res.toString();
        }
        List<Long> packageIds =
            stagingRuleList.stream().map(XiechengCollidingDataPackageRuleStaging::getPackageId).distinct().collect(Collectors.toList());
        if (packageIds.size() > 1) {
            res.append("存在多个数据包，已确认未保存数据！");
            return res.toString();
        }
        Long packageId = packageIds.get(0);
        XieChengCollidingDataPackage dataPackage = packageMapper.selectByPrimaryKey(packageId);
        res.append("创建").append(dataPackage.getPackageName()).append("具体规则为:\n");
        stagingRuleList.forEach((XiechengCollidingDataPackageRuleStaging stagingRule) -> res.append("开启撞库时间：")
            .append(DateUtil.format(stagingRule.getCollidingStartTime(), DatePattern.NORM_DATETIME_PATTERN)).append(" —— ")
            .append(DateUtil.format(stagingRule.getCollidingEndTime(), DatePattern.NORM_DATETIME_PATTERN)).append(" 的设定撞得量级 ")
            .append(stagingRule.getCollidingBackNumber()).append(" 设定撞得次数").append(stagingRule.getCollidingTimes()).append("\n"));
        return res.toString();
    }
}
