package com.br.marketing.service.Impl;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.OptConditionDTO;
import com.br.marketing.dto.PushDecisionsDTO;
import com.br.marketing.dto.SearchConditionDTO;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.PushDecisionsMapper;
import com.br.marketing.mapper.ScoreSearchConditionMapper;
import com.br.marketing.service.PushDecisionsService;
import com.br.marketing.vo.ConditionOfScoreVO;
import com.br.marketing.vo.PushDecisionsDetailVO;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
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

    @Autowired
    EntityOptServiceImpl entityOptService;

    @Autowired
    RedisChgService redisChgService;

    @Override
    public Result<Long> savePushDecisions(PushDecisionsDTO dto) {

        PushDecisionsExample pushDecisionsExample = new PushDecisionsExample();
        pushDecisionsExample.createCriteria().andRuleNameEqualTo(dto.getRuleName()).andApiCodeEqualTo(dto.getApiCode()).andIsDelEqualTo(1);
        int i = pushDecisionsMapper.countByExample(pushDecisionsExample);
        if (i > 0) {
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("推决策规则模板名称重复");
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
        pushDecisions.setCreateTime(new Date());
        pushDecisions.setUpdateTime(new Date());
        pushDecisionsMapper.insertSelective(pushDecisions);
        return new Result<Long>().setCode(ResultCode.SUCCESS.getValue()).setDate(pushDecisions.getId());
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
        pushDecisionsExample.createCriteria().andApiCodeEqualTo(apiCode).andIsDelEqualTo(1);
        List<PushDecisions> pushDecisions = pushDecisionsMapper.selectByExample(pushDecisionsExample);
        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(pushDecisions);
    }

    @Override
    public Result<Long> updatePushDecisions(PushDecisionsDTO dto) {
        if (StringUtils.isEmpty(dto.getId())) {
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("id为空");
        }
        PushDecisions pushDecisions = new PushDecisions();
        pushDecisions.setId(dto.getId());
        pushDecisions.setRuleName(dto.getRuleName());
        pushDecisions.setAutoTime(dto.getAutoTime());
        pushDecisions.setPushDatasets(dto.getPushDatasets());
        pushDecisions.setReachStrategy(dto.getReachStrategy());
        pushDecisionsMapper.updateByPrimaryKeySelective(pushDecisions);
        return new Result().setCode(ResultCode.SUCCESS.getValue());
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
