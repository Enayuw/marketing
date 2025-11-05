package com.br.marketing.service.Impl.xc;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import com.br.marketing.common.exception.KnowException;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.dto.rulecenter.XcDeleteTaskQueryDTO;
import com.br.marketing.dto.rulecenter.XcDeleteTaskVO;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.*;
import com.br.marketing.vo.xiecheng.param.UpdateRoundParam;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.vo.xiecheng.XiechengCollidingRuleVO;
import com.br.marketing.vo.xiecheng.XiechengCollidingStagingRuleVO;
import com.br.marketing.vo.xiecheng.XiechengPackageVO;
import com.br.marketing.vo.xiecheng.param.CollidingRuleConfirmParam;
import com.br.marketing.vo.xiecheng.param.CollidingRuleListParam;
import com.br.marketing.vo.xiecheng.param.UpdateCollidingRuleParam;
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

    public static final ThreadPoolExecutor XIECHENG_ROB_DATA_DELETE_THREAD = BrExecutors.getThreadPool(5, 5);

    @Resource
    private XiechengCollidingDataPackageRuleMapper packageRuleMapper;

    @Resource
    private XieChengCollidingDataPackageMapper packageMapper;

    @Resource
    private XiechengCollidingDataPackageRuleStagingMapper stagingMapper;

    @Resource
    private XieChengCollidingDataLoopCycleMapper loopCycleMapper;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private XieChengCollidingDataRobMapper robMapper;

    @Resource
    private XiechengCollidingDataProcessTaskMapper xiechengCollidingDataProcessTaskMapper;

    @Resource
    private XiechengCollidingTaskBatchMapper xiechengCollidingTaskBatchMapper;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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
        PageHelper.startPage(listParam.getCurrent(), listParam.getSize());
        JSONObject orderConfig = marketingCommonConfig.getXieChengCustomizeOrderByClauseConfig().get("falseList");
        String orderField = orderConfig.getString(listParam.getOrderField());
        String orderByClause = StringUtils.isEmpty(orderField) ? null
            : orderField + " " + (StringUtils.isEmpty(listParam.getOrderType()) ? "" : listParam.getOrderType());
        List<XiechengCollidingRuleVO> packageRuleList = packageRuleMapper.getCollidingRuleFalseList(listParam, orderByClause);
        List<Map<String, Long>> remainingNumbers = robMapper.selectRemainingNumberstiflash_();
        Map<Long, String> remainingNumbersMap =
            remainingNumbers.stream().collect(Collectors.toMap(remainingNumber -> remainingNumber.get("packageId"),
                remainingNumber -> String.valueOf(remainingNumber.get("remainingNumber")), (existingValue, newValue) -> existingValue));
        packageRuleList.forEach((XiechengCollidingRuleVO rule) -> {
            rule.setRemainingNumber(remainingNumbersMap.getOrDefault(rule.getPkgId(), "0"));
        });
        return PageResultReturn.setPageResult(packageRuleList, listParam.getCurrent(), listParam.getSize());
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
        JSONObject orderConfig = marketingCommonConfig.getXieChengCustomizeOrderByClauseConfig().get("trueList");
        String orderField = orderConfig.getString(listParam.getOrderField());
        String orderByClause = StringUtils.isEmpty(orderField) ? null
            : orderField + " " + (StringUtils.isEmpty(listParam.getOrderType()) ? "" : listParam.getOrderType());
        listParam.setApiCode(marketingCommonConfig.getXieChengCustomizeTrueApiCode());
        return loopCycleMapper.getCollidingRuleTrueListtiflash_(listParam, orderByClause);
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
     * 更新撞库规则
     *
     * @param param 更新参数
     * @return {@link Boolean }
     * @author senyang.zheng
     * @date 2024/04/24
     */
    @Override
    public Boolean updateCollidingRule(UpdateCollidingRuleParam param) {
        //校验collidingTimePoints
        String collidingTimePoints = param.getStartTimes();
        if (StringUtils.isEmpty(collidingTimePoints)) {
            throw new KnowException("请设置撞库时间！");
        }
        String[] timePoints = collidingTimePoints.split(",");
        for (String timePoint : timePoints) {
            if (!timePoint.matches(Constants.TIME_MINUTE_REGEX)) {
                throw new KnowException("撞库时间格式有误！");
            }
        }
        if (StringUtils.hasDuplicate(timePoints)) {
            throw new KnowException("撞库时间不可重复设置！");
        }
        param.setCollidingStartTime(DateUtil.parse(param.getCollidingStartTime(),
                DatePattern.NORM_DATETIME_PATTERN).toStringDefaultTimeZone());
        param.setCollidingEndTime(DateUtil.parse(param.getCollidingEndTime(),
                DatePattern.NORM_DATETIME_PATTERN).toStringDefaultTimeZone());
        return packageRuleMapper.updateCollidingRule(param) == 1;
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
        packageIds.stream().filter(this::checkPackageId).forEach((Long packageId) -> {
            XieChengCollidingDataPackage delete = new XieChengCollidingDataPackage();
            delete.setIsDelete(1);
            delete.setId(packageId);
            packageMapper.updateByPrimaryKeySelective(delete);
            // 异步删除操作
            CompletableFuture.runAsync(() -> deleteRobDataByPackageId(packageId), XIECHENG_ROB_DATA_DELETE_THREAD);
        });
        return Boolean.TRUE;
    }

    public void deleteRobDataByPackageId(Long packageId) {
        XieChengCollidingDataRobExample example = new XieChengCollidingDataRobExample();
        example.createCriteria().andIsDeleteEqualTo(0).andPackageIdEqualTo(packageId);
        int deleteCount = robMapper.countByExample(example);
        int limit = 10000;
        while (deleteCount > 0) {
            robMapper.batchDeleteRobDataByPackageId(packageId, limit);
            deleteCount -= limit;
        }
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
    public Long confirmCollidingRule(CollidingRuleConfirmParam confirmParam) {
        //校验collidingTimePoints
        String collidingTimePoints = confirmParam.getStartTimes();
        if (StringUtils.isEmpty(collidingTimePoints)) {
            throw new KnowException("请设置撞库时间！");
        }
        String[] timePoints = collidingTimePoints.split(",");
        for (String timePoint : timePoints) {
            if (!timePoint.matches(Constants.TIME_MINUTE_REGEX)) {
                throw new KnowException("撞库时间格式有误！");
            }
        }
        if (StringUtils.hasDuplicate(timePoints)) {
            throw new KnowException("撞库时间不可重复设置！");
        }
        //判断timePoints是否重复
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
        if (confirmParam.getPrsId() == null) {
            XiechengCollidingDataPackageRuleStaging stagingRule = new XiechengCollidingDataPackageRuleStaging();
            stagingRule.setApiCode(confirmParam.getApiCode());
            stagingRule.setPackageId(confirmParam.getPackageId());
            stagingRule.setCollidingDataTaskId(confirmParam.getCollidingDataTaskId());
            stagingRule.setCollidingBackNumber(confirmParam.getCollidingBackNumber());
            stagingRule.setCollidingStartTime(DateUtil.parse(confirmParam.getCollidingStartTime(), DatePattern.NORM_DATETIME_PATTERN));
            stagingRule.setCollidingEndTime(DateUtil.parse(confirmParam.getCollidingEndTime(), DatePattern.NORM_DATETIME_PATTERN));
            stagingRule.setCollidingTimes(confirmParam.getCollidingTimes());
            stagingRule.setStartTimes(collidingTimePoints);
            stagingMapper.insertSelective(stagingRule);
            return stagingRule.getId();
        } else {
            stagingMapper.updateStagingRule(confirmParam);
            return confirmParam.getPrsId();
        }
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
            insert.setStartTimes(stagingRule.getStartTimes());
            packageRuleMapper.insertSelective(insert);
            stagingRule.setIsDelete(1);
            stagingMapper.updateByPrimaryKeySelective(stagingRule);
        });
        return new ApiResult<Boolean>().success(Boolean.TRUE);
    }

    /**
     * 删除已确认暂存规则
     *
     * @param prsId prs id
     * @return {@link Boolean }
     * @author senyang.zheng
     * @date 2024/04/24
     */
    @Override
    public Boolean deleteStagingCollidingRule(Long prsId) {
        return stagingMapper.deleteByPrimaryKey(prsId) == 1;
    }

    /**
     * 修改包轮次
     *
     * @param param param
     * @return {@link Boolean }
     * @author hong.chen
     * @date 2024/08/07
     */
    @Override
    public Boolean updateRound(UpdateRoundParam param) {
        XieChengCollidingDataPackage update = new XieChengCollidingDataPackage();
        update.setRound(param.getRound());
        update.setId(param.getPkgId());
        return packageMapper.updateByPrimaryKeySelective(update) == 1;
    }

    @Override
    public PageResultReturn<XcDeleteTaskVO> getCollidingDataDeleteTaskList(XcDeleteTaskQueryDTO queryDTO) {
        PageHelper.startPage(queryDTO.getCurrent(), queryDTO.getSize());
        LocalDateTime releaseTimeBegin = null;
        LocalDateTime releaseTimeEnd = null;
        if(StringUtils.isNotBlank(queryDTO.getReleaseTimeBegin()) && StringUtils.isNotBlank(queryDTO.getReleaseTimeEnd())){
            releaseTimeBegin = LocalDateTime.parse(queryDTO.getReleaseTimeBegin(), formatter);
            releaseTimeEnd = LocalDateTime.parse(queryDTO.getReleaseTimeEnd(), formatter);
        }
        List<XcDeleteTaskVO> taskList = xiechengCollidingDataProcessTaskMapper
                .getCollidingDataDeleteTaskList(releaseTimeBegin, releaseTimeEnd, queryDTO.getTaskType(), queryDTO.getTaskStatus());
        return PageResultReturn.setPageResult(taskList, queryDTO.getCurrent(), queryDTO.getSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteCollidingDataDeleteTask(Long taskId) {
        //1.逻辑删除【b_xiecheng_colliding_data_process_task】
        XiechengCollidingDataProcessTaskExample taskExample = new XiechengCollidingDataProcessTaskExample();
        taskExample.createCriteria().andIdEqualTo(taskId);
        XiechengCollidingDataProcessTask task = new XiechengCollidingDataProcessTask();
        task.setIsDelete(Constants.DATA_VALID);
        xiechengCollidingDataProcessTaskMapper.updateByExampleSelective(task, taskExample);
        //2.逻辑删除【b_xiecheng_colliding_task_batch】
        XiechengCollidingTaskBatchExample batchExample = new XiechengCollidingTaskBatchExample();
        batchExample.createCriteria().andCollidingDataTaskIdEqualTo(taskId);
        XiechengCollidingTaskBatch batch = new XiechengCollidingTaskBatch();
        batch.setIsDelete(Constants.DATA_VALID);
        xiechengCollidingTaskBatchMapper.updateByExampleSelective(batch, batchExample);
        return true;
    }

}
