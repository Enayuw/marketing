package com.br.marketing.service.Impl;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.client.intelligentcustomerservice.IntelligentCustomerServiceClient;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDTO;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserTaskInfoDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.OptConditionDTO;
import com.br.marketing.dto.PushDecisionsDTO;
import com.br.marketing.dto.RunTaskDTO;
import com.br.marketing.dto.SearchConditionDTO;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.PushDecisionsMapper;
import com.br.marketing.mapper.ScoreSearchConditionMapper;
import com.br.marketing.mapper.StraHisFileMapper;
import com.br.marketing.service.PushDecisionsService;
import com.br.marketing.vo.PushDecisionsDetailVO;
import com.br.marketing.vo.ReachStrategyVO;
import com.br.marketing.vo.TaskTemplateVO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.stream.Collectors;

/**
 * @ClassName PushDecisionsServiceImpl
 * @Description TODO
 * @Author kongbx
 * @Date 2024/8/9 10:22
 */
@Service
@Slf4j
public class PushDecisionsServiceImpl implements PushDecisionsService {

    @Resource
    PushDecisionsMapper pushDecisionsMapper;

    @Resource
    ScoreSearchConditionMapper scoreSearchConditionMapper;

    @Resource
    StraHisFileMapper straHisFileMapper;

    @Autowired
    EntityOptServiceImpl entityOptService;

    @Autowired
    RedisChgService redisChgService;

    @Autowired
    IntelligentCustomerServiceClient intelligentCustomerServiceClient;

    @Override
    public Result<Long> savePushDecisions(PushDecisionsDTO dto) {

        ScoreSearchCondition scoreSearchCondition = scoreSearchConditionMapper.selectByPrimaryKey(dto.getDependencyTemplateId());
        if(scoreSearchCondition != null && scoreSearchCondition.getSourceType() == 0){
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("该规则模板未配置数据源！");
        }

        String fileIds = dto.getFileIds();
        if(fileIds.isEmpty()){
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("未选择跑分文件！");
        }

        // 更新依赖模板中数据源字段
        ScoreSearchCondition scoreSearchCondition1 = new ScoreSearchCondition();
        scoreSearchCondition1.setId(scoreSearchCondition.getId());
        scoreSearchCondition1.setSourceCondition(handleFileId(fileIds));
        scoreSearchConditionMapper.updateByPrimaryKeySelective(scoreSearchCondition1);
        // 查询推送决策是否重复
        PushDecisionsExample pushDecisionsExample = new PushDecisionsExample();
        pushDecisionsExample.createCriteria().andRuleNameEqualTo(dto.getRuleName()).andApiCodeEqualTo(dto.getApiCode()).andIsDelEqualTo(1);
        int i = pushDecisionsMapper.countByExample(pushDecisionsExample);
        if (i > 0) {
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("推决策规则模板名称重复！");
        }
        PushDecisions pushDecisions = new PushDecisions();
        pushDecisions.setApiCode(dto.getApiCode());
        pushDecisions.setRuleNumber(buildConditionNumber(dto.getApiCode()));
        pushDecisions.setRuleName(dto.getRuleName());
        pushDecisions.setDependencyTemplateId(dto.getDependencyTemplateId());
        pushDecisions.setStatus(dto.getStatus());
        pushDecisions.setAutoTime(dto.getAutoTime());
        pushDecisions.setPushDatasets(dto.getPushDatasets());
        pushDecisions.setReachStrategy(dto.getReachStrategy());
        pushDecisions.setAutoRefresh(dto.getAutoRefresh());
        pushDecisions.setPushTarget(dto.getPushTarget());
        pushDecisions.setCreateTime(new Date());
        pushDecisions.setUpdateTime(new Date());
        pushDecisionsMapper.insertSelective(pushDecisions);
        return new Result<Long>().setCode(ResultCode.SUCCESS.getValue()).setDate(pushDecisions.getId());
    }

    // fileIds去重
    private String handleFileId(String fileIds) {
        String[] elements = fileIds.split(",");
        Set<String> uniqueElements = new HashSet<>(Arrays.asList(elements));
        List<String> uniqueList = new ArrayList<>(uniqueElements);
        return String.join(",", uniqueList);
    }

    @Override
    public Result<Boolean> deletePushDecisions(Long id) {
        try {
            PushDecisions pushDecisions = new PushDecisions();
            pushDecisions.setId(id);
            pushDecisions.setIsDel(9);
            pushDecisionsMapper.updateByPrimaryKeySelective(pushDecisions);
            return new Result().setCode(ResultCode.SUCCESS.getValue());
        } catch (Exception e) {
            log.error("删除推决策规则模板报错，id={},",id,e);
        }
        return null;
    }

