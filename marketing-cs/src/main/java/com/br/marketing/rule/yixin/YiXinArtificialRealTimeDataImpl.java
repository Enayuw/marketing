package com.br.marketing.rule.yixin;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.client.dassservice.input.userdata.BatchRealTimeUserDataDTO;
import com.br.marketing.client.dassservice.input.userdata.DassBatchImportDataDTO;
import com.br.marketing.common.utils.AESUtil;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.impl.YiXinRuleCollectDataImpl;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.PhoneSaleExtendInfo;
import com.br.marketing.origin.MqFact;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.service.ZnkfPushService;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

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
public class YiXinArtificialRealTimeDataImpl implements AssembleData<BatchRealTimeUserDataDTO> {

    @Value("${api.dass.aesKey:00}")
    private String aesKey;

    @Resource
    private ZnkfPushService znkfPushService;

    private final static String CUSTOMER_NUMBER_IS_FIRST = "customer:realtime:first";

    @Override
    public BatchRealTimeUserDataDTO assemble(Object transmitFact, ProcessHandlerContext context) {
        BatchRealTimeUserDataDTO batchRealTimeUserDataDTO = new BatchRealTimeUserDataDTO();
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        YiXinRuleCollectDataImpl.YiXinRuleNecessaryData ruleNecessaryData =
                (YiXinRuleCollectDataImpl.YiXinRuleNecessaryData) context.getRuleNecessaryData();
        Map<String, MarketingSyncUser> customerMap = ruleNecessaryData.getCustomerMap();
        MarketingSyncUser marketingSyncUser = customerMap.get(transfer.getCustNum());
        if (!StringUtils.isEmpty(marketingSyncUser)){
            batchRealTimeUserDataDTO.setDassImportDataDTO(packageDassImportData(transfer,marketingSyncUser));
            batchRealTimeUserDataDTO.setPhoneSaleExtendInfo(packagePhoneSaleExtendInfo(transfer));
        }
        return batchRealTimeUserDataDTO;
    }

    private PhoneSaleExtendInfo packagePhoneSaleExtendInfo(MarketingTransferSyncUser transfer) {
        JSONObject reserveField = JSON.parseObject(transfer.getReserveField1());
        Integer liveType = reserveField.getInteger("liveType");
        PhoneSaleExtendInfo phoneSaleExtendInfo = new PhoneSaleExtendInfo();
        LocalDateTime localDateTime = transfer.getCreateTime().toInstant()
                .atZone(ZoneId.systemDefault()).toLocalDateTime();
        LocalDate localDate = localDateTime.toLocalDate();


        phoneSaleExtendInfo.setApiCode(transfer.getApiCode());
        phoneSaleExtendInfo.setCustNum(transfer.getCustNum());
        String reserveField1 = transfer.getReserveField1();
        if (StringUtils.hasText(reserveField1)) {
            JSONObject json = JSON.parseObject(reserveField1);
            phoneSaleExtendInfo.setTaskId(json.getString("taskId"));
        }
        phoneSaleExtendInfo.setUserType(transfer.getUserType());
        phoneSaleExtendInfo.setAppletDate(localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        phoneSaleExtendInfo.setAppletTime(localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        phoneSaleExtendInfo.setStatus("a");
        phoneSaleExtendInfo.setPStatus(1);
        phoneSaleExtendInfo.setCreateTime(new Date());
        phoneSaleExtendInfo.setType(String.valueOf(liveType));
        phoneSaleExtendInfo.setDxType(String.format("%03d", liveType));
        phoneSaleExtendInfo.setPushDxTime(new Date());
        phoneSaleExtendInfo.setTransformType("1");
        phoneSaleExtendInfo.setSourceId(transfer.getId());

        return phoneSaleExtendInfo;
    }

    private DassBatchImportDataDTO packageDassImportData(MarketingTransferSyncUser transfer,MarketingSyncUser syncUser) {
        JSONObject json = JSON.parseObject(transfer.getReserveField1());
        Integer liveType = json.getInteger("liveType");

        DassBatchImportDataDTO batchImportData = new DassBatchImportDataDTO();

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
        String name = StringUtils.hasText(syncUser.getName()) ?
                BrCipherMaker.getInstance().decode(syncUser.getName())
                : "";
        // 根据custNum取上传接口最新的name转成明文传输
        batchImportData.setName(name);
        batchImportData.setOrgname("yixin");
        // 根据custNum取上传接口最新的cell转aes加密
        batchImportData.setPhone(phone);
        batchImportData.setUid(transfer.getCustNum());
        batchImportData.setUserType("6");
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
            MqFact mqFact = context.getMqFact();
            String key = CUSTOMER_NUMBER_IS_FIRST.concat(":").concat(transfer.getUserType())
                    .concat(":").concat(transfer.getCustNum());
            Map<String, String> blackList = ruleNecessaryData.getBlackList();
            boolean notBlack = "N".equals(blackList.get(transfer.getId().toString()));
            boolean isDelay = mqFact.getIsDelay() != null && 1 == mqFact.getIsDelay();
            /*
            满足条件立即推送
                1、不满足客服黑名单
                1、当天该案件编号未被推送
                2、transformType 为1
                3、立即推送liveType 1,2,3 或者 从延迟队列过来的消息
             */
            return notBlack && znkfPushService.cusNumIsFirstToday(key) && transformType
                    && (Arrays.asList(1,2,3).contains(liveType) || isDelay);
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
        return RuleDataCollectionEnum.YI_XIN_REALTIME_DATA_COLLECTION.getCode();
    }
}
