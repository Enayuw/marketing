package com.br.marketing.rule.yixin;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.client.dassservice.input.userdata.*;
import com.br.marketing.common.utils.AESUtil;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.impl.YiXinRuleCollectDataImpl;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.PhoneSaleExtendInfo;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.service.ZnkfPushService;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
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
 * @Description : 宜信实时数据转电销静置数据特殊处理
 * ---------------------------------
 * @Author : jilong.xu
 * @Date : Create in 2022/3/28 15:29
 */
@Service
public class YiXinArtificialRealTimeSingleDataImpl implements AssembleData<RealTimeUserDataDTO> {

    @Value("${api.dass.aesKey:00}")
    private String aesKey;

    @Resource
    private ZnkfPushService znkfPushService;

    private final static String CUSTOMER_NUMBER_IS_FIRST = "customer:realtime:first";

    @Override
    public RealTimeUserDataDTO assemble(Object transmitFact, ProcessHandlerContext context) {
        RealTimeUserDataDTO realTimeUserDataDTO = new RealTimeUserDataDTO();
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        YiXinRuleCollectDataImpl.YiXinRuleNecessaryData ruleNecessaryData =
                (YiXinRuleCollectDataImpl.YiXinRuleNecessaryData) context.getRuleNecessaryData();
        Map<String, MarketingSyncUser> customerMap = ruleNecessaryData.getCustomerMap();
        MarketingSyncUser marketingSyncUser = customerMap.get(transfer.getCustNum());
        if (!StringUtils.isEmpty(marketingSyncUser)){
            DassSingleImportAdapDTO dassSingleImportAdapDTO = new DassSingleImportAdapDTO();
            dassSingleImportAdapDTO.setDassSingleImportDataDTO(packageDassImportData(transfer,marketingSyncUser));

            realTimeUserDataDTO.setDassSingleImportAdapDTO(dassSingleImportAdapDTO);
            realTimeUserDataDTO.setPhoneSaleExtendInfo(packagePhoneSaleExtendInfo(transfer,marketingSyncUser));
        }
        return realTimeUserDataDTO;
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

        return phoneSaleExtendInfo;
    }

    private DassSingleImportDataDTO packageDassImportData(MarketingTransferSyncUser transfer, MarketingSyncUser syncUser) {
        JSONObject json = JSON.parseObject(transfer.getReserveField1());
        Integer liveType = json.getInteger("liveType");

        DassSingleImportDataDTO dassSingleImportDataDTO = new DassSingleImportDataDTO();

        dassSingleImportDataDTO.setId(transfer.getId());

        // 根据custNum取上传接口最新的gender（0女1男）传男女
        String reserveField1 = syncUser.getReserveField1();
        if (StringUtils.hasText(reserveField1)){
            JSONObject jsonObject = JSON.parseObject(reserveField1);
            String gender = jsonObject.getString("gender");
            if ("0".equals(gender)){
                dassSingleImportDataDTO.setGender("女");
            }else if ("1".equals(gender)){
                dassSingleImportDataDTO.setGender("男");
            }
        }
        String cell = BrCipherMaker.getInstance().decode(syncUser.getCell());
        String phone = AESUtil.aesEncrypty(cell, aesKey);
        String name = StringUtils.hasText(syncUser.getName()) ?
                BrCipherMaker.getInstance().decode(syncUser.getName())
                : "";
        // 根据custNum取上传接口最新的name转成明文传输
        dassSingleImportDataDTO.setName(name);
        dassSingleImportDataDTO.setOrgname("yixin");
        // 根据custNum取上传接口最新的cell转aes加密
        dassSingleImportDataDTO.setPhone(phone);
        dassSingleImportDataDTO.setUid(transfer.getCustNum());
        dassSingleImportDataDTO.setUserType("6");
        dassSingleImportDataDTO.setType(String.format("%03d", liveType));
        dassSingleImportDataDTO.setAuditAmount(transfer.getAuditAmount());

        // 根据custNum取转化接口的rate rate=1 -> activity=2 rate=2 -> activity=4
        String rate = json.getString("rate");
        if ("1".equals(rate)){
            dassSingleImportDataDTO.setActivity("2");
        } else if ("2".equals(rate)){
            dassSingleImportDataDTO.setActivity("4");
        }
        // 拨打优先级 无静置为1 静置为2
        if (Arrays.asList(1,2,3).contains(liveType)){
            dassSingleImportDataDTO.setPrioritySymbol("1");
        } else if (Arrays.asList(4,6,8).contains(liveType)){
            dassSingleImportDataDTO.setPrioritySymbol("2");
        }
        return dassSingleImportDataDTO;
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
            String key = CUSTOMER_NUMBER_IS_FIRST.concat(":").concat(transfer.getUserType())
                    .concat(":").concat(transfer.getCustNum());
            Map<String, String> blackList = ruleNecessaryData.getBlackList();
            boolean notBlack = true;
            if (!CollectionUtils.isEmpty(blackList)){
                notBlack = "N".equals(blackList.get(transfer.getId().toString()));
            }
            Integer isDelay = context.getMqFact().getIsDelay();
            boolean flag = isDelay != null && isDelay == 1 ;
            /*
            满足条件立即推送
                1从延迟队列过来的消息
                2、不满足客服黑名单
                3、transformType 为1
                4、当天该案件编号未被推送
             */
            return flag && notBlack  && transformType && znkfPushService.cusNumIsFirstToday(key);
        }
        return false;
    }

    @Override
    public String label() {
        return "YiXin_RealTimeData_ArtificialSingleDelayRealTimeData";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.ARTIFICIAL_REAL_TIME_USERDATA.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return RuleDataCollectionEnum.YI_XIN_DATA_COLLECTION.getCode();
    }
}