    @Override
    public Result<PageResultReturn<PushDecisionsDetailVO>> getPushDecisionsList(SearchConditionDTO dto) {
        if (dto.getSize() == null) {
            dto.setSize(10);
        }
        PageHelper.startPage(dto.getCurrent(), dto.getSize());
        List<PushDecisionsDetailVO> decisionsListBySearch = pushDecisionsMapper.getDecisionsListBySearch(dto);
        PageResultReturn pageResultReturn = PageResultReturn.setPageResult(decisionsListBySearch, dto.getCurrent(), dto.getSize());
        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(pageResultReturn);
    }

    @Override
    public Result<PushDecisionsDetailVO> getPushDecisionsDetails(Long id) {
        PushDecisions pushDecisions = pushDecisionsMapper.selectByPrimaryKey(id);
        PushDecisionsDetailVO pushDecisionsDetailVO = new PushDecisionsDetailVO();
        pushDecisionsDetailVO.setId(pushDecisions.getId());
        pushDecisionsDetailVO.setApiCode(pushDecisions.getApiCode());
        pushDecisionsDetailVO.setRuleNumber(pushDecisions.getRuleNumber());
        pushDecisionsDetailVO.setRuleName(pushDecisions.getRuleName());

        Long dependencyTemplateId = pushDecisions.getDependencyTemplateId();
        if(dependencyTemplateId != null){
            ScoreSearchCondition scoreSearchCondition = scoreSearchConditionMapper.selectByPrimaryKey(dependencyTemplateId);
            pushDecisionsDetailVO.setDependencyTemplateId(scoreSearchCondition.getId());
            pushDecisionsDetailVO.setDependencyTemplateName(scoreSearchCondition.getName());
            pushDecisionsDetailVO.setDependencyTemplateSource(scoreSearchCondition.getSourceType());
        }
        pushDecisionsDetailVO.setStatus(pushDecisions.getStatus());
        pushDecisionsDetailVO.setAutoTime(pushDecisions.getAutoTime());
        pushDecisionsDetailVO.setPushDatasets(pushDecisions.getPushDatasets());
        pushDecisionsDetailVO.setReachStrategy(pushDecisions.getReachStrategy());
        pushDecisionsDetailVO.setCreateTime(pushDecisions.getCreateTime().toString());
        pushDecisionsDetailVO.setUpdateTime(pushDecisions.getUpdateTime().toString());
        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(pushDecisionsDetailVO);
    }

