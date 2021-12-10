package com.br.marketing.service.Impl;

import com.br.common.util.DateUtils;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.exception.BusinessException;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.userinfo.UserDetail;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.*;
import com.br.marketing.service.FastTaskRuleService;
import com.br.marketing.vo.*;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 手动跑数任务规则 业务实现
 * songjuanjuan
 */
@Service
@Slf4j
public class FastTaskRuleServiceImpl implements FastTaskRuleService {

    @Autowired
    private FastTaskRuleMapper fastTaskRuleMapper;

    @Autowired
    private MarketingSyncReportMapper syncReportMapper;

    @Autowired
    private ScoreRuleConfigMapper scoreRuleConfigMapper;

    @Autowired
    private MarketingCustomerMapper marketingCustomerMapper;

    @Autowired
    private RedisChgService redisChgService;

    @Override
    public PageResultReturn list(int current, int size, String search, Integer status, String createTimeStart, String createTimeEnd,
                                 String updateTimeStart, String updateTimeEnd, String taskStatus) {

        if (StringUtils.isNotEmpty(createTimeEnd)){
            createTimeEnd = DateUtils.format(addDay(createTimeEnd, 1, "yyyy-MM-dd"), "yyyy-MM-dd");
        }
        if (StringUtils.isNotEmpty(updateTimeEnd)){
            updateTimeEnd = DateUtils.format(addDay(updateTimeEnd, 1, "yyyy-MM-dd"), "yyyy-MM-dd");
        }
        if (StringUtils.isNotEmpty(search) && search.contains("_")){
            search = search.replace("_", "\\_");
        }

        PageHelper.startPage(current, size);
        List<FastTaskRuleListVO> fastTaskRuleListVOS = fastTaskRuleMapper.selectList(search,status,
                createTimeStart,createTimeEnd,updateTimeStart,updateTimeEnd,taskStatus);
        return PageResultReturn.setPageResult(fastTaskRuleListVOS, current,size);
    }

    private Date addDay(String date, Integer addDays, String format) {
        Calendar c = Calendar.getInstance();
        Date time = null;
        try {
            Date endTime = DateUtils.parse(date, format);
            c.setTime(endTime);
            c.add(Calendar.DAY_OF_MONTH, addDays);
            time = c.getTime();
        } catch (ParseException e) {
            log.error("date:{} is error", date, e);
        }
        return time;
    }

    @Override
    @Transactional
    public ApiResult<Boolean> save(FastTaskRuleDetailVO vo, UserDetail userDetail) {
        String[] split = vo.getRuleIds().split(",");
        Integer i = 1;
        String dataCondition = getDataCondition(vo.getDataIdDesc());
        for(String s : split){
            FastTaskRule fastTaskRule = new FastTaskRule();
            //如果一个配置 建多个任务，任务名称后加数字区分
            if(split.length>1){
                fastTaskRule.setRuleName(vo.getRuleName()+i.toString());
                i++;
            }else {
                fastTaskRule.setRuleName(vo.getRuleName());
            }
            fastTaskRule.setRuleNumber(createNo());//任务编号
            //fastTaskRule.setRuleNumber("F20211210455");//任务编号
            fastTaskRule.setTaskType(vo.getTaskType());//跑分类型
            fastTaskRule.setDataIdDesc(vo.getDataIdDesc());//跑分数据,逗号分隔
            fastTaskRule.setDataType(vo.getDataType());//跑分范围
            fastTaskRule.setRuleId(Long.parseLong(s));//跑分规则
            fastTaskRule.setTaskTime(vo.getTaskTime());//跑分日期

            fastTaskRule.setApiCode(vo.getApiCode());
            fastTaskRule.setDataCondition(dataCondition);
            ScoreRuleConfig scoreRuleConfig = scoreRuleConfigMapper.selectByPrimaryKey(Long.parseLong(s));
            fastTaskRule.setStrategyId(scoreRuleConfig.getStrategyId());
            //fastTaskRule.setProductInfo("");
            fastTaskRule.setProductField(scoreRuleConfig.getStrategyProductJson());
            fastTaskRule.setCallbackInfo(scoreRuleConfig.getBaseInfo());
            fastTaskRule.setStatus(1);
            fastTaskRule.setOptId(userDetail.getUserId());
            fastTaskRule.setOptName(userDetail.getUsername());
            fastTaskRule.setIsDel(1);
            fastTaskRule.setCreateTime(new Date());
            fastTaskRule.setUpdateTime(new Date());
            fastTaskRuleMapper.insert(fastTaskRule);
        }

        return new ApiResult<Boolean>().success(true);
    }

