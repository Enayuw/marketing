package com.br.marketing.rule.yixin;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDetailByRuleDTO;
import com.br.marketing.common.enums.SoleFieldEnum;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.impl.YiXinRuleCollectDataImpl;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.enums.ScoreThreeKeyEncryptEnum;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.origin.TransferSource;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.service.PushRuleService;
import com.br.marketing.service.customertagsprocess.CustomerTagsProcessServiceImpl;
import com.br.marketing.service.customertagsprocess.valobj.CustomerTagsValue;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @Description : 宜信非实时数据转决策
 * ---------------------------------
 * @Author : zhen.Li1
 * @Date : Create in 2024/5/29 15:29
 */
@Service
@Slf4j
public class YiXinNonTimeToPolicyImpl implements AssembleData<PushMarketingUserDetailByRuleDTO> {

    @Resource
    MarketingTransferSyncUserMapper transferSyncUserMapper;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Autowired
    private PushRuleService pushRuleService;

    @Resource
    CustomerTagsProcessServiceImpl customerTagsProcessService;

    @Override
    public PushMarketingUserDetailByRuleDTO assemble(Object transmitFact, ProcessHandlerContext context) {
        log.warn("开始组装推决策参数 YiXin_NonRealTime_Policy ");
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        HashMap<String, Integer> pushCellEncPolicy = marketingCommonConfig.getPushCellEncPolicy();
        Integer encType = ScoreThreeKeyEncryptEnum.md5.getValue();
        if (pushCellEncPolicy != null && pushCellEncPolicy.get(context.getApiCode()) != null) {
            encType = pushCellEncPolicy.get(context.getApiCode());
        }
        PushMarketingUserDetailByRuleDTO pushMarketingUserDetailByRuleDTO = new PushMarketingUserDetailByRuleDTO();

        pushMarketingUserDetailByRuleDTO.setInitId(transfer.getId());
        pushMarketingUserDetailByRuleDTO.setCaseNumber(transfer.getCustNum());
        YiXinRuleCollectDataImpl.YiXinRuleNecessaryData ruleNecessaryData =
                (YiXinRuleCollectDataImpl.YiXinRuleNecessaryData) context.getRuleNecessaryData();
        SyncUserValidityPeriodsBO syncUserValidityPeriodsBO = ruleNecessaryData.getCustomerMap().get(transfer.getCustNum());
        MarketingSyncUser marketingSyncUser = syncUserValidityPeriodsBO.getSyncUsers().get(0);
        String cell = marketingSyncUser.getCell();
        cell = pushRuleService.encrypt3k(encType, BrCipherMaker.getInstance().decode(cell));
        pushMarketingUserDetailByRuleDTO.setPhone(cell);
        pushMarketingUserDetailByRuleDTO.setCell(BrCipherMaker.getInstance().decode(marketingSyncUser.getCell()));
        pushMarketingUserDetailByRuleDTO.setBatchNumber(getBatchNumber(transfer.getType()));

        JSONObject jsonObject = JSONObject.parseObject(marketingSyncUser.getReserveField1());
        jsonObject.put("cell", cell);
        jsonObject.put("batchNumber", getBatchNumber(transfer.getType()));
        buildJson(jsonObject, marketingSyncUser);

        Map<String, Object> stringObjectMap = entityToMapWithBeanMap(transfer);
        mergeJSONObjects(jsonObject, stringObjectMap);

        pushMarketingUserDetailByRuleDTO.setVariables(jsonObject);
        pushMarketingUserDetailByRuleDTO.setStrategyCode("");
        //去重参数设置
        pushMarketingUserDetailByRuleDTO.setSoleField(SoleFieldEnum.CUST_NUM_STATUS_SOLE.getValue());
        pushMarketingUserDetailByRuleDTO.setStatus(transfer.getType());
        pushMarketingUserDetailByRuleDTO.setSoleType(30);
        pushMarketingUserDetailByRuleDTO.setPushApiCode(marketingCommonConfig.getYiXinToPolicyApiCode());
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

    private String getBatchNumber(String type) {
        String batchNumber = "";
        switch (type) {
            case "13":
                batchNumber = "rg1_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                break;
            case "15":
            case "23":
            case "6":
                batchNumber = "rg3_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                break;
            case "8":
            case "20":
            case "21":
                batchNumber = "rg5_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                break;
            default:
                batchNumber = "";
        }
        return batchNumber;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        YiXinRuleCollectDataImpl.YiXinRuleNecessaryData ruleNecessaryData =
                (YiXinRuleCollectDataImpl.YiXinRuleNecessaryData) context.getRuleNecessaryData();
        SyncUserValidityPeriodsBO syncUserValidityPeriodsBO = ruleNecessaryData.getCustomerMap().get(transfer.getCustNum());
        if (syncUserValidityPeriodsBO == null) {
            log.warn("宜信非实时推决策不在有效期内 --{} ", transfer.getCustNum());
            return false;
        }

        String reserveField1 = transfer.getReserveField1();
        if (!StringUtils.isEmpty(reserveField1)) {
            JSONObject json = JSON.parseObject(reserveField1);
            boolean transformType = "1".equals(json.getString("transformType"));
            if (transformType) {
                return Boolean.FALSE;
            }
            //过滤type=13，registerChannel！=1的数据
            if ("13".equals(transfer.getType())) {
                boolean registerChannel = !"1".equals(json.getString("registerChannel"));
                if (registerChannel) {
                    return Boolean.FALSE;
                }
            }
        }
        if (!context.getMqFact().getSource().equals(TransferSource.TRANSFER_DATA_SET_PROCESS.getCode())) {
            return Boolean.FALSE;
        }
        Map<String, String> blackList = ruleNecessaryData.getBlackList();
        boolean notBlack = true;
        if (!CollectionUtils.isEmpty(blackList)){
            notBlack = "N".equals(blackList.get(transfer.getId().toString()));
        }
        if (!notBlack){
            log.warn("宜信实时推决策id:{} cust_num:{}不满足黑名单条件", transfer.getId(), transfer.getCustNum());
            return false;
        }
        String tCid = transfer.gettCid();
        String apiCode = transfer.getApiCode();
        Set<String> custNums = Sets.newHashSet(transfer.getCustNum());
        List caseEffectiveCust = transferSyncUserMapper.getByInCustAndCaseEffective(tCid, apiCode, custNums);
        if (!CollectionUtils.isEmpty(caseEffectiveCust)) {
            log.warn("id:{} cust_num:{}caseEffetive=0 剔除", transfer.getId(), transfer.getCustNum());
            return false;
        }
        return Boolean.TRUE;
    }

    @Override
    public String label() {
        return "YiXin_NonRealTime_Policy";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.INIT_TO_POLICY_SOLE.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return RuleDataCollectionEnum.YI_XIN_DATA_COLLECTION.getCode();
    }
}