    @Override
    public Result updateStatus(OptConditionDTO dto) {
        PushDecisions p = pushDecisionsMapper.selectByPrimaryKey(dto.getId());
        if (!new Integer(1).equals(p.getIsDel())) {
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("该推决策规则配置不存在");
        }
        PushDecisions pushDecisions = new PushDecisions();
        pushDecisions.setId(dto.getId());
        pushDecisions.setStatus(dto.getStatus());
        pushDecisionsMapper.updateByPrimaryKeySelective(pushDecisions);
        entityOptService.writeOptLog(dto.getId(), pushDecisions, p);
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    @Override
    public Result<List<PushDecisionsDetailVO>> getDecisionsByRule(String apiCode) {
        PushDecisionsExample pushDecisionsExample = new PushDecisionsExample();
        pushDecisionsExample.createCriteria().andApiCodeEqualTo(apiCode).andIsDelEqualTo(Constants.DATA_VALID);
        List<PushDecisions> pushDecisions = pushDecisionsMapper.selectByExample(pushDecisionsExample);
        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(pushDecisions);
    }

    @Override
    public Result<Long> updatePushDecisions(PushDecisionsDTO dto) {
        if (StringUtils.isEmpty(dto.getId())) {
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("id为空");
        }
        String fileIds = dto.getFileIds();
        if(fileIds.isEmpty()){
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("未选择跑分文件！");
        }
        // 更新依赖模板中数据源字段
        ScoreSearchCondition scoreSearchCondition = new ScoreSearchCondition();
        scoreSearchCondition.setId(dto.getDependencyTemplateId());
        scoreSearchCondition.setSourceCondition(handleFileId(fileIds));
        scoreSearchConditionMapper.updateByPrimaryKeySelective(scoreSearchCondition);
        // 更新推送决策配置
        PushDecisions pushDecisions = new PushDecisions();
        pushDecisions.setId(dto.getId());
        pushDecisions.setRuleName(dto.getRuleName());
        pushDecisions.setAutoTime(dto.getAutoTime());
        pushDecisions.setPushDatasets(dto.getPushDatasets());
        pushDecisions.setReachStrategy(dto.getReachStrategy());
        pushDecisions.setAutoRefresh(dto.getAutoRefresh());
        pushDecisions.setUpdateTime(new Date());
        pushDecisionsMapper.updateByPrimaryKeySelective(pushDecisions);
        return new Result().setCode(ResultCode.SUCCESS.getValue());
    }

    @Override
    public Result<List<TaskTemplateVO>> getRunTaskByTemplate(RunTaskDTO dto) {
        if(dto == null){
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("查询参数为空");
        }
        // 1- 查询规则模板
        ScoreSearchCondition scoreSearchCondition = scoreSearchConditionMapper.selectByPrimaryKey(Long.valueOf(dto.getTemplateId()));
        if(scoreSearchCondition == null){
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("未查询到规则模板,模板id：" + dto.getTemplateId());
        }

        // 2- 获取所有跑分id
        List<String> ids = new ArrayList<>();
        String sourceCondition = scoreSearchCondition.getSourceCondition();
        if(sourceCondition.contains(",")){
            ids = Arrays.stream(sourceCondition.split(","))
                    .map(String::trim)
                    .collect(Collectors.toList());
        }else {
            ids.add(sourceCondition);
        }
        if(CollectionUtil.isEmpty(ids)){
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("跑分文件为空,对应模板id：" + scoreSearchCondition.getId());
        }
        // 3- 根据跑分文件id查询跑分配置
        List<String> ruleNameShorts = straHisFileMapper.getFileById(ids);
        if(CollectionUtil.isEmpty(ruleNameShorts)){
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("跑分配置为空,文件ids：" + ids);
        }

        // 4- 根据跑分配置 查询所有跑分文件
        List<TaskTemplateVO> files = straHisFileMapper.getFileByruleNameShorts(ruleNameShorts,dto.getApiCode());

        // fileId多个场景，合并为一条
        files = new ArrayList<>(files.stream()
                .collect(Collectors.toMap(
                        TaskTemplateVO::getFileId,
                        task -> task,
                        (existing, replacement) -> {
                            existing.setUserType(existing.getUserType() + "," + replacement.getUserType());
                            return existing;
                        }
                ))
                .values());

        // 筛选出规则模板中已关联的跑分id，status置为1
        for (TaskTemplateVO taskTemplateVO : files) {
            if(ids.contains(taskTemplateVO.getFileId())){
                taskTemplateVO.setStatus(1);
            }
        }

        // 倒叙排序
        List<TaskTemplateVO> sortedFiles = files.stream()
                .sorted(Comparator.comparing(TaskTemplateVO::getTaskCreateTime).reversed())
                .collect(Collectors.toList());

        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(sortedFiles);

    }

    @Override
    public Result<List<ReachStrategyVO>> getReachStrategyByApiCode(String apiCode) {
        //基础信息
        PushMarketingUserTaskInfoDTO pushMarketingUserTaskInfoDTO = new PushMarketingUserTaskInfoDTO();
        pushMarketingUserTaskInfoDTO.setMethod("getAISTR");
        //传输参数信息
        PushMarketingUserDTO pushMarketingUserDTO = new PushMarketingUserDTO();
        pushMarketingUserDTO.setApiCode(apiCode);
        pushMarketingUserDTO.setJsonData(pushMarketingUserTaskInfoDTO);
        Result result = intelligentCustomerServiceClient.getReachStrategy(pushMarketingUserDTO);
        if (!ResultCode.SUCCESS.getValue().equals(result.getCode())) {
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("查询触发策略有误，请重试！："+apiCode);
        }
        List<ReachStrategyVO> list = new ArrayList<>();
        JSONObject data = (JSONObject) result.getData();
        if(data.isEmpty()){
            new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(list);
        }
        String jsonString = data.getString("data");
        JSONArray jsonArray = JSONArray.parseArray(jsonString);
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            ReachStrategyVO reachStrategyVO = new ReachStrategyVO();
            reachStrategyVO.setAistrName(jsonObject.getString("aistrName"));
            reachStrategyVO.setAistrNum(jsonObject.getString("aistrNum"));
            list.add(reachStrategyVO);
        }
        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(list);
    }

    String buildConditionNumber(String apiCode) {
        String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String key = RedisKeyConstant.decisionsNumber.concat(":").concat(yyyyMMdd);
        Long incr = redisChgService.incr(key);
        redisChgService.expire(key, getKeyExpiration());
        String s = incr.toString();
        int length = s.length();
        for (int i = 3; i > length; i--) {
            s = "0" + s;
        }
        return "JC" + yyyyMMdd.concat("_").concat(apiCode).concat("_").concat(s);
    }

    /**
     * 获取当前时间到第二天凌晨的秒
     *
     * @dateTime 2021/10/19 9:21
     */
    private int getKeyExpiration() {
        final LocalDateTime now = LocalDateTime.now();
        // 当前毫秒数
        long l = now.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        LocalDateTime localDateTime = now.plusDays(1);
        // 第二天凌晨毫秒数
        long l1 = localDateTime.toLocalDate().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        return (int) (l1 - l) / 1000;
    }

}
