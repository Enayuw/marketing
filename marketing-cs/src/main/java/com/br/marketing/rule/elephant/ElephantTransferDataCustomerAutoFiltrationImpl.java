package com.br.marketing.rule.elephant;

import com.alibaba.fastjson.JSON;
import com.br.common.util.BrCipherMaker;
import com.br.common.util.DateUtils;
import com.br.marketing.bo.SyncUserValidityPeriodBO;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.impl.ElephantCollectDataImpl;
import com.br.marketing.context.impl.NiwodaiRuleCollectDataImpl;
import com.br.marketing.context.impl.RsCollectDataImpl;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.service.IPeriodOfValidityService;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import com.br.marketing.vo.TransferSyncUserToRobotAiVO;
import io.kubernetes.client.openapi.models.V1AggregationRule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;


/**
 * 小象转化数据自动过滤推客服
 *
 * @author GuangChao.Zhang
 * @version 1.0
 * @Date 2023/3/23 17:52
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class ElephantTransferDataCustomerAutoFiltrationImpl implements AssembleData<ConversionData> {


    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(
            DateHelper.LINE_DATE_COLON_TIME_FORMAT);


    @Override
    public ConversionData assemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        ConversionData conversionData = new ConversionData();
        conversionData.setDataId(transfer.getId().toString());
        conversionData.setCid(transfer.getCid());
        conversionData.setCaseNum(transfer.getCustNum());
        conversionData.setGroupType(transfer.getUserType());
        conversionData.setPartnerProcessDate(ObjectUtils.isEmpty(transfer.getCreateTime())
                ? LocalDateTime.now().format(DATE_TIME_FORMATTER) : DateUtils.format(transfer.getCreateTime()
                , DateHelper.LINE_DATE_COLON_TIME_FORMAT));
        ElephantCollectDataImpl.ElephantRuleNecessaryData data =
                (ElephantCollectDataImpl.ElephantRuleNecessaryData) context.getRuleNecessaryData();
        conversionData.setInversionStatus("0");
        Map<String, SyncUserValidityPeriodBO> syncUserValidityPeriodMap = data.getSyncUserValidityPeriodMap();
        SyncUserValidityPeriodBO bo = syncUserValidityPeriodMap.get(transfer.getCustNum());
        conversionData.setPhone(BrCipherMaker.getInstance().decode(bo.getSyncUser().getCell()));
        conversionData.setExpireDate(bo.getBuilder().addOfDayTimeStrString().builder().getEndOfDayTimeStr());
        TransferSyncUserToRobotAiVO vo = new TransferSyncUserToRobotAiVO();
        BeanUtils.copyProperties(transfer, vo);
        conversionData.setInversionInfo(JSON.toJSONString(vo));
        conversionData.setExpireBeginDate(bo.getBuilder().builder().getBeginDateStr());
        conversionData.setExpireEndDate(bo.getBuilder().builder().getEnDateStr());
        conversionData.setSoleType(-1);
        return conversionData;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        if (transmitFact instanceof MarketingTransferSyncUser) {
            MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
            ElephantCollectDataImpl.ElephantRuleNecessaryData ruleNecessaryData =
                    (ElephantCollectDataImpl.ElephantRuleNecessaryData) context.getRuleNecessaryData();
            if(ruleNecessaryData.getSyncUserValidityPeriodMap().get(transfer.getCustNum())!=null){
                SyncUserValidityPeriodBO syncUserValidityPeriodBO = ruleNecessaryData.getSyncUserValidityPeriodMap().get(transfer.getCustNum());
                MarketingSyncUser syncUser = syncUserValidityPeriodBO.getSyncUser();
                Date appletTime = syncUser.getAppletTime();
                String applyLoanTime = StringUtils.isNotEmpty(JSON.parseObject(transfer.getReserveField1()).getString("applyLoanTime"))?JSON.parseObject(transfer.getReserveField1()).getString("applyLoanTime"):"";
                // applyResult
                if(StringUtils.isNotEmpty(applyLoanTime)){
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date applyLoanTimeDate = sdf.parse(applyLoanTime);
                    if((applyLoanTimeDate.before(appletTime) || applyLoanTimeDate.equals(appletTime)) && ("0").equals(transfer.getApplyResult())){
                        return true;
                    }
                }
                if(("1").equals(transfer.getApplyResult()) && ("0").equals(transfer.getIfLent())){
                    return true;
                }
                if(StringUtils.isNotEmpty(transfer.getUnlentAmount()) && Double.valueOf(transfer.getUnlentAmount())<=0){
                    return true;
                }
            }
        }
        return false;
    }



    @Override
    public String label() {
        return "Elephant_TransferData_Customer_Auto_Filtration";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.CUSTOMER_TRANSFER_SOLE.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return RuleDataCollectionEnum.ELEPHANT_DATA_COLLECTION.getCode();
    }
}
