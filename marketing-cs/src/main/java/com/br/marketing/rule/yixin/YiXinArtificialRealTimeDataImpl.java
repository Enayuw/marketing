package com.br.marketing.rule.yixin;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import com.br.marketing.service.customertagsprocess.valobj.CustomerTagsValue;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDetailByRuleDTO;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDetailDTO;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.impl.YiXinRuleCollectDataImpl;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.enums.ScoreThreeKeyEncryptEnum;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.service.PushRuleService;
import com.br.marketing.service.ZnkfPushService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import com.google.common.collect.Sets;

import lombok.extern.slf4j.Slf4j;

/**
 * code is far away from bug with the animal protecting ┏┓ ┏┓ ┏┛┻━━━┛┻┓ ┃ ┃ ┃ ━ ┃ ┃ ┳┛ ┗┳ ┃ ┃ ┃ ┃ ┻ ┃ ┃ ┃ ┗━┓ ┏━┛ ┃ ┃神兽保佑 ┃ ┃代码无BUG！ ┃ ┗━━━┓ ┃ ┣┓ ┃ ┏┛
 * ┗┓┓┏━┳┓┏┛ ┃┫┫ ┃┫┫ ┗┻┛ ┗┻┛
 *
 * @Description : 宜信实时数据转吊销 ---------------------------------
 * @Author : jilong.xu
 * @Date : Create in 2022/3/28 15:29
 */
@Service
@Slf4j
public class YiXinArtificialRealTimeDataImpl implements AssembleData<PushMarketingUserDetailByRuleDTO> {

    @Resource
    private ZnkfPushService znkfPushService;

    @Resource
    MarketingTransferSyncUserMapper transferSyncUserMapper;

    private final static String CUSTOMER_NUMBER_IS_FIRST = "customer:realtime:first";

    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    @Autowired
    PushRuleService pushRuleService;

    @Override
    public PushMarketingUserDetailByRuleDTO assemble(Object transmitFact, ProcessHandlerContext context) {
        log.warn("开始组装推决策参数 YiXin_NonRealTime_Policy ");
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser)transmitFact;
        HashMap<String, Integer> pushCellEncPolicy = marketingCommonConfig.getPushCellEncPolicy();
        Integer encType = ScoreThreeKeyEncryptEnum.md5.getValue();
        if (pushCellEncPolicy != null && pushCellEncPolicy.get(context.getApiCode()) != null) {
            encType = pushCellEncPolicy.get(context.getApiCode());
        }
        YiXinRuleCollectDataImpl.YiXinRuleNecessaryData ruleNecessaryData =
            (YiXinRuleCollectDataImpl.YiXinRuleNecessaryData)context.getRuleNecessaryData();
        SyncUserValidityPeriodsBO syncUserValidityPeriodsBO = ruleNecessaryData.getCustomerMap().get(transfer.getCustNum());
        if (syncUserValidityPeriodsBO == null) {
            return null;
        }
        MarketingSyncUser marketingSyncUser = syncUserValidityPeriodsBO.getSyncUsers().get(0);
        PushMarketingUserDetailByRuleDTO pushMarketingUserDetailByRuleDTO = new PushMarketingUserDetailByRuleDTO();

        pushMarketingUserDetailByRuleDTO.setInitId(transfer.getId());
        pushMarketingUserDetailByRuleDTO.setCaseNumber(transfer.getCustNum());