    private String getDataCondition(String dataIdDesc) {
        String[] split = dataIdDesc.split(",");
        StringBuilder dataCondition = new StringBuilder("[");
        for(String s : split){
            MarketingSyncReport marketingSyncReport = syncReportMapper.selectByPrimaryKey(Long.parseLong(s));
            dataCondition.append("{"+marketingSyncReport.getAppletDate()+","+marketingSyncReport.getUserType()+"}");
        }
        return dataCondition.append("]").toString();
    }

    /**
     * 2021/9/3 19:10
     * 以天为维度生成递增的编号
     * 编码规则：R+日期+序号（三位） 例如：R20210903001,R20210903002,...,R20210903999
     */
    private String createNo() {
        String yyyyMMdd6 = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String key = "marketing:inner:".concat(yyyyMMdd6);
        Long index = redisChgService.incr(key);
        if (index > 999) {
            throw new BusinessException("很遗憾小主，今天的规则编号(".concat(yyyyMMdd6) + "999)已经用尽");
        }
        redisChgService.expire(key, getKeyExpiration());
        String prefix3 = String.format("%03d", index);
        return "F".concat(yyyyMMdd6.concat(prefix3));
    }

    /**
     * 获取当前时间到第二天凌晨的秒
     *
     * @dateTime 2021/10/19 9:21
     */
    private int getKeyExpiration() {
        LocalDateTime now = LocalDateTime.now();
        // 当前毫秒数
        long l = now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        LocalDateTime localDateTime = now.plusDays(1);
        // 第二天凌晨毫秒数
        long l1 = localDateTime.toLocalDate().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        return (int) (l1 - l) / 1000;
    }


    @Override
    public FastTaskRuleDetailVO getFastTask(String id) {
        FastTaskRule fastTaskRule = fastTaskRuleMapper.selectByPrimaryKey(Long.parseLong(id));
        FastTaskRuleDetailVO vo = new FastTaskRuleDetailVO();
        BeanUtils.copyProperties(fastTaskRule, vo);
        //跑分数据:
        String[] split = fastTaskRule.getDataIdDesc().split(",");
        StringBuilder dataCondition = new StringBuilder("[");
        for(String s : split){
            MarketingSyncReport marketingSyncReport = syncReportMapper.selectByPrimaryKey(Long.parseLong(s));
            dataCondition.append("{"+marketingSyncReport.getAppletDate()+","+marketingSyncReport.getApiCode()
                    +","+marketingSyncReport.getUserType()+","+marketingSyncReport.getDuplicateRemovalNum()
                    +"}");
        }
        dataCondition.append("]");

        vo.setDataCondition(dataCondition.toString());
        vo.setScoreRules(null);//还没确定展示什么格式
        return vo;
    }

    @Override
    public boolean updateStatusById(String id, Integer status,UserDetail userDetail) {
        try {
            FastTaskRule fastTaskRule = new FastTaskRule();
            fastTaskRule.setId(Long.parseLong(id));
            fastTaskRule.setStatus(status);
            fastTaskRule.setUpdateTime(new Date());
            fastTaskRule.setOptId(userDetail.getUserId());
            fastTaskRule.setOptName(userDetail.getUsername());
            fastTaskRuleMapper.updateByPrimaryKeySelective(fastTaskRule);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            log.error(e.getMessage(),e);
            return false;
        }
    }

    @Override
    public ApiResult<Boolean> update(String id,String ruleName, String taskTime, UserDetail user) {
        try {
            FastTaskRule fastTaskRule = new FastTaskRule();
            fastTaskRule.setId(Long.parseLong(id));
            fastTaskRule.setRuleName(ruleName);
            fastTaskRule.setTaskTime(taskTime);
            fastTaskRule.setUpdateTime(new Date());
            fastTaskRule.setOptId(user.getUserId());
            fastTaskRule.setOptName(user.getUsername());
            fastTaskRuleMapper.updateByPrimaryKeySelective(fastTaskRule);
            return new ApiResult<Boolean>().success(true);
        }catch (Exception e){
            e.printStackTrace();
            log.error(e.getMessage(),e);
            return new ApiResult<Boolean>().success(false);
        }

    }

}
