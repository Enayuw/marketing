package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.MarketingPreUserDetailDTO;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.SoleRuleConfig;
import com.br.marketing.service.IMarketingSyncUserService;
import com.br.marketing.service.SoleStrategyService;
import com.br.marketing.vo.CustomerSoleRuleVO;
import com.br.marketing.vo.RuleConditionFactorVo;
import com.br.marketing.vo.RuleConditionVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@Service
public class SoleDbStrategyImpl implements SoleStrategyService {

    @Autowired
    IMarketingSyncUserService iMarketingSyncUserService;

    @Override
    public Result<Integer> actionSole(List<CustomerSoleRuleVO> soleRuleVOS, MarketingSyncUser syncUser) {
        List<CustomerSoleRuleVO> customerSoleRuleVO = this.matchSoleRule(soleRuleVOS, syncUser);
        if(customerSoleRuleVO.size()<=0){
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(1).setMessage("数据无需去重");
        }
        StringBuilder soleSql = new StringBuilder();
        StringBuilder soleSqlWhereToday = new StringBuilder();
        boolean rulesMark = false;
        for (CustomerSoleRuleVO soleRuleVO : customerSoleRuleVO) {
            StringBuilder dbWhereStr = new StringBuilder(" where is_repeat=2 ");
            StringBuilder dbWhereTodayStr = new StringBuilder();
            String timeStrNowSql = "";
            String timeStrNextSql = "";
            StringBuilder soleStr = new StringBuilder();
            String soleFields = soleRuleVO.getSoleFields();
            Integer soleCycleTimes = soleRuleVO.getSoleCycleTimes();
            String endTimeNow =null;
            String endTimeNext =null;
            String startTime =null;
            if(soleCycleTimes != null){
                LocalDateTime now = LocalDateTime.now();
                endTimeNow = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                endTimeNext = now.plusDays(1L).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                startTime = now.minusDays(Long.valueOf(soleCycleTimes)).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                timeStrNowSql = String.format(" applet_date >='%s' and applet_date <'%s' ",startTime,endTimeNext);
                timeStrNextSql = String.format(" applet_date >='%s' and applet_date <'%s' ",endTimeNow,endTimeNext);
            }
            String[] fields = soleFields.split(",");

            boolean cidMark = false;
            boolean apiCodeMark = false;
            for (String field : fields) {
                switch (field.toLowerCase()){
                    case "cid":
                        cidMark = true;
                        break;
                    case "apicode":
                        apiCodeMark = true;
                        break;
                    case "taskid":
                        soleStr.append(" and ").append(String.format(" cus_batch = '%s'",syncUser.getCusBatch()));
                        break;
                    case "cell":
                        soleStr.append(" and ").append(String.format(" cell = '%s'",syncUser.getCell()));
                        break;
                    case "cusnum":
                        soleStr.append(" and ").append(String.format(" cust_num = '%s'",syncUser.getCustNum()));
                        break;
                    default:
                        break;
                }
            }
            if(StringUtils.isNotBlank(timeStrNowSql)){
                dbWhereStr.append(" and ").append(timeStrNowSql);
                dbWhereTodayStr.append(" and ").append(timeStrNextSql);
            }
            if(StringUtils.isNotBlank(soleStr.toString())){
                dbWhereStr.append(soleStr);
                dbWhereTodayStr.append(soleStr);
            }
            if(StringUtils.isNotBlank(dbWhereStr.toString())){
                dbWhereStr.append(" and ").append(String.format("(%s)",soleRuleVO.getConditionDbDesc()));
                dbWhereTodayStr.append(" and ").append(String.format("(%s)",soleRuleVO.getConditionDbDesc()));
            }
            if(cidMark&&!apiCodeMark){
                //todo 需要查询多张apicode表
            }

            if(StringUtils.isNotBlank(soleSql.toString())){
                rulesMark =true;
                soleSql.append(" union ");
            }
            String todayWhere = dbWhereTodayStr.toString().replaceFirst("and", "");
            if(StringUtils.isNotBlank(soleSqlWhereToday.toString())){
                soleSqlWhereToday.append(" or ").append(String.format("(%s)",todayWhere));
            }else{
                soleSqlWhereToday.append(String.format("(%s)",todayWhere));
            }

            soleSql.append(" select count(*) as num")
                    .append(" from b_marketing_sync_"+soleRuleVO.getApiCode())
                    .append(dbWhereStr);
        }
        String sqlCount = null;
        String sqlToday = null;
        String sqlTodayWhere = null;
        if(rulesMark){
            sqlCount = String.format("select sum(num) from (%s)",soleSql);
        }else{
            sqlCount = soleSql.toString();
        }
        if(StringUtils.isNotBlank(soleSqlWhereToday.toString())){
            sqlTodayWhere = String.format("where  is_repeat=1 and %s",soleSqlWhereToday);
            sqlToday = String.format("select id from b_marketing_sync_%s %s" +
                            " order by applet_time desc limit 1"
                    ,syncUser.getApiCode(),sqlTodayWhere);
        }

        if(StringUtils.isNull(sqlCount)){
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage("该数据没有生成去重规则");
        }

        Long  size= iMarketingSyncUserService.countRepeat(sqlCount);
        if(size >=1){
            return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(2);
        }

        Long soleValidUser = iMarketingSyncUserService.getSoleValidUser(sqlToday);
        if(syncUser.getId().equals(soleValidUser)){
            String updateValidSql = String.format("update b_marketing_sync_%s set is_repeat=2 where id = %d"
                    ,syncUser.getApiCode(),syncUser.getId());
            iMarketingSyncUserService.updateRepeatUserStatus(updateValidSql);
            String updateInvalidSql = String.format("update b_marketing_sync_%s set is_repeat=3 %s and id!=%d"
            ,syncUser.getApiCode(),sqlTodayWhere,syncUser.getId());
            iMarketingSyncUserService.updateRepeatUserStatus(updateInvalidSql);
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(1);
        }else{
            String updateInValidSql = String.format("update b_marketing_sync_%s set is_repeat=3 where id = %d"
                    ,syncUser.getApiCode(),syncUser.getId());
            iMarketingSyncUserService.updateRepeatUserStatus(updateInValidSql);
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(2);
        }
    }