        PushMarketingUserDetailDTO marketingUserDetailDTO = new PushMarketingUserDetailDTO();
        marketingUserDetailDTO.setCaseNumber(transfer.getCustNum());
        String cell = BrCipherMaker.getInstance().decode(marketingSyncUser.getCell());
        String phone = pushRuleService.encrypt3k(encType, BrCipherMaker.getInstance().decode(cell));
        pushMarketingUserDetailByRuleDTO.setPhone(phone);
        pushMarketingUserDetailByRuleDTO.setCell(cell);
        pushMarketingUserDetailByRuleDTO.setStrategyCode("");
        JSONObject parseObject = JSON.parseObject(transfer.getReserveField1());
        String liveType = parseObject.getString("liveType");
        JSONObject jsonObject = JSONObject.parseObject(marketingSyncUser.getReserveField1());
        if ("1".equals(liveType) || "2".equals(liveType)){
            pushMarketingUserDetailByRuleDTO.setBatchNumber("rg8_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
            pushMarketingUserDetailByRuleDTO.setStatus(liveType);
            jsonObject.put("batchNumber", "rg8_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        } else if ("3".equals(liveType) || "8".equals(liveType) || "9".equals(liveType)){
            pushMarketingUserDetailByRuleDTO.setBatchNumber("rg9_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
            pushMarketingUserDetailByRuleDTO.setStatus(liveType);
            jsonObject.put("batchNumber", "rg9_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        } else {
            log.warn("宜信实时推决策liveType字段非(1、2、3、8 )，liveType：{}，custNum：{}", liveType, transfer.getCustNum());
            return null;
        }
        jsonObject.put("cell", cell);
        buildJson(jsonObject, marketingSyncUser);

        Map<String, Object> stringObjectMap = entityToMapWithBeanMap(transfer);
        mergeJSONObjects(jsonObject, stringObjectMap);

        pushMarketingUserDetailByRuleDTO.setVariables(jsonObject);
        return pushMarketingUserDetailByRuleDTO;

    }

    private JSONObject buildJson(JSONObject jsonObject, MarketingSyncUser syncUser) {
        jsonObject.put("cusBatch", emptyDefault(syncUser.getCusBatch()));
        jsonObject.put("requestBatch", emptyDefault(syncUser.getRequestBatch()));
        jsonObject.put("custNum", emptyDefault(syncUser.getCustNum()));
        jsonObject.put("idCard", emptyDefault(get3keyValue(syncUser.getIdCard(), "idCard", CustomerTagsValue.PushJc3keyTypeEnum.MD5_ALL.getValue())));
        jsonObject.put("name", emptyDefault(get3keyValue(syncUser.getName(), "name", CustomerTagsValue.PushJc3keyTypeEnum.MD5_ALL.getValue())));
        jsonObject.put("groupType", emptyDefault(syncUser.getGroupType()));
        jsonObject.put("userType", emptyDefault(syncUser.getUserType()));
        jsonObject.put("registerDate", emptyDefault(syncUser.getRegisterDate()));
        jsonObject.put("appletDate", emptyDefault(syncUser.getAppletDate()));
        jsonObject.put("taskId", emptyDefault(syncUser.getCusBatch()));
        return jsonObject;
    }

    public static Map<String, Object> entityToMapWithBeanMap(Object entity) {
        Map<String, Object> resultMap = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();

        BeanMap beanMap = BeanMap.create(entity);

        for (Object keyObj : beanMap.keySet()) {
            String key = keyObj.toString();
            Object value = beanMap.get(keyObj);

            if ("id".equals(key) || "createTime".equals(key) || "updateTime".equals(key)
                    || "tCid".equals(key) || "cid".equals(key) || "status".equals(key)
                    || "isTask".equals(key) || "taskTime".equals(key) || "isRepeat".equals(key)) {
                continue;
            }

            if (value == null || "".equals(value.toString().trim())) {
                continue;
            }

            // 处理 reserveField1 和 reserveField2
            if ("reserveField1".equals(key) || "reserveField2".equals(key)) {
                if (value instanceof String) {
                    try {
                        // 将 JSON 字符串解析为 Map 并合并到结果中
                        Map<String, Object> nestedMap = objectMapper.readValue(value.toString(), new TypeReference<Map<String, Object>>() {});
                        resultMap.putAll(nestedMap);
                    } catch (Exception e) {
                        log.warn("reserveField1或reserveField2不是 JSON 格式，跳过！, key :" + key);
                    }
                }
            } else {
                resultMap.put(key, value);
            }
        }
        return resultMap;
    }

    public JSONObject mergeJSONObjects(JSONObject jsonObject, Map<String, Object> map) {
        if (jsonObject == null) {
            return map == null ? new JSONObject() : new JSONObject(map);
        }
        if (map == null) {
            return jsonObject;
        }

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value == null || "".equals(value.toString())) {
                continue;
            }
            if (!jsonObject.containsKey(key)) {
                jsonObject.put(key, value);
            }
        }
        return jsonObject;
    }

    private String emptyDefault(String value) {
        return com.br.common.util.StringUtils.isNotEmpty(value) ? value : "";
    }

    private String get3keyValue(String content, String contentType, Integer encryptionType) {

        if (org.apache.commons.lang3.StringUtils.isBlank(content)) {
            return content;
        }

        if (CustomerTagsValue.PushJc3keyTypeEnum.MD5_ALL.getValue().equals(encryptionType)) {
            String decode = BrCipherMaker.getInstance().decode(content);
            return org.apache.commons.lang3.StringUtils.isNotBlank(decode) ? DigestUtils.md5DigestAsHex(decode.getBytes()) : content;
        }

        return null;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser)transmitFact;
        String tCid = transfer.gettCid();
        String apiCode = transfer.getApiCode();
        String reserveField1 = transfer.getReserveField1();
        if (StringUtils.hasText(reserveField1)) {
            YiXinRuleCollectDataImpl.YiXinRuleNecessaryData ruleNecessaryData =
                (YiXinRuleCollectDataImpl.YiXinRuleNecessaryData)context.getRuleNecessaryData();
            JSONObject json = JSON.parseObject(reserveField1);
            boolean transformType = "1".equals(json.getString("transformType"));
            Integer liveType = json.getInteger("liveType");
            String key = CUSTOMER_NUMBER_IS_FIRST.concat(":").concat(transfer.getCustNum());
            Map<String, String> blackList = ruleNecessaryData.getBlackList();
            boolean notBlack = true;
            if (!CollectionUtils.isEmpty(blackList)) {
                notBlack = "N".equals(blackList.get(transfer.getId().toString()));
            }
            Integer isDelay = context.getMqFact().getIsDelay();
            SyncUserValidityPeriodsBO syncUserValidityPeriodsBO = ruleNecessaryData.getCustomerMap().get(transfer.getCustNum());
            if (syncUserValidityPeriodsBO == null) {
                return false;
            }
            MarketingSyncUser marketingSyncUser = syncUserValidityPeriodsBO.getSyncUsers().get(0);
            boolean messageDelay = isDelay != null && isDelay == 1;
            if (marketingSyncUser == null) {
                log.warn("宜信实时推决策上传表记录不在有效期内 --{} ", transfer.getCustNum());
                return false;
            } else {
                String decode = BrCipherMaker.getInstance().decode(marketingSyncUser.getCell());
                if (StringUtils.isEmpty(decode)) {
                    log.warn("宜信实时推决策手机号解密失败 --{} ", transfer.getCustNum());
                    return false;
                }
            }
            /*
            满足条件立即推送
                1、不满足客服黑名单
                2、transformType 为1
                3、立即推送liveType 1,2,3或者 从延迟队列过来的消息
                4、当天该案件编号未被推送
             */
            if (!notBlack) {
                log.warn("宜信实时推决策id:{} cust_num:{}不满足黑名单条件", transfer.getId(), transfer.getCustNum());
                return false;
            }
            boolean flag = transformType && (Arrays.asList(1, 2, 3).contains(liveType) || messageDelay);
            if (!flag) {
                log.warn("宜信实时推决策id:{} cust_num:{}不满足立即推送条件", transfer.getId(), transfer.getCustNum());
                return false;
            }
            if (!znkfPushService.cusNumIsFirstToday(key)) {
                log.warn("宜信实时推决策id:{} cust_num:{}不满足当天推送条件", transfer.getId(), transfer.getCustNum());
                return false;
            }
            Set<String> custNums = Sets.newHashSet(transfer.getCustNum());
            List caseEffectiveCust = transferSyncUserMapper.getByInCustAndCaseEffective(tCid, apiCode, custNums);
            if (!CollectionUtils.isEmpty(caseEffectiveCust)) {
                log.warn("宜信实时推决策id:{} cust_num:{}caseEffetive=0 剔除", transfer.getId(), transfer.getCustNum());
                return false;
            }

            return true;
        }
        return false;
    }

    @Override
    public String label() {
        return "YiXin_RealTimeData_ArtificialToPolicyRule";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.YIXIN_REALTIME_TO_POLICY.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return RuleDataCollectionEnum.YI_XIN_DATA_COLLECTION.getCode();
    }
}
