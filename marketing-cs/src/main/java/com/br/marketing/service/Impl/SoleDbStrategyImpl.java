package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.MarketingDataValidConfig;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.service.IMarketingSyncUserService;
import com.br.marketing.service.SoleStrategyService;
import com.br.marketing.vo.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class SoleDbStrategyImpl implements SoleStrategyService {
    private static final Logger log = LoggerFactory.getLogger(PushRuleServiceImpl.class);

    @Autowired
    IMarketingSyncUserService iMarketingSyncUserService;

    /**
     *  去重方法
     *  1、matchSoleRule方法匹配出涉及到的去重规则
     *  2、遍历去重规则 进行多条去重规则sql的拼接（sql逻辑 获取该规则下时间最早的那一条id）
     *  3、遍历去重sql执行
     *      sql获取到的id与当前这条数据id进行比较 如果遍历所有sql id都是一样的，认为未重复，有一个id不匹配，就认为是重复的。
     *      更新该条数据状态 （1-未去重（默认）；2-未重复；3-重复）
     */
    @Override
    public Result<Integer> actionSole(List<CustomerSoleRuleVO> soleRuleVOS, MarketingSyncUser syncUser) {
        long st1 = System.currentTimeMillis();
        List<CustomerSoleRuleVO> customerSoleRuleVO = this.matchSoleRule(soleRuleVOS, syncUser);
        if(customerSoleRuleVO.size()<=0){
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(1).setMessage("数据无需去重");
        }
        //region 多规则拼接
        List<String> todaySqls = new ArrayList<>();
        for (CustomerSoleRuleVO soleRuleVO : customerSoleRuleVO) {
            /** 查询T日内的未统计的去重的数据的where条件 */
            StringBuilder dbWhereTodayStr = new StringBuilder();

            LocalDate now = LocalDate.parse(syncUser.getAppletDate(),DateTimeFormatter.ofPattern("yyyy-MM-dd"));
//            String endTimeNow = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String endTimeNext = now.plusDays(1L).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            /** 时间范围  */
            Integer soleCycleTimes = soleRuleVO.getSoleCycleTimes();
            String timeStrNowSql = soleCycleTimes != null
                    ?   String.format(" applet_date >='%s' and applet_date <'%s' "
                    ,now.minusDays(Long.valueOf(soleCycleTimes)).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                    ,endTimeNext)
                    :   String.format(" applet_date <'%s' ",endTimeNext);

            /** 去重字段的where条件 */
            StringBuilder soleStr = new StringBuilder();
            String soleFields = soleRuleVO.getSoleFields();
            //去重字段
            String[] fields = soleFields.split(",");

            boolean cidMark = false;
            boolean apiCodeMark = false;
            String cellStr = "",cusNumStr = "",taskidStr = "";
            for (String field : fields) {
                switch (field.toLowerCase()){
                    case "cid":
                        cidMark = true;
                        break;
                    case "apicode":
                        apiCodeMark = true;
                        break;
                    case "cell":
                        cellStr = String.format(" and cell = '%s'",syncUser.getCell());
                        break;
                    case "cusnum":
                        cusNumStr = String.format(" and cust_num = '%s'",syncUser.getCustNum());
                        break;
                    case "taskid":
                        taskidStr = String.format(" and cus_batch = '%s'",syncUser.getCusBatch());
                        break;
                    default:
                        break;
                }
            }
            soleStr.append(String.format("%s %s %s",cellStr,cusNumStr,taskidStr));
            /** 拼接去重字段条件 */
            if(StringUtils.isNotBlank(soleStr.toString())){
                dbWhereTodayStr.append(soleStr);
            }
            Integer allUserType = soleRuleVO.getAllUserType();
            // 默认不支持全场景
            boolean notAllUserTypeFlag = null == allUserType || 0 == allUserType;
            if(notAllUserTypeFlag && StringUtils.isNotBlank(soleRuleVO.getConditionDbDesc())){
                /** 拼接场景条件 */
                dbWhereTodayStr.append(" and ").append(String.format("(%s)",soleRuleVO.getConditionDbDesc()));
            }

            /** 去重字段筛选 */
            dbWhereTodayStr.append(" and ").append(" is_repeat in (1,2) ");

            /** 拼接T的时间范围 */
            if(StringUtils.isNotBlank(timeStrNowSql)){
                dbWhereTodayStr.append(" and ").append(timeStrNowSql);
            }

            /** 查询T日满足去重规则的数据条数 */
            String sqlToday = String.format("select id,applet_time from b_marketing_sync_%s where %s order by applet_time asc,id asc limit 1"
                    , syncUser.getApiCode(), dbWhereTodayStr.toString().replaceFirst("and", ""));
            todaySqls.add(sqlToday);
        }
        //endregion
        long et1 = System.currentTimeMillis()-st1;
        long st2 = System.currentTimeMillis();
        boolean isRepat = true; //未重复
        for (String todaySql : todaySqls) {
            TodayIdTimeBySoleVo soleValidUser = iMarketingSyncUserService.getSoleValidUser(todaySql);
            if(!syncUser.getId().equals(soleValidUser.getId())){
                isRepat = false;
                break;
            }
        }
        long et2 = System.currentTimeMillis()-st2;
        long st3 = System.currentTimeMillis();
        if(isRepat){
            String updateValidSql = String.format("update b_marketing_sync_%s set is_repeat=2 where id = %d"
                    ,syncUser.getApiCode(),syncUser.getId());
            iMarketingSyncUserService.updateRepeatUserStatus(updateValidSql);
            syncUser.setIsRepeat(2);
            long et3 = System.currentTimeMillis()-st3;
            if(log.isInfoEnabled()){
                log.info(String.format("去重数据：%d,去重具体耗时：匹配规则加去重sql拼接：%d,去重sql执行：%d,修改去重数据：%d",syncUser.getId(),et1,et2,et3));
            }
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(1);
        }else{
            String updateInValidSql = String.format("update b_marketing_sync_%s set is_repeat=3 where id = %d"
                    ,syncUser.getApiCode(),syncUser.getId());
            iMarketingSyncUserService.updateRepeatUserStatus(updateInValidSql);
            syncUser.setIsRepeat(3);
            long et3 = System.currentTimeMillis()-st3;
            if(log.isInfoEnabled()){
                log.info(String.format("去重数据：%d,去重具体耗时：匹配规则加去重sql拼接：%d,去重sql执行：%d,修改去重数据：%d",syncUser.getId(),et1,et2,et3));
            }
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(2);
        }
    }

    private List<CustomerSoleRuleVO> matchSoleRule(List<CustomerSoleRuleVO> soleRuleVOS, MarketingSyncUser syncUser){
        List<CustomerSoleRuleVO> res = new ArrayList<>();
        for (CustomerSoleRuleVO soleRuleVO : soleRuleVOS) {
            Integer allUserType = soleRuleVO.getAllUserType();
            if(null != allUserType && 1 == allUserType){
                // 全场景的时候是不需要在进行userType条件拼接，也就是说 SoleRuleVO.ConditionDbDesc的值是null
                res.add(soleRuleVO);
            }else{
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
        }
        return res;
    }

    @Override
    public List<CustomerScoreRuleVO> matchScoreRule(List<CustomerScoreRuleVO> scoreRuleVos,String userType){
        MarketingSyncUser marketingSyncUser = new MarketingSyncUser();
        marketingSyncUser.setUserType(userType);
        List<CustomerScoreRuleVO> res = new ArrayList<>();
        for (CustomerScoreRuleVO scoreRuleVO : scoreRuleVos) {
            RuleConditionVo conditionVo = JSON.parseObject(scoreRuleVO.getConditionInfo(), new TypeReference<RuleConditionVo>() {
            }.getType());

            StringBuilder dbStr = new StringBuilder();
            if("or".equals(conditionVo.getLogicalOperation())){
                boolean orResult = false;
                for (RuleConditionFactorVo ruleConditionFactorVo : conditionVo.getOperationFactor()) {
                    Result<Boolean> booleanResult = this.matchSoleRuleOperation(ruleConditionFactorVo, marketingSyncUser);
                    if(booleanResult.getData()){
                        orResult = true;
                    }
                }
                if(orResult) {
                    res.add(scoreRuleVO);
                }
            }

            if("and".equals(conditionVo.getLogicalOperation())){
                boolean andResult = true;
                for (RuleConditionFactorVo ruleConditionFactorVo : conditionVo.getOperationFactor()) {
                    Result<Boolean> booleanResult = this.matchSoleRuleOperation(ruleConditionFactorVo, marketingSyncUser);
                    dbStr.append(" and ").append(booleanResult.getMessage());
                    if(!booleanResult.getData()){
                        andResult = false;
                    }
                }
                if(andResult){
                    res.add(scoreRuleVO);
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
                fieldValue = syncUser.getUserType();
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
                }else if(StringUtils.isNotNull(vo.getFieldValue())&&vo.getFieldValue().equals(fieldValue)){
                    dbStr.append(String.format("='%s'",fieldValue));
                    result=true;
                }else{
                    dbStr.append(String.format("='%s'",vo.getFieldValue()));
                    result=false;
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
        String dbStr= conditionVo.getOperationFactor()
                .stream().map(t->analysisFactor(t))
                .collect(Collectors.joining(" "+conditionVo.getLogicalOperation()+" "));
        if(StringUtils.isNotBlank(dbStr)){
            return new Result<String>().setCode(ResultCode.SUCCESS.getValue())
                    .setDate(dbStr);
        }
        return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("规则有误");
    }

    @Override
    public String analysisSimpleConditionPlus(String conditionStr, String date, String time) {
        return String.format("applet_date='%s' and applet_time<='%s' and (%s)", date, time, conditionStr);
    }

    @Override
    public Result<List<String>> analysisConditions(String conditionStr) {
        if (StringUtils.isBlank(conditionStr)) {
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("规则不能传空");
        }
        List<RuleConditionVo> conditionVos = new ArrayList<>();
        try {
            conditionVos = JSON.parseObject(conditionStr, new TypeReference<List<RuleConditionVo>>() {
            }.getType());
        }catch (Exception ex){
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("规则有误");
        }
        List<String> dblist = new ArrayList<>();
        conditionVos.forEach(t->{
            String dbStr= t.getOperationFactor()
                    .stream().map(k->analysisFactor(k))
                    .collect(Collectors.joining(" "+t.getLogicalOperation()+" "));
            if(StringUtils.isNotBlank(dbStr)){
                dblist.add(dbStr);
            }
        });
        return new Result<List<String>>().setCode(ResultCode.SUCCESS.getValue()).setDate(dblist);
    }

    @Override
    public Result<String> analysisTransferConditions(String conditionStr, String date, String time) {
        List<String> res = new ArrayList<>();
        if (StringUtils.isBlank(conditionStr)) {
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("规则不能传空");
        }
        RuleConditionVo conditionVo = new RuleConditionVo();
        try {
            conditionVo = JSON.parseObject(conditionStr, new TypeReference<RuleConditionVo>() {
            }.getType());
        }catch (Exception ex){
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("规则有误");
        }
        if(!conditionVo.getLogicalOperation().equals("or")){
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("规则不支持转化");
        }
        JSONArray resObj = new JSONArray();
        conditionVo.getOperationFactor().forEach(t->{
            JSONObject simpleCondition = new JSONObject();
            JSONArray simpleConditionDetail = new JSONArray();
            JSONObject jsonDate = new JSONObject();
            JSONObject jsonTime = new JSONObject();
            JSONObject jsonOr = new JSONObject();
            simpleConditionDetail.add(jsonDate);
            simpleConditionDetail.add(jsonTime);
            simpleConditionDetail.add(jsonOr);
            simpleCondition.put("logicalOperation","and");
            simpleCondition.put("operationFactor",simpleConditionDetail);

            jsonDate.put("fieldName","appletDate");
            jsonDate.put("fieldValue",date);
            jsonDate.put("operation","=");

            jsonTime.put("fieldName","appletTime");
            jsonTime.put("fieldValue",time);
            jsonTime.put("operation","<=");

            jsonOr.put("fieldName",t.getFieldName());
            jsonOr.put("fieldValue",t.getFieldValue());
            jsonOr.put("operation",t.getOperation());

            resObj.add(simpleCondition);});
        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(JSON.toJSONString(resObj));
    }

    @Override
    public Result<String> analysisTransferConditionsByValidConfig(String conditionStr, List<MarketingDataValidConfig> configList, String time) {
        if (StringUtils.isBlank(conditionStr)) {
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("规则不能传空");
        }
        RuleConditionVo conditionVo = new RuleConditionVo();
        try {
            conditionVo = JSON.parseObject(conditionStr, new TypeReference<RuleConditionVo>() {
            }.getType());
        } catch (Exception ex) {
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("规则有误");
        }
        if (!conditionVo.getLogicalOperation().equals("or")) {
            return new Result<String>().setCode(ResultCode.FAIL.getValue()).setMessage("规则不支持转化");
        }
        JSONArray resObj = new JSONArray();
        RuleConditionVo finalConditionVo = conditionVo;
        configList.forEach((MarketingDataValidConfig config) -> finalConditionVo.getOperationFactor().forEach((RuleConditionFactorVo t) -> {
            if (!Objects.equals(t.getFieldValue(), config.getUserType())) {
                return;
            }

            JSONObject simpleCondition = new JSONObject();
            JSONArray simpleConditionDetail = new JSONArray();
            JSONObject jsonDate = new JSONObject();
            JSONObject jsonTime = new JSONObject();
            JSONObject jsonOr = new JSONObject();
            simpleConditionDetail.add(jsonDate);

            simpleConditionDetail.add(jsonOr);
            simpleCondition.put("logicalOperation", "and");
            simpleCondition.put("operationFactor", simpleConditionDetail);

            jsonDate.put("fieldName", "appletDate");
            jsonDate.put("fieldValue", config.getAppletDate());
            jsonDate.put("operation", "=");

            if (config.getAppletDate().equals(LocalDate.now().toString())) {
                jsonTime.put("fieldName", "appletTime");
                jsonTime.put("fieldValue", time);
                jsonTime.put("operation", "<=");
                simpleConditionDetail.add(jsonTime);
            }

            jsonOr.put("fieldName", t.getFieldName());
            jsonOr.put("fieldValue", t.getFieldValue());
            jsonOr.put("operation", t.getOperation());

            resObj.add(simpleCondition);
        }));

        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(JSON.toJSONString(resObj));
    }

    private String analysisFactor(RuleConditionFactorVo vo){

        if(vo == null){
            return null;
        }
        StringBuilder dbStr = new StringBuilder();
        dbStr.append(StringUtils.humpToLine2(vo.getFieldName()));
        dbStr.append(vo.getOperation()).append(StringUtils.isNull(vo.getFieldValue())?"null":"'".concat(vo.getFieldValue()).concat("'"));
        return dbStr.toString();
    }
}
