package com.br.marketing.rule.yixin;

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
import com.br.marketing.service.TransferDataValidityPeriodService;
import com.br.marketing.service.ZnkfPushService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * code is far away from bug with the animal protecting
 * ┏┓　　　┏┓
 * ┏┛┻━━━┛┻┓
 * ┃　　　　　　　┃
 * ┃　　　━　　　┃
 * ┃　┳┛　┗┳　┃
 * ┃　　　　　　　┃
 * ┃　　　┻　　　┃
 * ┃　　　　　　　┃
 * ┗━┓　　　┏━┛
 * 　　┃　　　┃神兽保佑
 * 　　┃　　　┃代码无BUG！
 * 　　┃　　　┗━━━┓
 * 　　┃　　　　　　　┣┓
 * 　　┃　　　　　　　┏┛
 * 　　┗┓┓┏━┳┓┏┛
 * 　　　┃┫┫　┃┫┫
 * 　　　┗┻┛　┗┻┛
 *
 * @Description : 宜信实时数据转吊销
 * ---------------------------------
 * @Author : jilong.xu
 * @Date : Create in 2022/3/28 15:29
 */
@Service
@Slf4j
public class YiXinArtificialRealTimeDataImpl implements AssembleData<PushMarketingUserDetailByRuleDTO> {

    @Value("${api.dass.aesKey:00}")
    private String aesKey;

    @Resource
    private ZnkfPushService znkfPushService;

    @Resource
    MarketingTransferSyncUserMapper transferSyncUserMapper;

    private final static String CUSTOMER_NUMBER_IS_FIRST = "customer:realtime:first";

    @Resource
    private TransferDataValidityPeriodService transferDataValidityPeriodService;

    @Autowired
    private PushRuleService pushRuleService;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Override
    public PushMarketingUserDetailByRuleDTO assemble(Object transmitFact, ProcessHandlerContext context) {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        HashMap<String, Integer> pushCellEncPolicy = marketingCommonConfig.getPushCellEncPolicy();
        Integer encType = ScoreThreeKeyEncryptEnum.md5.getValue();
        if (pushCellEncPolicy != null && pushCellEncPolicy.get(context.getApiCode()) != null) {
            encType = pushCellEncPolicy.get(context.getApiCode());
        }
        YiXinRuleCollectDataImpl.YiXinRuleNecessaryData ruleNecessaryData =
                (YiXinRuleCollectDataImpl.YiXinRuleNecessaryData) context.getRuleNecessaryData();
        SyncUserValidityPeriodsBO syncUserValidityPeriodsBO = ruleNecessaryData.getCustomerMap().get(transfer.getCustNum());
        MarketingSyncUser marketingSyncUser = syncUserValidityPeriodsBO.getSyncUsers().get(0);
        if (marketingSyncUser == null) {
            return null;
        }
        PushMarketingUserDetailByRuleDTO pushMarketingUserDetailByRuleDTO = new PushMarketingUserDetailByRuleDTO();

        pushMarketingUserDetailByRuleDTO.setInitId(transfer.getId());
        pushMarketingUserDetailByRuleDTO.setCaseNumber(transfer.getCustNum());


        PushMarketingUserDetailDTO marketingUserDetailDTO = new PushMarketingUserDetailDTO();
        marketingUserDetailDTO.setCaseNumber(transfer.getCustNum());
        Map<String, SyncUserValidityPeriodsBO> boMap = transferDataValidityPeriodService
                .getValidityPeriodsByCustNumAndUserType(Collections.singleton(transfer.getCustNum())
                        , transfer.getUserType(), transfer.getApiCode(), new Date());
        String cell = boMap.get(transfer.getCustNum()).getSyncUsers().get(0).getCell();
        cell = pushRuleService.encrypt3k(encType, BrCipherMaker.getInstance().decode(cell));
        pushMarketingUserDetailByRuleDTO.setPhone(cell);

        String type = transfer.getType();
        if ("1".equals(type) || "2".equals(type)){
            pushMarketingUserDetailByRuleDTO.setBatchNumber("rg8_" +LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        } else if ("3".equals(type) || "8".equals(type)){
            pushMarketingUserDetailByRuleDTO.setBatchNumber("rg9_" +LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        } else {
            log.warn("宜信实时促申或促提batchNumber字段非(1、2、3、8 )", type);
        }
        JSONObject parseObject = JSON.parseObject(transfer.getReserveField1());
        pushMarketingUserDetailByRuleDTO.setVariables(parseObject);
        return pushMarketingUserDetailByRuleDTO;

    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        String tCid = transfer.gettCid();
        String apiCode = transfer.getApiCode();
        String reserveField1 = transfer.getReserveField1();
        if (StringUtils.hasText(reserveField1)){
            YiXinRuleCollectDataImpl.YiXinRuleNecessaryData ruleNecessaryData =
                    (YiXinRuleCollectDataImpl.YiXinRuleNecessaryData) context.getRuleNecessaryData();
            JSONObject json = JSON.parseObject(reserveField1);
            boolean transformType = "1".equals(json.getString("transformType"));
            Integer liveType = json.getInteger("liveType");
            String key = CUSTOMER_NUMBER_IS_FIRST.concat(":").concat(transfer.getCustNum());
            Map<String, String> blackList = ruleNecessaryData.getBlackList();
            boolean notBlack = true;
            if (!CollectionUtils.isEmpty(blackList)){
                notBlack = "N".equals(blackList.get(transfer.getId().toString()));
            }
            Integer isDelay = context.getMqFact().getIsDelay();
            SyncUserValidityPeriodsBO syncUserValidityPeriodsBO = ruleNecessaryData.getCustomerMap().get(transfer.getCustNum());
            MarketingSyncUser marketingSyncUser =syncUserValidityPeriodsBO.getSyncUsers().get(0);
            boolean messageDelay = isDelay != null && isDelay == 1 ;
            if (marketingSyncUser == null) {
                log.warn("上传表记录不在有效期内 --{} ", transfer.getCustNum());
                return false;
            }else{
                String decode = BrCipherMaker.getInstance().decode(marketingSyncUser.getCell());
                if (StringUtils.isEmpty(decode)){
                    log.warn("手机号解密失败 --{} ", transfer.getCustNum());
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
            if (!notBlack){
                log.warn("id:{} cust_num:{}不满足黑名单条件", transfer.getId(), transfer.getCustNum());
                return false;
            }
            boolean flag = transformType && (Arrays.asList(1,2,3).contains(liveType) || messageDelay);
            if (!flag){
                log.warn("id:{} cust_num:{}不满足立即推送条件", transfer.getId(), transfer.getCustNum());
                return false;
            }
            if (!znkfPushService.cusNumIsFirstToday(key)){
                log.warn("id:{} cust_num:{}不满足当天推送条件", transfer.getId(), transfer.getCustNum());
                return false;
            }
            Set<String> custNums = Sets.newHashSet(transfer.getCustNum());
            List caseEffectiveCust = transferSyncUserMapper.getByInCustAndCaseEffective(tCid, apiCode,custNums);
            if (!CollectionUtils.isEmpty(caseEffectiveCust)) {
                log.warn("id:{} cust_num:{}caseEffetive=0 剔除", transfer.getId(), transfer.getCustNum());
                return false;
            }

            return  true;
        }
        return false;
    }

    @Override
    public String label() {
        return "YiXin_RealTimeData_ArtificialBatchRealTimeData";
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
