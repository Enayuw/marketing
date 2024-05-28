package com.br.marketing.rule.yishi;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.common.util.DateUtils;
import com.br.marketing.bo.PeriodOfValidityBO;
import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.common.enums.SoleFieldEnum;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.impl.NiwodaiRuleCollectDataImpl;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import com.br.marketing.vo.TransferSyncUserToRobotAiVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * D20240522医时自动化过滤-3710136
 * https://c.100credit.cn/pages/viewpage.action?pageId=160811114
 *
 * @author guangxiu.li
 * @dateTime 2024/05/24 9:10
 */
@Service
@Slf4j
public class YiShiTransferAutomaticFiltrationImpl implements AssembleData<ConversionData> {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(
            DateHelper.LINE_DATE_COLON_TIME_FORMAT);
    private final static Map<String, String> TAG_MAP = new ConcurrentHashMap<>();


    @Override
    public ConversionData assemble(Object transmitFact, ProcessHandlerContext context) {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        ConversionData conversionData = new ConversionData();
        conversionData.setDataId(transfer.getId().toString());
        conversionData.setCid(transfer.getCid());
        conversionData.setCaseNum(transfer.getCustNum());
        conversionData.setGroupType(transfer.getUserType());
        conversionData.setPartnerProcessDate(ObjectUtils.isEmpty(transfer.getCreateTime())
                ? LocalDateTime.now().format(DATE_TIME_FORMATTER) : DateUtils.format(transfer.getCreateTime()
                , DateHelper.LINE_DATE_COLON_TIME_FORMAT));
        NiwodaiRuleCollectDataImpl.NiwodaiRuleNecessaryData data =
                (NiwodaiRuleCollectDataImpl.NiwodaiRuleNecessaryData) context.getRuleNecessaryData();
        conversionData.setInversionStatus(data.getInversionStatus());
        Map<String, SyncUserValidityPeriodsBO> syncUserValidityPeriodMap = data.getSyncUserValidityPeriodMap();
        SyncUserValidityPeriodsBO bo = syncUserValidityPeriodMap.get(transfer.getCustNum());
        MarketingSyncUser marketingSyncUser = bo.getSyncUsers().get(0);
        PeriodOfValidityBO.Builder builder = bo.getBuilders().get(0);
        conversionData.setPhone(BrCipherMaker.getInstance().decode(marketingSyncUser.getCell()));
        PeriodOfValidityBO periodOfValidityBO = builder.addDateString().addOfDayTimeStrString().builder();
        // 有效期设置
        conversionData.setExpireDate(periodOfValidityBO.getEndOfDayTimeStr());
        TransferSyncUserToRobotAiVO vo = new TransferSyncUserToRobotAiVO();
        BeanUtils.copyProperties(transfer, vo);
        conversionData.setInversionInfo(JSON.toJSONString(vo));
        // 去重参数设置
        conversionData.setInitId(transfer.getId());
        conversionData.setSoleField(SoleFieldEnum.CELL_SOLE.getValue());
        conversionData.setSoleType(-1);
        conversionData.setExpireBeginDate(periodOfValidityBO.getBeginDateStr());
        conversionData.setExpireEndDate(periodOfValidityBO.getEnDateStr());
        return conversionData;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws IllegalAccessException {
        if (transmitFact instanceof MarketingTransferSyncUser) {
            MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
            String reserveField1 = transfer.getReserveField1();
            if (StringUtils.isNotBlank(reserveField1)) {
                JSONObject jsonObject;
                try {
                    jsonObject = JSONObject.parseObject(reserveField1);
                } catch (Exception e) {
                    log.warn(e.getMessage(), e);
                    return false;
                }
                Set<Map.Entry<String, String>> entries = TAG_MAP.entrySet();
                // 遍历所有标记
                for (Map.Entry<String, String> entry : entries) {
                    // 检查指定标记中值中满足1的值
                    if ("1".equals(jsonObject.getString(entry.getKey()))) {
                        NiwodaiRuleCollectDataImpl.NiwodaiRuleNecessaryData data =
                                (NiwodaiRuleCollectDataImpl.NiwodaiRuleNecessaryData) context.getRuleNecessaryData();
                        // 检查有效期配置，非空时满足有效期
                        if (data.getSyncUserValidityPeriodMap().get(transfer.getCustNum()) != null) {
                            data.setInversionStatus(entry.getValue());
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public String label() {
        return "YiShi_TransferData_Customer_Auto_Filtration";
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
