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
import com.br.marketing.strategy.PolicyHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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
    InitDataToPolicyImpl initDataToPolicyImpl;

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
            ProcessHandlerContext context = new ProcessHandlerContext();
            context.setApiCode(apiCode);
            MqFact mqFact = new MqFact();
            mqFact.setSource(source);
            mqFact.setSourceId(null);
            context.setMqFact(mqFact);

            LocalDateTime localDateTime = LocalDateTime.now().minusMinutes(intervalTime);
            ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.systemDefault());
            Date date = Date.from(zonedDateTime.toInstant());
            PeriodPushLogExample periodPushLogExample = new PeriodPushLogExample();
            periodPushLogExample.createCriteria()
                    .andApiCodeEqualTo(apiCode)
                    .andSourceEqualTo(source)
                    .andStatusEqualTo(1)
                    .andIsDelEqualTo(1)
                    .andCreateTimeLessThanOrEqualTo(date);
            periodPushLogExample.setOrderByClause(" create_time limit 2000");
            List<PeriodPushLog> periodPushLogList = periodPushLogMapper.selectByExample(periodPushLogExample);
            // 获取满足时间间隔的数据并获取不超过2000个id的数据
            List<Long> idList = new ArrayList<>();
            periodPushLogList.stream().forEach((PeriodPushLog t)->{
                String[] split = t.getIds().split(",");
                int size = idList.size() + split.length;
                if(size<2001){
                    List<Long> idLongList = Arrays.stream(split)
                            .map(Long::parseLong)
                            .collect(Collectors.toList());
                    idList.addAll(idLongList);
                }else{
                    return;
                }
            });
            // 计算id的数量，保证每次不超过2000个id调用决策接口
            if(TransferSource.INIT_DATA_SET_PROCESS.getCode() == source){
                MarketingSyncInfoExample marketingSyncInfoExample = new MarketingSyncInfoExample();
                marketingSyncInfoExample.createCriteria()
                        .andApiCodeEqualTo(apiCode).andIdIn(idList);
                List<MarketingSyncInfo> marketingSyncInfos = marketingSyncInfoMapper.selectByExample(marketingSyncInfoExample);
                // 是否满足有效期，去重等规则（本次不做）

                // 参数拼装
                List<PushMarketingUserDetailByRuleDTO> policyByRuleList = marketingSyncInfos.stream().map((MarketingSyncInfo t) -> {
                    try {
                        return assemble(t, context);
//                        return initDataToPolicyImpl.assemble(t, context);
                    } catch (Exception e) {
                        log.warn("按分钟级隔离调用决策参数拼接异常,apiCode:{}-id:{}--", apiCode, t.getId(), e);
                    }
                    return null;
                }).collect(Collectors.toList());
                try{
                    // 调用接口
//                    policyHandler.call(policyByRuleList,context);
                    Result result = batchCall(policyByRuleList, context);
                    int num = policyByRuleList.size();
                    PeriodPushLogExample example = new PeriodPushLogExample();
                    example.createCriteria()
                            .andApiCodeEqualTo(apiCode)
                            .andIsDelEqualTo(1)
                            .andStatusEqualTo(1)
                            .andSourceEqualTo(source);
                    PeriodPushLog periodPushLog = new PeriodPushLog();
                    periodPushLog.setPushNum(num);
                    if (ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                        periodPushLog.setStatus(2);
                        periodPushLog.setFailNum(0);
                        periodPushLogMapper.updateByExampleSelective(periodPushLog, example);
                    }else{
                        periodPushLog.setStatus(3);
                        periodPushLog.setFailNum(num);
                        periodPushLogMapper.updateByExampleSelective(periodPushLog, example);
                    }
                } catch (Exception e) {
                    log.error("apiCode:[{}]调用三方接口处理异常,idList[{}]--",apiCode, JSON.toJSONString(idList), e);
                }
            }else{
                // 本次暂不处理
            }
        }
    }

    public PushMarketingUserDetailByRuleDTO assemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        HashMap<String, Integer> pushCellEncPolicy = marketingCommonConfig.getPushCellEncPolicy();
        Integer encType = ScoreThreeKeyEncryptEnum.md5.getValue();
        if (pushCellEncPolicy != null && pushCellEncPolicy.get(context.getApiCode()) != null) {
            encType = pushCellEncPolicy.get(context.getApiCode());
        }
        MarketingSyncUser syncUser = (MarketingSyncUser) transmitFact;
        PushMarketingUserDetailByRuleDTO pushMarketingUserDetailByRuleDTO = new PushMarketingUserDetailByRuleDTO();
        pushMarketingUserDetailByRuleDTO.setInitId(syncUser.getId());
        pushMarketingUserDetailByRuleDTO.setCaseNumber(syncUser.getCustNum());
        pushMarketingUserDetailByRuleDTO.setPhone(pushRuleService.encrypt3k(encType, BrCipherMaker.getInstance().decode(syncUser.getCell())));
        JSONObject varDto = new JSONObject();
        String reserveField1 = syncUser.getReserveField1();
        JSONObject reserveField1JSONObject = null;
        if (JSON.isValid(reserveField1)) {
            reserveField1JSONObject = JSONObject.parseObject(reserveField1);
        }
        if(null != reserveField1JSONObject){
            varDto.putAll(reserveField1JSONObject);
        }
        varDto.put("groupType", syncUser.getUserType());
        varDto.put("id", pushRuleService.encrypt3k(encType, BrCipherMaker.getInstance().decode(syncUser.getIdCard())));
        varDto.put("name", pushRuleService.encrypt3k(encType, BrCipherMaker.getInstance().decode(syncUser.getName())));
        if (StringUtils.isNotBlank(reserveField1)) {
            JSONObject initJson = JSON.parseObject(reserveField1);
            for (String s : initJson.keySet()) {
                varDto.put(s, initJson.getString(s));
                if (s.toLowerCase().equals("strategycode")) {
                    pushMarketingUserDetailByRuleDTO.setStrategyCode(initJson.getString(s));
                }
                if (s.toLowerCase().equals("batchnumber")) {
                    pushMarketingUserDetailByRuleDTO.setBatchNumber(initJson.getString(s));
                }
            }
        }
        if (StringUtils.isBlank(pushMarketingUserDetailByRuleDTO.getStrategyCode())) {
            pushMarketingUserDetailByRuleDTO.setStrategyCode("");
        }
        pushMarketingUserDetailByRuleDTO.setVariables(varDto);
        return pushMarketingUserDetailByRuleDTO;
    }

    public Result batchCall(List<PushMarketingUserDetailByRuleDTO> policyByRuleList, ProcessHandlerContext context) {
        ArrayList<PushMarketingUserDetailDTO> pushs = new ArrayList<>();
        List<Long> sourceIds = new ArrayList<>();
        policyByRuleList.forEach(t->{
            PushMarketingUserDetailDTO entity = new PushMarketingUserDetailDTO();
            BeanUtils.copyProperties(t, entity);
            pushs.add(entity);
            sourceIds.add(t.getInitId());
        });
        PushMarketingUserTaskInfoDTO taskInfoDTO = new PushMarketingUserTaskInfoDTO();
        taskInfoDTO.setData(pushs);
        taskInfoDTO.setAccessNumber(UUID.randomUUID().toString());
        taskInfoDTO.setMethod("caseAdd");
        taskInfoDTO.setBatchNumber(context.getApiCode());
//        taskInfoDTO.setStrategyCode(strategy);

        PushMarketingUserDTO pushMarketingUserDTO = new PushMarketingUserDTO();
        pushMarketingUserDTO.setApiCode(context.getApiCode());
        pushMarketingUserDTO.setJsonData(taskInfoDTO);

        PolicyRetryByRuleDTO retryByRuleDTO = new PolicyRetryByRuleDTO();
        retryByRuleDTO.setIds(sourceIds);
        retryByRuleDTO.setInfoId(context.getMqFact().getSourceId());
        retryByRuleDTO.setPushMarketingUserDTO(pushMarketingUserDTO);
        return methodRetryHandlerService.callPolicyData(retryByRuleDTO, null);
    }
}
