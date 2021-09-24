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
import com.br.marketing.vo.TodayIdTimeBySoleVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class SoleDbStrategyImpl implements SoleStrategyService {

    @Autowired
    IMarketingSyncUserService iMarketingSyncUserService;

    /**
     *  去重方法
     *  新建两条where条件，一条是T-1前，一条是T日
     *  遍历所有的去重规则，进行where条件拼接（参与去重的数据源为 未去重数据（1） 和 不重复的数据（2））
     *  T-1 前的数据有 -> 则认为重复（3）
     *            无 -> 获取T日 满足条件的最小时间的数据id  相等  -> 不重复（2）
     *                                               不相等 -> 重复（3）
     */
//    @Override
//    public Result<Integer> actionSole(List<CustomerSoleRuleVO> soleRuleVOS, MarketingSyncUser syncUser) {
//        List<CustomerSoleRuleVO> customerSoleRuleVO = this.matchSoleRule(soleRuleVOS, syncUser);
//        if(customerSoleRuleVO.size()<=0){
//            return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(1).setMessage("数据无需去重");
//        }
//        /** 多规则T-1日内的wehere条件 */
//        StringBuilder soleSql = new StringBuilder();
//        /** 多规则T日内的wehere条件 */
//        StringBuilder soleSqlWhereToday = new StringBuilder();
//        //一条数据 满足多条去重规则标志 true是多条
//        boolean rulesMark = false;
//        //region 多规则拼接
//        List<String> countSqls = new ArrayList<>();
//        List<String> todaySqls = new ArrayList<>();
//        for (CustomerSoleRuleVO soleRuleVO : customerSoleRuleVO) {
//            /** 查询T-1时间内已经去重统计过的数据的where条件 */
//            StringBuilder dbWhereStr = new StringBuilder();
//            /** 查询T日内的未统计的去重的数据的where条件 */
//            StringBuilder dbWhereTodayStr = new StringBuilder();
//
//            LocalDateTime now = LocalDateTime.now();
//            String endTimeNow = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
//            String endTimeNext = now.plusDays(1L).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
//
//            /** T时间范围 */
//            String timeStrNextSql = String.format(" applet_date >='%s' and applet_date <'%s' ",endTimeNow,endTimeNext);
//
//            /** T-1时间范围 */
//            Integer soleCycleTimes = soleRuleVO.getSoleCycleTimes();
//            String timeStrNowSql = soleCycleTimes != null
//                    ?   String.format(" applet_date >='%s' and applet_date <'%s' "
//                        ,now.minusDays(Long.valueOf(soleCycleTimes)).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
//                        ,endTimeNow)
//                    :   String.format(" applet_date <'%s' ",endTimeNow);
//
//            /** 去重字段的where条件 */
//            StringBuilder soleStr = new StringBuilder();
//            String soleFields = soleRuleVO.getSoleFields();
//            //去重字段
//            String[] fields = soleFields.split(",");
//
//            boolean cidMark = false;
//            boolean apiCodeMark = false;
//            String cellStr = "",cusNumStr = "",taskidStr = "";
//            for (String field : fields) {
//                switch (field.toLowerCase()){
//                    case "cid":
//                        cidMark = true;
//                        break;
//                    case "apicode":
//                        apiCodeMark = true;
//                        break;
//                    case "cell":
//                        cellStr = String.format(" and cell = '%s'",syncUser.getCell());
//                        break;
//                    case "cusnum":
//                        cusNumStr = String.format(" and cust_num = '%s'",syncUser.getCustNum());
//                        break;
//                    case "taskid":
//                        taskidStr = String.format(" and cus_batch = '%s'",syncUser.getCusBatch());
//                        break;
//                    default:
//                        break;
//                }
//            }
//            soleStr.append(String.format("%s %s %s",cellStr,cusNumStr,taskidStr));
//            /** 拼接去重字段条件 */
//            if(StringUtils.isNotBlank(soleStr.toString())){
//                dbWhereStr.append(soleStr);
//                dbWhereTodayStr.append(soleStr);
//            }
//
//            /** 拼接场景条件 */
//            if(StringUtils.isNotBlank(soleRuleVO.getConditionDbDesc())){
//                dbWhereStr.append(" and ").append(String.format("(%s)",soleRuleVO.getConditionDbDesc()));
//                dbWhereTodayStr.append(" and ").append(String.format("(%s)",soleRuleVO.getConditionDbDesc()));
//            }
//
//            /** 去重字段筛选 */
//            dbWhereStr.append(" and ").append(" is_repeat in (1,2) ");
//            dbWhereTodayStr.append(" and ").append(" is_repeat in (1,2) ");
//
//            /** 拼接T-1的时间范围 */
//            if(StringUtils.isNotBlank(timeStrNowSql)){
//                dbWhereStr.append(" and ").append(timeStrNowSql);
//            }
//
//            /** 拼接T的时间范围 */
//            if(StringUtils.isNotBlank(timeStrNextSql)){
//                dbWhereTodayStr.append(" and ").append(timeStrNextSql);
//            }
//
//            /** 查询T-1日前满足去重规则的数据条数 */
//            String sqlCount = String.format("select count(*) from b_marketing_sync_%s where %s"
//                    ,syncUser.getApiCode(),dbWhereStr.toString().replaceFirst("and", ""));
//            countSqls.add(sqlCount);
//            /** 查询T日满足去重规则的数据条数 */
//            String sqlToday = String.format("select id,applet_time from b_marketing_sync_%s where %s order by applet_time asc,id asc limit 1"
//                    , syncUser.getApiCode(), dbWhereTodayStr.toString().replaceFirst("and", ""));
//            todaySqls.add(sqlToday);
//        }
//        //endregion
//
//        boolean countMark = false;
//        for (String countSql : countSqls) {
//            Long  size= iMarketingSyncUserService.countRepeat(countSql);
//            if(size>=1){
//                countMark = true;
//                break;
//            }
//        }
//
//        if(countMark){
//            String updateInValidSql = String.format("update b_marketing_sync_%s set is_repeat=3 where id = %d"
//                    ,syncUser.getApiCode(),syncUser.getId());
//            iMarketingSyncUserService.updateRepeatUserStatus(updateInValidSql);
//            return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(2);
//        }
//        TodayIdTimeBySoleVo soleVo = null;
//        for (String todaySql : todaySqls) {
//            TodayIdTimeBySoleVo soleValidUser = iMarketingSyncUserService.getSoleValidUser(todaySql);
//            if(soleVo==null){
//                soleVo = soleValidUser;
//            }else{
//                if(soleVo.getAppletTime().compareTo(soleValidUser.getAppletTime())<0){
//                    continue;
//                }
//                if(soleVo.getAppletTime().compareTo(soleValidUser.getAppletTime())>0){
//                    soleVo = soleValidUser;
//                    continue;
//                }
//                if(soleVo.getId()>soleValidUser.getId()){
//                    soleVo = soleValidUser;
//                    continue;
//                }
//            }
//        }
//        if(soleVo!=null&&syncUser.getId().equals(soleVo.getId())){
//            String updateValidSql = String.format("update b_marketing_sync_%s set is_repeat=2 where id = %d"
//                    ,syncUser.getApiCode(),syncUser.getId());
//            iMarketingSyncUserService.updateRepeatUserStatus(updateValidSql);
//            return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(1);
//        }else{
//            String updateInValidSql = String.format("update b_marketing_sync_%s set is_repeat=3 where id = %d"
//                    ,syncUser.getApiCode(),syncUser.getId());
//            iMarketingSyncUserService.updateRepeatUserStatus(updateInValidSql);
//            return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(2);
//        }
//    }

    /**
     *  去重方法
     *  新建两条where条件，一条是T-1前，一条是T日
     *  遍历所有的去重规则，进行where条件拼接（参与去重的数据源为 未去重数据（1） 和 不重复的数据（2））
     *  T-1 前的数据有 -> 则认为重复（3）
     *            无 -> 获取T日 满足条件的最小时间的数据id  相等  -> 不重复（2）
     *                                               不相等 -> 重复（3）
     */
    @Override
    public Result<Integer> actionSole(List<CustomerSoleRuleVO> soleRuleVOS, MarketingSyncUser syncUser) {
        List<CustomerSoleRuleVO> customerSoleRuleVO = this.matchSoleRule(soleRuleVOS, syncUser);
        if(customerSoleRuleVO.size()<=0){
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(1).setMessage("数据无需去重");
        }
        /** 多规则T-1日内的wehere条件 */
        StringBuilder soleSql = new StringBuilder();
        /** 多规则T日内的wehere条件 */
        StringBuilder soleSqlWhereToday = new StringBuilder();
        //一条数据 满足多条去重规则标志 true是多条
        boolean rulesMark = false;
        //region 多规则拼接
        List<String> countSqls = new ArrayList<>();
        List<String> todaySqls = new ArrayList<>();
        for (CustomerSoleRuleVO soleRuleVO : customerSoleRuleVO) {
            /** 查询T-1时间内已经去重统计过的数据的where条件 */
            StringBuilder dbWhereStr = new StringBuilder();
            /** 查询T日内的未统计的去重的数据的where条件 */
            StringBuilder dbWhereTodayStr = new StringBuilder();

            LocalDate now = LocalDate.parse(syncUser.getAppletDate(),DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String endTimeNow = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
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
                dbWhereStr.append(soleStr);
                dbWhereTodayStr.append(soleStr);
            }

            /** 拼接场景条件 */
            if(StringUtils.isNotBlank(soleRuleVO.getConditionDbDesc())){
                dbWhereStr.append(" and ").append(String.format("(%s)",soleRuleVO.getConditionDbDesc()));
                dbWhereTodayStr.append(" and ").append(String.format("(%s)",soleRuleVO.getConditionDbDesc()));
            }

            /** 去重字段筛选 */
            dbWhereStr.append(" and ").append(" is_repeat in (1,2) ");
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
        
        boolean isRepat = true; //未重复
        for (String todaySql : todaySqls) {
            TodayIdTimeBySoleVo soleValidUser = iMarketingSyncUserService.getSoleValidUser(todaySql);
            if(!syncUser.getId().equals(soleValidUser.getId())){
                isRepat = false;
                break;
            }
        }
        if(isRepat){
            String updateValidSql = String.format("update b_marketing_sync_%s set is_repeat=2 where id = %d"
                    ,syncUser.getApiCode(),syncUser.getId());
            iMarketingSyncUserService.updateRepeatUserStatus(updateValidSql);
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
