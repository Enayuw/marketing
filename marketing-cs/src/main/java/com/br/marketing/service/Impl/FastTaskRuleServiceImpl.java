package com.br.marketing.service.Impl;

import com.br.common.util.DateUtils;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.exception.BusinessException;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.entity.FastTaskRule;
import com.br.marketing.entity.MarketingSyncReport;
import com.br.marketing.entity.ScoreRuleConfig;
import com.br.marketing.entity.auth.MarketingUserDetail;
import com.br.marketing.mapper.FastTaskRuleMapper;
import com.br.marketing.mapper.MarketingSyncReportMapper;
import com.br.marketing.mapper.ScoreRuleConfigMapper;
import com.br.marketing.service.FastTaskRuleService;
import com.br.marketing.vo.FastTaskRuleDetailVO;
import com.br.marketing.vo.FastTaskRuleListVO;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

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
    private RedisChgService redisChgService;

    @Override
    public PageResultReturn list(int current, int size, String search, Integer status, String createTimeStart, String createTimeEnd,
                                 String updateTimeStart, String updateTimeEnd, Integer taskStatus) {

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
    @Deprecated
    public ApiResult<Boolean> save(FastTaskRuleDetailVO vo, MarketingUserDetail userDetail) {
        String[] split = vo.getRuleIds().split(",");
        Integer i = 1;
        String dataCondition = getDataCondition(vo.getDataIdDesc());
        for(String s : split){
            FastTaskRule fastTaskRule = new FastTaskRule();
            //如果一个配置 建多个任务，任务名称后加数字区分
            if(split.length>1){
                fastTaskRule.setRuleName(vo.getRuleName()+"_"+i.toString());
                i++;
            }else {
                fastTaskRule.setRuleName(vo.getRuleName());
            }
            fastTaskRule.setRuleNumber(createNo());//任务编号
            //fastTaskRule.setRuleNumber("F20211210008");//任务编号
            fastTaskRule.setTaskType(vo.getTaskType());//跑分类型
            fastTaskRule.setDataIdDesc(vo.getDataIdDesc());//跑分数据,逗号分隔
            fastTaskRule.setUntaskNum(vo.getUntaskNum());//未跑分数据量
            fastTaskRule.setDataType(vo.getDataType());//跑分范围
            fastTaskRule.setRuleId(Long.parseLong(s));//跑分规则
            fastTaskRule.setTaskTime(vo.getTaskTime());//跑分日期
            fastTaskRule.setApiCode(vo.getApiCode());
            fastTaskRule.setDataCondition(dataCondition);

            //从跑分规则表 复制
            ScoreRuleConfig scoreRuleConfig = scoreRuleConfigMapper.selectByPrimaryKey(Long.parseLong(s));
            fastTaskRule.setStrategyId(scoreRuleConfig.getStrategyId());
            fastTaskRule.setProductInfo(scoreRuleConfig.getProductInfo());
            fastTaskRule.setProductField(scoreRuleConfig.getStrategyProductJson());
            fastTaskRule.setCallbackInfo(scoreRuleConfig.getBaseInfo());
            fastTaskRule.setTaskType(scoreRuleConfig.getTaskType());

            fastTaskRule.setStatus(1);
            fastTaskRule.setOptId(String.valueOf(userDetail.getId()));
            fastTaskRule.setOptName(userDetail.getUserName());
            fastTaskRule.setIsDel(1);
            fastTaskRule.setCreateTime(new Date());
            fastTaskRule.setUpdateTime(new Date());
            fastTaskRuleMapper.insert(fastTaskRule);
        }

        return new ApiResult<Boolean>().success(true);
    }

    private String getDataCondition(String dataIdDesc) {
        String[] split = dataIdDesc.split(",");
        final char ch = ',';
        StringBuilder dataCondition = new StringBuilder("[");
        for(String s : split){
            //[{"appletDate":"2021-12-02","userType":"S01"},{"appletDate":"2021-12-02","userType":"S02"}]
            MarketingSyncReport marketingSyncReport = syncReportMapper.selectByPrimaryKey(Long.parseLong(s));
            dataCondition.append("{\"appletDate\":\"").append(marketingSyncReport.getAppletDate()).append("\"")
                        .append(",\"userType\":\"").append(marketingSyncReport.getUserType()).append("\"")
                        .append("}").append(ch);;

        }
        // 得到最后一个字符的索引地址
        int index = dataCondition.length() - 1;
        // 取到最后一个字符
        char c = dataCondition.charAt(index);
        if (ch == c) {
            // 删除最后一个字符
            dataCondition.deleteCharAt(index);
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
        String key = "marketing:inner:fasttaskrule:".concat(yyyyMMdd6);
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
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        FastTaskRule fastTaskRule = fastTaskRuleMapper.selectByPrimaryKey(Long.parseLong(id));
        FastTaskRuleDetailVO vo = new FastTaskRuleDetailVO();
        BeanUtils.copyProperties(fastTaskRule, vo);
        //跑分数据:
        String[] split = fastTaskRule.getDataIdDesc().split(",");
        List<Map> dataCondition = new ArrayList<>();
        for(String s : split){
            Map map = new HashMap();
            MarketingSyncReport marketingSyncReport = syncReportMapper.selectByPrimaryKey(Long.parseLong(s));
            map.put("appletDate",marketingSyncReport.getAppletDate());
            map.put("apiCode",marketingSyncReport.getApiCode());
            map.put("userType",marketingSyncReport.getUserType());
            map.put("duplicateRemovalNum",marketingSyncReport.getDuplicateRemovalNum());
            dataCondition.add(map);
        }
        vo.setDataCondition(dataCondition);
        Long ruleId = fastTaskRule.getRuleId();
        ScoreRuleConfig scoreRuleConfig = scoreRuleConfigMapper.selectByPrimaryKey(ruleId);
        Map map = new HashMap();
        map.put("id",scoreRuleConfig.getId());
        map.put("ruleName",scoreRuleConfig.getRuleName());
        map.put("ruleNameShort",scoreRuleConfig.getRuleNameShort());
        map.put("strategyId",scoreRuleConfig.getStrategyId());
        vo.setScoreRule(map);
        vo.setCreateTime(formatter.format(fastTaskRule.getCreateTime()));
        vo.setUpdateTime(formatter.format(fastTaskRule.getUpdateTime()));
        return vo;
    }

    @Override
    public boolean updateStatusById(String id, Integer status,MarketingUserDetail userDetail) {
        try {
            FastTaskRule fastTaskRule = new FastTaskRule();
            fastTaskRule.setId(Long.parseLong(id));
            fastTaskRule.setStatus(status);
            fastTaskRule.setUpdateTime(new Date());
            fastTaskRule.setOptId(String.valueOf(userDetail.getId()));
            fastTaskRule.setOptName(userDetail.getUserName());
            fastTaskRuleMapper.updateByPrimaryKeySelective(fastTaskRule);
            return true;
        }catch (Exception e){
            e.printStackTrace();
            log.error(e.getMessage(),e);
            return false;
        }
    }

    @Override
    public ApiResult<Boolean> update(String id,String ruleName, String taskTime, MarketingUserDetail user) {
        try {
            FastTaskRule fastTaskRule = new FastTaskRule();
            fastTaskRule.setId(Long.parseLong(id));
            fastTaskRule.setRuleName(ruleName);
            fastTaskRule.setTaskTime(taskTime);
            fastTaskRule.setStatus(1);
            fastTaskRule.setUpdateTime(new Date());
            fastTaskRule.setOptId(String.valueOf(user.getId()));
            fastTaskRule.setOptName(user.getUserName());
            fastTaskRuleMapper.updateByPrimaryKeySelective(fastTaskRule);
            return new ApiResult<Boolean>().success(true);
        }catch (Exception e){
            e.printStackTrace();
            log.error(e.getMessage(),e);
            return new ApiResult<Boolean>().success(false);
        }

    }

    @Override
    public List<ScoreRuleConfig> getScoreRules(String apiCode) {
        List<String> apiCodeList = new ArrayList<>();
        if(!StringUtils.isEmpty(apiCode)){
            String[] apiCodeArray = apiCode.split(",");
            apiCodeList = Arrays.asList(apiCodeArray);
        }
        List<ScoreRuleConfig> list = scoreRuleConfigMapper.getScoreRules(apiCodeList);
        List<ScoreRuleConfig> soleList = list.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(
                () -> new TreeSet<>(Comparator.comparing(ScoreRuleConfig::getId))), ArrayList::new));
        return soleList;
    }

    @Override
    public Integer getNum(String ids, String apiCode) {
        Integer total = 0;
        String[] split = ids.split(",");
        for(String s : split){
            MarketingSyncReport syncReport = syncReportMapper.selectByPrimaryKey(Long.parseLong(s));
            Integer num = fastTaskRuleMapper.getUnScoreNum(apiCode,syncReport.getAppletDate(),syncReport.getUserType());
            total=total+num;
        }
        return total;
    }

}
