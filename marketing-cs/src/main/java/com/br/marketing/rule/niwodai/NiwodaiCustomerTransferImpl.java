package com.br.marketing.rule.niwodai;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.common.util.DateUtils;
import com.br.marketing.bo.PeriodOfValidityBO;
import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.common.enums.SoleFieldEnum;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.impl.NiwodaiRuleCollectDataImpl;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import com.br.marketing.vo.TransferSyncUserToRobotAiVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * D20230314你我贷自动化过滤-3710064 业务
 * http://c.100credit.cn/pages/viewpage.action?pageId=103562399
 *
 * @author Guo Zeqiang
 * @dateTime 2023/3/23 9:10
 */
@Service
@Slf4j
public class NiwodaiCustomerTransferImpl implements AssembleData<ConversionData> {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(
            DateHelper.LINE_DATE_COLON_TIME_FORMAT);
    private final static Map<String, String> TAG_MAP = new ConcurrentHashMap<>();

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    static {
        TAG_MAP.put("F", "0");
        TAG_MAP.put("B", "0");
        TAG_MAP.put("C", "0");
        TAG_MAP.put("H", "2");
    }

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
                //1.有效期判断
                NiwodaiRuleCollectDataImpl.NiwodaiRuleNecessaryData data =
                        (NiwodaiRuleCollectDataImpl.NiwodaiRuleNecessaryData) context.getRuleNecessaryData();
                if (data.getSyncUserValidityPeriodMap().get(transfer.getCustNum()) == null) {
                    return false;
                }
                //2.规则过滤并赋值inversionStatus
                String inversionStatus = getInversionStatus(context.getApiCode(), transfer.getUserType(), jsonObject);
                if (StringUtils.isEmpty(inversionStatus)) {
                    return false;
                }
                data.setInversionStatus(inversionStatus);
                return true;
            }
        }
        return false;
    }

    /**
     * 获得inversionStatus
     * @param apiCode
     * @param userType
     * @param reserveField1
     */
    private String getInversionStatus(String apiCode, String userType, JSONObject reserveField1) {
        String inversionStatus = "";
        Map<String, JSONArray> youMeLoanTransferFilterConfig = marketingCommonConfig.getYouMeLoanTransferFilterConfig();
        JSONArray config = youMeLoanTransferFilterConfig.get(apiCode);
        if (config != null) {
            List<Map> congfigList = config.toJavaList(Map.class);
            tagGroup:
            for (Map map : congfigList) {
                //1.tag匹配
                List<Map<String, String>> tagList = (List<Map<String, String>>) map.get("tag");
                for (Map<String, String> tag : tagList) {
                    if (!tag.get("tagValue").equals(reserveField1.getString(tag.get("tagKey")))) {
                        continue tagGroup;
                    }
                }
                //2.userType匹配
                String userTypeString = (String) map.get("userType");
                if (StringUtils.isNotEmpty(userTypeString)) {
                    List<String> userTypes = Arrays.stream(userTypeString.split(",")).collect(Collectors.toList());
                    if (!userTypes.contains(userType)) {
                        continue;
                    }
                }
                //3.匹配通过后，赋值inversionStatus
                inversionStatus = (String) map.get("inversionStatus");
            }
        }
        return inversionStatus;
    }

    @Override
    public String label() {
        return "niwodai_TransferData_CustomerTransfer";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.CUSTOMER_TRANSFER_SOLE.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return RuleDataCollectionEnum.NIWODAI_DATA_COLLECTION.getCode();
    }
}