    private List<CustomerSoleRuleVO> matchSoleRule(List<CustomerSoleRuleVO> soleRuleVOS, MarketingSyncUser syncUser){
        List<CustomerSoleRuleVO> res = new ArrayList<>();
        for (CustomerSoleRuleVO soleRuleVO : soleRuleVOS) {
            RuleConditionVo conditionVo = JSON.parseObject(soleRuleVO.getConditionInfo(), new TypeReference<RuleConditionVo>() {
            }.getType());

            StringBuilder dbStr = new StringBuilder();
            if("or".equals(conditionVo.getLogicalOperation())){
                boolean orResult = false;
                for (RuleConditionFactorVo ruleConditionFactorVo : conditionVo.getOperationFactor()) {
                    Result<Boolean> booleanResult = this.matchSoleRuleOperation(ruleConditionFactorVo, syncUser);
                    if(booleanResult.getData()){
                        orResult = true;
                    }
                    dbStr.append(" or ").append(booleanResult.getMessage());
                }
                if(orResult) {
                    soleRuleVO.setConditionDbDesc(dbStr.toString().replaceFirst("or",""));
                    res.add(soleRuleVO);
                }
            }

            if("and".equals(conditionVo.getLogicalOperation())){
                boolean andResult = true;
                for (RuleConditionFactorVo ruleConditionFactorVo : conditionVo.getOperationFactor()) {
                    Result<Boolean> booleanResult = this.matchSoleRuleOperation(ruleConditionFactorVo, syncUser);
                    dbStr.append(" and ").append(booleanResult.getMessage());
                    if(!booleanResult.getData()){
                        andResult = false;
                    }
                }
                if(andResult){
                    soleRuleVO.setConditionDbDesc(dbStr.toString().replaceFirst("and",""));
                    res.add(soleRuleVO);
                }
            }

        }
        return res;
    }


    private Result<Boolean> matchSoleRuleOperation(RuleConditionFactorVo vo, MarketingSyncUser syncUser){

        String fieldValue = null;
        StringBuilder dbStr = new StringBuilder();
        switch (vo.getFieldName().toLowerCase()){
            case "usertype":
                fieldValue = syncUser.getGroupType();
                dbStr.append("user_type");
                break;
            default:
                break;
        }
        boolean result = false;
        switch (vo.getOperation()){
            case "=":
                if(StringUtils.isNull(vo.getFieldValue())&&StringUtils.isNull(fieldValue)){
                    dbStr.append("=null");
                    result =true;
                }
                if(StringUtils.isNotNull(vo.getFieldValue())&&vo.getFieldValue().equals(fieldValue)){
                    dbStr.append(String.format("='%s'",fieldValue));
                    result=true;
                }
                break;
            default:
                break;
        }
        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(result).setMessage(dbStr.toString());
    }


    @Override
    public Result<String> analysisCondition(String conditionStr) {
        if (StringUtils.isBlank(conditionStr)) {
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("规则不能传空");
        }
        RuleConditionVo conditionVo = null;
        try{
            conditionVo = JSON.parseObject(conditionStr, new TypeReference<RuleConditionVo>() {
        }.getType());
        }catch(Exception ex){
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("规则解析有误");
        }
        StringBuilder dbStr = new StringBuilder();
        for (RuleConditionFactorVo ruleConditionFactorVo : conditionVo.getOperationFactor()) {
            String factor = this.analysisFactor(ruleConditionFactorVo);
            if(StringUtils.isNotBlank(factor)){
                dbStr.append(String.format(" %s ",conditionVo.getLogicalOperation())).append(factor);
            }
        }
        if(StringUtils.isNotBlank(dbStr.toString())){
            return new Result<String>().setCode(ResultCode.SUCCESS.getValue())
                    .setDate(dbStr.toString().replaceFirst(conditionVo.getLogicalOperation(),""));
        }
        return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("规则有误");
    }

    private String analysisFactor(RuleConditionFactorVo vo){

        if(vo == null){
            return null;
        }

        StringBuilder dbStr = new StringBuilder();
        switch (vo.getFieldName().toLowerCase()){
            case "usertype":
                dbStr.append("user_type");
                break;
            default:
                break;
        }
        switch (vo.getOperation()){
            case "=":
                if(StringUtils.isNull(vo.getFieldValue())){
                    dbStr.append("=null");
                }else{
                    dbStr.append(String.format("='%s'",vo.getFieldValue()));
                }
                break;
            default:
                break;
        }
        return dbStr.toString();
    }
}
