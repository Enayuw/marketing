package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.client.intelligentcustomerservice.input.*;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.*;
import com.br.marketing.enums.ScoreThreeKeyEncryptEnum;
import com.br.marketing.mapper.MarketingSyncInfoMapper;
import com.br.marketing.mapper.PeriodPushLogMapper;
import com.br.marketing.origin.MqFact;
import com.br.marketing.origin.TransferSource;
import com.br.marketing.rule.ibu.InitDataToPolicyImpl;
import com.br.marketing.service.IPeriodPushService;
import com.br.marketing.service.PushRuleService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.MethodRetryHandlerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 周期调用决策处理实现类
 * @Author: yu.xia@brgroup.com
 * @Date: 2024-05-28
 */
@Slf4j
@Service
public class PeriodPushServiceImpl implements IPeriodPushService {

    @Resource
    MarketingCommonConfig marketingCommonConfig;

    @Resource
    MarketingSyncInfoMapper marketingSyncInfoMapper;

    @Resource
    private PeriodPushLogMapper periodPushLogMapper;

    @Resource
    private MethodRetryHandlerService methodRetryHandlerService;

    @Resource
    PushRuleService pushRuleService;

    @Override
    public void handle() {
        // 获取apiCode对应的间隔时间配置
        Map<String, JSONObject> periodPushConfig = marketingCommonConfig.getPeriodPushConfig();
        // 处理数据，按照时间间隔给apiCode进行排序

        // 根据apiCode获取2000个条数据创建时间和待处理数据的字段ids（拿2000条是为了防止出现每个数据记录中ids只有1个id的情况）
        for (Map.Entry<String,JSONObject> entry : periodPushConfig.entrySet()) {
            String apiCode = entry.getKey();
            JSONObject jsonObject = entry.getValue();
            // 原始数据来源
            Integer source = jsonObject.getInteger("source");
            // 间隔时间（默认分钟）
            Integer intervalTime = jsonObject.getInteger("intervalTime");
            // TODO 加锁 加线程池

            ProcessHandlerContext context = new ProcessHandlerContext();
            context.setApiCode(apiCode);
            MqFact mqFact = new MqFact();
            mqFact.setSource(source);
            mqFact.setSourceId(null);
            context.setMqFact(mqFact);

            LocalDateTime localDateTime = LocalDateTime.now().minusMinutes(intervalTime);
            ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.systemDefault());
            Date date = Date.from(zonedDateTime.toInstant());
            while(true){
                PeriodPushLogExample periodPushLogExample = new PeriodPushLogExample();
                periodPushLogExample.createCriteria()
                        .andApiCodeEqualTo(apiCode)
                        .andSourceEqualTo(source)
                        .andStatusEqualTo(1)
                        .andIsDelEqualTo(1)
                        .andCreateTimeLessThanOrEqualTo(date);
                periodPushLogExample.setOrderByClause(" create_time limit 2000");
                List<PeriodPushLog> periodPushLogList = periodPushLogMapper.selectByExample(periodPushLogExample);
                if(null == periodPushLogList || periodPushLogList.size()<1){
                    break;
                }
                List<Long> periodPushLogIdList = new ArrayList<>();
//                List<Long> periodPushLogIdList = periodPushLogList.stream().map(PeriodPushLog::getId).collect(Collectors.toList());
                // 获取满足时间间隔的上传详情表数据并获取不超过2000批的数据
                List<Long> idsList = new ArrayList<>();
                periodPushLogList.stream().forEach((PeriodPushLog t)->{
                    String[] split = t.getIds().split(",");
                    int size = idsList.size() + split.length;
                    if(size<2001){
                        List<Long> idLongList = Arrays.stream(split)
                                .map(Long::parseLong)
                                .collect(Collectors.toList());
                        idsList.addAll(idLongList);
                        periodPushLogIdList.add(t.getId());
                    }else{
                        return;
                    }
                });
                // 每次不超过2000个id调用决策接口
                List<MarketingSyncUser> syncUserList = marketingSyncInfoMapper.getDataByIdList(apiCode, idsList);

                if(null == syncUserList || syncUserList.size()<1){
                    continue;
                }
                // 获取满足条件的数据并进行参数拼装
                List<PushMarketingUserDetailByRuleDTO> policyByRuleList = syncUserList.stream().map((MarketingSyncUser t) -> {
                    try {
                        return assemble(t, context);
//                        return initDataToPolicyImpl.assemble(t, context);
                    } catch (Exception e) {
                        log.warn("按分钟级隔离调用决策参数拼接异常,apiCode:{}-id:{}--", apiCode, t.getId(), e);
                    }
                    return null;
                }).filter((PushMarketingUserDetailByRuleDTO t) -> t != null).collect(Collectors.toList());

                if(policyByRuleList.size()>0){
                    // 调用接口
                    batchCall(policyByRuleList, context, periodPushLogIdList);
                }else{
                    PeriodPushLogExample example = new PeriodPushLogExample();
                    example.createCriteria()
                            .andApiCodeEqualTo(apiCode)
                            .andIsDelEqualTo(1)
                            .andStatusEqualTo(1)
                            .andSourceEqualTo(source)
                            .andIdIn(periodPushLogIdList);
                    PeriodPushLog periodPushLog = new PeriodPushLog();
                    periodPushLog.setPushNum(0);
                    periodPushLog.setFailNum(0);
                    periodPushLog.setStatus(5);
                    periodPushLog.setErrorContent("不满足operateType=1筛选数据条件");
                    periodPushLogMapper.updateByExampleSelective(periodPushLog, example);
                }
            }
        }
    }

    public PushMarketingUserDetailByRuleDTO assemble(MarketingSyncUser syncUser, ProcessHandlerContext context) throws Exception {
        PushMarketingUserDetailByRuleDTO pushMarketingUserDetailByRuleDTO = new PushMarketingUserDetailByRuleDTO();
        String reserveField1 = syncUser.getReserveField1();
        if (JSON.isValid(reserveField1)) {
            pushMarketingUserDetailByRuleDTO.setInitId(syncUser.getId());
            pushMarketingUserDetailByRuleDTO.setCaseNumber(syncUser.getCustNum());
            HashMap<String, Integer> pushCellEncPolicy = marketingCommonConfig.getPushCellEncPolicy();
            Integer encType = ScoreThreeKeyEncryptEnum.md5.getValue();
            if (pushCellEncPolicy != null && pushCellEncPolicy.get(context.getApiCode()) != null) {
                encType = pushCellEncPolicy.get(context.getApiCode());
            }
            pushMarketingUserDetailByRuleDTO.setPhone(pushRuleService.encrypt3k(encType, BrCipherMaker.getInstance().decode(syncUser.getCell())));
            JSONObject varDto = new JSONObject();
            JSONObject reserveField1JSONObject = JSON.parseObject(reserveField1);
            if(null != reserveField1JSONObject && !reserveField1JSONObject.isEmpty()){
                String operateType = reserveField1JSONObject.getString("operateType");
                if("1".equals(operateType)){
                    varDto.putAll(reserveField1JSONObject);
                    varDto.put("groupType", syncUser.getUserType());
                    varDto.put("id", pushRuleService.encrypt3k(encType, BrCipherMaker.getInstance().decode(syncUser.getIdCard())));
                    varDto.put("name", pushRuleService.encrypt3k(encType, BrCipherMaker.getInstance().decode(syncUser.getName())));
                    String apiCode = syncUser.getApiCode();
                    String nowDay = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                    for (String s : reserveField1JSONObject.keySet()) {
                        varDto.put(s, reserveField1JSONObject.getString(s));
                        if (s.toLowerCase().equals("strategycode")) {
                            pushMarketingUserDetailByRuleDTO.setStrategyCode(reserveField1JSONObject.getString(s));
                        }
//                        if (s.toLowerCase().equals("batchnumber")) {
//                            pushMarketingUserDetailByRuleDTO.setBatchNumber(reserveField1JSONObject.getString(s));
//                        }
                    }
                    if (StringUtils.isBlank(pushMarketingUserDetailByRuleDTO.getStrategyCode())) {
                        pushMarketingUserDetailByRuleDTO.setStrategyCode("");
                    }
                    if (StringUtils.isBlank(pushMarketingUserDetailByRuleDTO.getBatchNumber())) {
                        pushMarketingUserDetailByRuleDTO.setBatchNumber(nowDay+"_"+apiCode);
                    }
                    pushMarketingUserDetailByRuleDTO.setVariables(varDto);
                    return pushMarketingUserDetailByRuleDTO;
                }
            }
        }
        return null;
    }

    /**
     * 批量推送方法
     * @Author yu.xia@brgroup.com
     * @Date 2024/5/31 11:04
     * @param policyByRuleList 满足条件的数据
     * @param context context
     * @param idList 待推送数据在 b_period_push_log 的记录
     */
    public void batchCall(List<PushMarketingUserDetailByRuleDTO> policyByRuleList
            , ProcessHandlerContext context, List<Long> idList) {
        String apiCode = context.getApiCode();
        Integer source = context.getMqFact().getSource();
//        Map<String, List<PushMarketingUserDetailByRuleDTO>> strategyMap = policyByRuleList.stream()
//                .collect(Collectors.groupingBy(PushMarketingUserDetailByRuleDTO::getStrategyCode));
        int successNum = 0;
        int errorNum = 0;
//        for (String strategy : strategyMap.keySet()) {
            ArrayList<PushMarketingUserDetailDTO> pushs = new ArrayList<>();
            List<Long> sourceIds = new ArrayList<>();
//            List<PushMarketingUserDetailByRuleDTO> datas = strategyMap.get(strategy);
        policyByRuleList.forEach((PushMarketingUserDetailByRuleDTO t)->{
                PushMarketingUserDetailDTO entity = new PushMarketingUserDetailDTO();
                BeanUtils.copyProperties(t, entity);
                pushs.add(entity);
                sourceIds.add(t.getInitId());
            });
            PushMarketingUserTaskInfoDTO taskInfoDTO = new PushMarketingUserTaskInfoDTO();
            taskInfoDTO.setData(pushs);
            taskInfoDTO.setAccessNumber(UUID.randomUUID().toString());
            taskInfoDTO.setMethod("caseAdd");
            taskInfoDTO.setBatchNumber(apiCode);
            // 成熟之后需要根据客户入参字段选择性添加这个字段（加上后决策会走自动流程）
//            taskInfoDTO.setStrategyCode(strategy);

            PushMarketingUserDTO pushMarketingUserDTO = new PushMarketingUserDTO();
            pushMarketingUserDTO.setApiCode(apiCode);
            pushMarketingUserDTO.setJsonData(taskInfoDTO);

            PolicyRetryByRuleDTO retryByRuleDTO = new PolicyRetryByRuleDTO();
            retryByRuleDTO.setIds(sourceIds);
            retryByRuleDTO.setInfoId(context.getMqFact().getSourceId());
            retryByRuleDTO.setPushMarketingUserDTO(pushMarketingUserDTO);
            Result result = methodRetryHandlerService.callPolicyData(retryByRuleDTO, null);
            if (ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                successNum = policyByRuleList.size();
//                successNum = successNum + datas.size();
            }else{
                errorNum = policyByRuleList.size();
//                errorNum = errorNum + datas.size();
            }
//        }
        int num = policyByRuleList.size();
        PeriodPushLogExample example = new PeriodPushLogExample();
        example.createCriteria()
                .andApiCodeEqualTo(apiCode)
                .andIsDelEqualTo(1)
                .andStatusEqualTo(1)
                .andSourceEqualTo(source)
                .andIdIn(idList);
        PeriodPushLog periodPushLog = new PeriodPushLog();
        periodPushLog.setPushNum(num);
        periodPushLog.setFailNum(errorNum);
        if (errorNum > 0) {
            periodPushLog.setStatus(3);
            log.warn("[{}]间隔推送决策发现推送不成功数据-total:{}-success:{}-error:{}", apiCode, num, successNum, errorNum);
        }else{
            periodPushLog.setStatus(2);
        }
        periodPushLogMapper.updateByExampleSelective(periodPushLog, example);
    }
}
