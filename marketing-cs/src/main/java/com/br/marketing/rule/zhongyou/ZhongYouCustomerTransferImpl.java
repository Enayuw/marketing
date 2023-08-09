package com.br.marketing.rule.zhongyou;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.common.util.DateUtils;
import com.br.marketing.bo.PeriodOfValidityBO;
import com.br.marketing.bo.SyncUserValidityPeriodBO;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.service.TransferDataValidityPeriodService;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import com.br.marketing.vo.TransferSyncUserToRobotAiVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Description ZhongYouCustomerTransferImpl
 * @Author hong.chen
 * @CreateTime 2023/08/07
 */
@Service
@Slf4j
public class ZhongYouCustomerTransferImpl implements AssembleData<ConversionData> {

    private static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DateHelper.LINE_DATE_COLON_TIME_FORMAT);
    @Resource
    private TransferDataValidityPeriodService transferDataValidityPeriodService;

    @Override
    public ConversionData assemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        log.warn("中邮推客服转化,apicode={}", transfer.getApiCode());
        ConversionData conversionData = new ConversionData();
        conversionData.setDataId(transfer.getId().toString());
        // 获取有效期失效日期
        List<MarketingTransferSyncUser> transferList = new ArrayList<>();
        transferList.add(transfer);
        Map<String, SyncUserValidityPeriodBO> periodBOMap =
                transferDataValidityPeriodService.getSyncUserValidityPeriodMap(transferList, transfer.getApiCode());
        SyncUserValidityPeriodBO bo = periodBOMap.get(transfer.getCustNum());
        PeriodOfValidityBO periodOfValidityBO = bo.getBuilder().addDateString().addOfDayTimeStrString().builder();
        // 有效期设置
        conversionData.setExpireDate(periodOfValidityBO.getEndOfDayTimeStr());
        conversionData.setPartnerProcessDate(ObjectUtils.isEmpty(transfer.getCreateTime())
                ? LocalDateTime.now().format(DATE_TIME_FORMATTER) : DateUtils.format(transfer.getCreateTime()
                , DateHelper.LINE_DATE_COLON_TIME_FORMAT));

        // phone
        if (bo.getSyncUser() != null) {
            conversionData.setPhone(BrCipherMaker.getInstance().decode(bo.getSyncUser().getCell()));
        } else {
            conversionData.setPhone("");
        }

        conversionData.setCid(transfer.getCid());
        conversionData.setInversionStatus("0");
        conversionData.setCaseNum(transfer.getCustNum());
        // inversionInfo(必填)
        TransferSyncUserToRobotAiVO vo = new TransferSyncUserToRobotAiVO();
        BeanUtils.copyProperties(transfer, vo);
        conversionData.setInversionInfo(JSON.toJSONString(vo));


        return conversionData;
    }

//    public static void main(String[] args) {
//        LocalDateTime parse = LocalDateTime.parse("2021-04-09 23:15:32", DATE_TIME_FORMATTER);
//        LocalDateTime parse1 = LocalDateTime.parse("2021-04-09 23:15:32", DATE_TIME_FORMATTER);
//        System.out.println(parse1.isAfter(parse));
//    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        log.warn("中邮推客服转化:isNeedAssemble");
        boolean flag = Boolean.FALSE;
        if (transmitFact instanceof MarketingTransferSyncUser) {
            MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
            String reserveField1 = transfer.getReserveField1();
            if (StringUtils.hasText(reserveField1)) {
                JSONObject json = JSON.parseObject(reserveField1);
                String applyLentTime = json.getString("applyLentTime");
                String pushTime = json.getString("pushTime");
                if (StringUtils.isEmpty(applyLentTime) || StringUtils.isEmpty(pushTime)) {
                    return false;
                }
                LocalDateTime date1 = LocalDateTime.parse(applyLentTime, DATE_TIME_FORMATTER);
                LocalDateTime date2 = LocalDateTime.parse(pushTime, DATE_TIME_FORMATTER);
                // 若applyLentTime小于pushTime，则不推送
                if (date1.isBefore(date2)) {
                    return false;
                }

                // 判断是否在有效期范围[t,t+45]
                MarketingSyncUser syncUser = transferDataValidityPeriodService.getNewValidityPeriodDataFirstVersion(transfer, null);
                if (syncUser == null) {
                    return false;
                }

                flag = Boolean.TRUE;
            }
        }
        return flag;
    }

    @Override
    public String label() {
        return "ZhongYou_TransferData_CustomerTransfer";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.CUSTOMER_TRANSFER.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return null;
    }
}
