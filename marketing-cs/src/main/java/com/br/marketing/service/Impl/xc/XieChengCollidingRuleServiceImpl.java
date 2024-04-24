package com.br.marketing.service.Impl.xc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.entity.XieChengCollidingDataPackage;
import com.br.marketing.entity.XiechengCollidingDataPackageRule;
import com.br.marketing.entity.XiechengCollidingDataPackageRuleExample;
import com.br.marketing.entity.XiechengCollidingDataPackageRuleStaging;
import com.br.marketing.entity.XiechengCollidingDataPackageRuleStagingExample;
import com.br.marketing.mapper.XieChengCollidingDataLoopCycleMapper;
import com.br.marketing.mapper.XieChengCollidingDataPackageMapper;
import com.br.marketing.mapper.XiechengCollidingDataPackageRuleMapper;
import com.br.marketing.mapper.XiechengCollidingDataPackageRuleStagingMapper;
import com.br.marketing.vo.xiecheng.XiechengCollidingRuleVO;
import com.br.marketing.vo.xiecheng.XiechengCollidingStagingRuleVO;
import com.br.marketing.vo.xiecheng.XiechengPackageVO;
import com.br.marketing.vo.xiecheng.param.CollidingRuleConfirmParam;
import com.br.marketing.vo.xiecheng.param.CollidingRuleListParam;
import com.br.marketing.vo.xiecheng.param.UpdateCollidingSwitchParam;
import com.br.marketing.vo.xiecheng.param.UpdatePriorityParam;
import com.github.pagehelper.PageHelper;
import com.google.common.base.Splitter;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class XieChengCollidingRuleServiceImpl implements XieChengCollidingRuleService {

    @Resource
    private XiechengCollidingDataPackageRuleMapper packageRuleMapper;

    @Resource
    private XieChengCollidingDataPackageMapper packageMapper;

    @Resource
    private XiechengCollidingDataPackageRuleStagingMapper stagingMapper;
    @Resource
    private XieChengCollidingDataLoopCycleMapper loopCycleMapper;

    /**
     * 获取调度任务列表-False-分页
     *
     * @param listParam 列表参数
     * @return {@link PageResultReturn }<{@link XiechengCollidingRuleVO }>
     * @author senyang.zheng
     * @date 2024/04/23
     */
    @Override
    public PageResultReturn<XiechengCollidingRuleVO> getCollidingRuleFalseList(CollidingRuleListParam listParam) {
        PageHelper.startPage(listParam.getPage(), listParam.getPageSize());
        List<XiechengCollidingRuleVO> packageRuleList = packageRuleMapper.getCollidingRuleFalseList(listParam);
        return PageResultReturn.setPageResult(packageRuleList, listParam.getPage(), listParam.getPageSize());
    }

    /**
     * 获取调度任务列表-True-不分页
     *
     * @param listParam 列表参数
     * @return {@link List }<{@link XiechengCollidingRuleVO }>
     * @author senyang.zheng
     * @date 2024/04/24
     */
    @Override
    public List<XiechengCollidingRuleVO> getCollidingRuleTrueList(CollidingRuleListParam listParam) {
        return loopCycleMapper.getCollidingRuleTrueList(listParam);
    }

    /**
     * 修改包优先级
     *
     * @param param param
     * @return {@link Boolean }
     * @author senyang.zheng
     * @date 2024/04/23
     */
    @Override
    public Boolean updatePriority(UpdatePriorityParam param) {
        XieChengCollidingDataPackage update = new XieChengCollidingDataPackage();
        update.setPriority(param.getPriority());
        update.setId(param.getPkgId());
        return packageMapper.updateByPrimaryKeySelective(update) == 1;
    }

    /**
     * 获取携程撞库规则详情
     *
     * @param dprId dpr id
     * @return {@link XiechengCollidingRuleVO }
     * @author senyang.zheng
     * @date 2024/04/23
     */
    @Override
    public XiechengCollidingRuleVO getCollidingRuleDetail(Long dprId) {
        return packageRuleMapper.getPackageRuleDetail(dprId);
    }

    /**
     * 变更任务状态
     *
     * @param param param
     * @return {@link Boolean }
     * @author senyang.zheng
     * @date 2024/04/23
     */
    @Override
    public Boolean updateCollidingSwitch(UpdateCollidingSwitchParam param) {
        XiechengCollidingDataPackageRule update = new XiechengCollidingDataPackageRule();
        update.setCollidingSwitch(param.getCollidingSwitch());
        update.setId(param.getDprId());
        return packageRuleMapper.updateByPrimaryKeySelective(update) == 1;
    }

    /**
     * 删除撞库规则
     *
     * @param dprIds dpr ids
     * @return {@link Boolean }
     * @author senyang.zheng
     * @date 2024/04/23
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteCollidingRules(String dprIds) {
        List<Long> ids = splitToLongList(dprIds, ",");
        if (CollectionUtil.isEmpty(ids)) {
            return Boolean.FALSE;
        }
        List<XiechengCollidingDataPackageRule> packageRuleList = packageRuleMapper.listByIds(ids);
        List<Long> packageIds = packageRuleList.stream().map(XiechengCollidingDataPackageRule::getPackageId).distinct().collect(Collectors.toList());
        packageRuleMapper.deleteByIds(ids);
        packageIds.stream().filter(this::checkPackageId) // 过滤出满足条件的 packageId
            .forEach(packageId -> {
                XieChengCollidingDataPackage delete = new XieChengCollidingDataPackage();
                delete.setIsDelete(1);
                delete.setId(packageId);
                packageMapper.updateByPrimaryKeySelective(delete);
            });
        return Boolean.TRUE;
    }

    private Boolean checkPackageId(Long packageId) {
        XiechengCollidingDataPackageRuleExample example = new XiechengCollidingDataPackageRuleExample();
        example.createCriteria().andPackageIdEqualTo(packageId).andIsDeleteEqualTo(0);
        return packageRuleMapper.countByExample(example) == 0;
    }

    public static List<Long> splitToLongList(String str, String separator) {
        return Optional.ofNullable(str).filter(s -> !s.trim().isEmpty()).map(s -> Splitter.on(separator).splitToList(s))
            .orElseGet(Collections::emptyList).stream().map(Long::parseLong).collect(Collectors.toList());
    }

    /**
     * 获取撞库数据包下拉列表-不分页
     *
     * @return {@link List }<{@link XiechengPackageVO }>
     * @author senyang.zheng
     * @date 2024/04/24
     */
    @Override
    public List<XiechengPackageVO> getPackageList() {
        return packageMapper.getPackageList();
    }

    /**
     * 确认/暂存 撞库规则
     *
     * @param confirmParam 确认参数
     * @return {@link Boolean }
     * @author senyang.zheng
     * @date 2024/04/24
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean confirmCollidingRule(CollidingRuleConfirmParam confirmParam) {
        XiechengCollidingDataPackageRuleStagingExample example = new XiechengCollidingDataPackageRuleStagingExample();
        example.createCriteria().andIsDeleteEqualTo(0).andPackageIdNotEqualTo(confirmParam.getPackageId());
        int hisCount = stagingMapper.countByExample(example);
        // 删除暂存记录中其他包的数据
        if (hisCount != 0) {
            XiechengCollidingDataPackageRuleStaging delete = new XiechengCollidingDataPackageRuleStaging();
            delete.setIsDelete(1);
            delete.setPackageId(confirmParam.getPackageId());
            stagingMapper.updateByExample(delete, example);
        }
        XiechengCollidingDataPackageRuleStaging insert = new XiechengCollidingDataPackageRuleStaging();
        insert.setApiCode(confirmParam.getApiCode());
        insert.setPackageId(confirmParam.getPackageId());
        insert.setCollidingDataTaskId(confirmParam.getCollidingDataTaskId());
        insert.setCollidingBackNumber(confirmParam.getCollidingBackNumber());
        insert.setCollidingStartTime(DateUtil.parse(confirmParam.getCollidingStartTime(), DatePattern.NORM_DATETIME_PATTERN));
        insert.setCollidingEndTime(DateUtil.parse(confirmParam.getCollidingEndTime(), DatePattern.NORM_DATETIME_PATTERN));
        insert.setCollidingTimes(confirmParam.getCollidingTimes());
        return stagingMapper.insert(insert) == 1;
    }

    /**
     * 获取暂存规则列表
     *
     * @return {@link List }<{@link XiechengCollidingStagingRuleVO }>
     * @author senyang.zheng
     * @date 2024/04/24
     */
    @Override
    public List<XiechengCollidingStagingRuleVO> getCollidingRuleStagingList() {
        return stagingMapper.getCollidingRuleStagingList();
    }

    /**
     * 保存撞库规则
     *
     * @return {@link ApiResult }<{@link Boolean }>
     * @author senyang.zheng
     * @date 2024/04/24
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<Boolean> saveCollidingRule() {
        XiechengCollidingDataPackageRuleStagingExample example = new XiechengCollidingDataPackageRuleStagingExample();
        example.createCriteria().andIsDeleteEqualTo(0);
        List<XiechengCollidingDataPackageRuleStaging> stagingRuleList = stagingMapper.selectByExample(example);
        if (CollectionUtil.isEmpty(stagingRuleList)) {
            return new ApiResult<Boolean>().fail(Boolean.FALSE, "请确认数据包规则是否已确认！");
        }
        List<Long> packageIds =
            stagingRuleList.stream().map(XiechengCollidingDataPackageRuleStaging::getPackageId).distinct().collect(Collectors.toList());
        if (packageIds.size() > 1) {
            return new ApiResult<Boolean>().fail(Boolean.FALSE, "存在多个数据包，已确认未保存数据！");
        }
        stagingRuleList.forEach((XiechengCollidingDataPackageRuleStaging stagingRule) -> {
            XiechengCollidingDataPackageRule insert = new XiechengCollidingDataPackageRule();
            insert.setApiCode(stagingRule.getApiCode());
            insert.setPackageId(stagingRule.getPackageId());
            insert.setCollidingDataTaskId(stagingRule.getCollidingDataTaskId());
            insert.setCollidingBackNumber(stagingRule.getCollidingBackNumber());
            insert.setCollidingStartTime(stagingRule.getCollidingStartTime());
            insert.setCollidingEndTime(stagingRule.getCollidingEndTime());
            insert.setCollidingTimes(stagingRule.getCollidingTimes());
            packageRuleMapper.insert(insert);
        });
        return new ApiResult<Boolean>().success(Boolean.TRUE);
    }

}
