package com.br.marketing.rule.yixin;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.client.dassservice.input.DassImportDataDTO;
import com.br.marketing.client.dassservice.input.userdata.BatchRealTimeUserDataDTO;
import com.br.marketing.common.utils.AESUtil;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.impl.YiXinRuleCollectDataImpl;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.PhoneSaleExtendInfo;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.service.ZnkfPushService;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
public class YiXinArtificialRealTimeDataImpl implements AssembleData<BatchRealTimeUserDataDTO> {

    @Value("${api.dass.aesKey:00}")
    private String aesKey;

    @Resource
    private ZnkfPushService znkfPushService;

    @Resource
    MarketingTransferSyncUserMapper transferSyncUserMapper;

    private final static String CUSTOMER_NUMBER_IS_FIRST = "customer:realtime:first";

    @Override
    public BatchRealTimeUserDataDTO assemble(Object transmitFact, ProcessHandlerContext context) {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        YiXinRuleCollectDataImpl.YiXinRuleNecessaryData ruleNecessaryData =
                (YiXinRuleCollectDataImpl.YiXinRuleNecessaryData) context.getRuleNecessaryData();
        Map<String, MarketingSyncUser> customerMap = ruleNecessaryData.getCustomerMap();
        MarketingSyncUser marketingSyncUser = getSyncUser(customerMap, transfer.getCustNum());
        if (marketingSyncUser == null) {
            return null;
        }
        BatchRealTimeUserDataDTO batchRealTimeUserDataDTO = new BatchRealTimeUserDataDTO();
        batchRealTimeUserDataDTO.setDassImportDataDTO(packageDassImportData(transfer, marketingSyncUser));
        batchRealTimeUserDataDTO.setPhoneSaleExtendInfo(packagePhoneSaleExtendInfo(transfer, marketingSyncUser));
        return batchRealTimeUserDataDTO;

    }

    private PhoneSaleExtendInfo packagePhoneSaleExtendInfo(MarketingTransferSyncUser transfer, MarketingSyncUser marketingSyncUser) {
        JSONObject reserveField = JSON.parseObject(transfer.getReserveField1());
        Integer liveType = reserveField.getInteger("liveType");
        PhoneSaleExtendInfo phoneSaleExtendInfo = new PhoneSaleExtendInfo();
        LocalDateTime localDateTime = transfer.getCreateTime().toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDateTime();
        LocalDate localDate = localDateTime.toLocalDate();


        phoneSaleExtendInfo.setApiCode(transfer.getApiCode());
        phoneSaleExtendInfo.setCustNum(transfer.getCustNum());
        phoneSaleExtendInfo.setTaskId(marketingSyncUser.getCusBatch());
        phoneSaleExtendInfo.setUserType(transfer.getUserType());
        phoneSaleExtendInfo.setAppletDate(localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        phoneSaleExtendInfo.setAppletTime(localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        phoneSaleExtendInfo.setPStatus(1);
        phoneSaleExtendInfo.setCreateTime(new Date());
        phoneSaleExtendInfo.setType(String.valueOf(liveType));
        phoneSaleExtendInfo.setDxType(String.format("%03d", liveType));
        phoneSaleExtendInfo.setPushDxTime(new Date());
        phoneSaleExtendInfo.setTransformType("1");
        phoneSaleExtendInfo.setSourceId(transfer.getId());
        phoneSaleExtendInfo.setCell(marketingSyncUser.getCell());

        return phoneSaleExtendInfo;
    }

    private DassImportDataDTO packageDassImportData(MarketingTransferSyncUser transfer, MarketingSyncUser syncUser) {
        JSONObject json = JSON.parseObject(transfer.getReserveField1());
        Integer liveType = json.getInteger("liveType");

        DassImportDataDTO batchImportData = new DassImportDataDTO();

        batchImportData.setId(transfer.getId());

        // 根据custNum取上传接口最新的gender（0女1男）传男女
        String reserveField1 = syncUser.getReserveField1();
        if (StringUtils.hasText(reserveField1)){
            JSONObject jsonObject = JSON.parseObject(reserveField1);
            String gender = jsonObject.getString("gender");
            if ("0".equals(gender)){
                batchImportData.setGender("女");
            }else if ("1".equals(gender)){
                batchImportData.setGender("男");
            }
        }
        String cell = BrCipherMaker.getInstance().decode(syncUser.getCell());
        String phone = AESUtil.aesEncrypty(cell, aesKey);
        String decodeName;
        String name = StringUtils.hasText(syncUser.getName()) ?
                (syncUser.getName().equals(decodeName = BrCipherMaker.getInstance().decode(syncUser.getName())) ? "1"
                        : decodeName) : "";
        // 根据custNum取上传接口最新的name转成明文传输
        batchImportData.setName(name);
        batchImportData.setOrgname("yixin");
        // 根据custNum取上传接口最新的cell转aes加密
        batchImportData.setPhone(phone);
        batchImportData.setUid(transfer.getCustNum());
        batchImportData.setUserType("A");
        batchImportData.setSource("16");
        batchImportData.setType(String.format("%03d", liveType));
        batchImportData.setAuditAmount(transfer.getAuditAmount());

        // 根据custNum取转化接口的rate rate=1 -> activity=2 rate=2 -> activity=4
        String rate = json.getString("rate");
        if ("1".equals(rate)){
            batchImportData.setActivity("2");
        } else if ("2".equals(rate)){
            batchImportData.setActivity("4");
        }
        // 拨打优先级 无静置为1 静置为2
        if (Arrays.asList(1,2,3).contains(liveType)){
            batchImportData.setPrioritySymbol("1");
        } else if (Arrays.asList(4,6,8).contains(liveType)){
            batchImportData.setPrioritySymbol("2");
        }
        return batchImportData;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
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
            Map<String, MarketingSyncUser> customerMap = ruleNecessaryData.getCustomerMap();
            MarketingSyncUser marketingSyncUser = customerMap.get(transfer.getCustNum());
            boolean messageDelay = isDelay != null && isDelay == 1 ;
            if (marketingSyncUser == null) {
                log.warn("上传表记录不存在 --{} ", transfer.getCustNum());
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
            String tCid = transfer.gettCid();
            String apiCode = transfer.getApiCode();
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
        return InterfaceHandlerEnum.ARTIFICIAL_BATCH_REALTIME_DATA.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return RuleDataCollectionEnum.YI_XIN_DATA_COLLECTION.getCode();
    }
}
