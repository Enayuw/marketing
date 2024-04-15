package com.br.marketing.rule.weiedai;

import com.br.common.util.BrCipherMaker;
import com.br.marketing.bo.SyncUserValidityPeriodBO;
import com.br.marketing.client.dassservice.input.userdata.DassSingleImportAdapSoleDTO;
import com.br.marketing.client.dassservice.input.userdata.DassSingleImportDataDTO;
import com.br.marketing.client.dassservice.input.userdata.RealTimeUserDataSoleDTO;
import com.br.marketing.common.enums.DistributeSourceTypeEnum;
import com.br.marketing.common.enums.SoleFieldEnum;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.context.impl.WeieDaiCollectDataImpl;
import com.br.marketing.dto.customer.CallRecordBO;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.PhoneSaleExtendInfo;
import com.br.marketing.rpcclient.RpcClientProxy;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.service.PushDataService;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;

/**
 * 微e贷拨打明细转人工
 *
 * @author senyang.zheng
 * @date 2023/09/21
 */
@Service
public class WeieDaiCallRecordToDass implements AssembleData<RealTimeUserDataSoleDTO> {

    @Resource
    private PushDataService pushDataService;

    @Override
    public RealTimeUserDataSoleDTO assemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        CallRecordBO dto = (CallRecordBO) transmitFact;
        WeieDaiCollectDataImpl.WeieDaiNecessaryData ruleNecessaryData = (WeieDaiCollectDataImpl.WeieDaiNecessaryData) context.getRuleNecessaryData();
        Map<String, SyncUserValidityPeriodBO> syncUserPeriodMap = ruleNecessaryData.getSyncUserPeriodMap();
        SyncUserValidityPeriodBO syncUserData = syncUserPeriodMap.get(dto.getCaseNum());
        return buildRealTimeUserDataSoleDTO(dto, syncUserData);
    }

    /**
     * 封装实时推送接口参数
     *
     * @param dto          拨打明细数据
     * @param syncUserData 有效期内最新上传数据
     * @return {@link RealTimeUserDataSoleDTO }
     * @author senyang.zheng
     * @date 2023/09/21
     */
    private RealTimeUserDataSoleDTO buildRealTimeUserDataSoleDTO(CallRecordBO dto, SyncUserValidityPeriodBO syncUserData) {
        MarketingSyncUser syncUser = syncUserData.getSyncUser();
        RealTimeUserDataSoleDTO realTimeUserDataSoleDTO = new RealTimeUserDataSoleDTO();
        String phone = RpcClientProxy.decode(dto.getCaseNum(), "cell", "md5", "");
        String cusBatch = syncUser.getCusBatch();
        realTimeUserDataSoleDTO.setDassSingleImportAdapDTO(buildDassSingleImportAdapDTO(phone, syncUser));
        realTimeUserDataSoleDTO.setPhoneSaleExtendInfo(buildPhoneSaleExtendInfo(dto, phone, cusBatch));
        //去重逻辑 单一cell，T日仅推送一次
        realTimeUserDataSoleDTO.setSoleType(1);
        realTimeUserDataSoleDTO.setSoleField(SoleFieldEnum.CELL_SOLE.getValue());

        realTimeUserDataSoleDTO.setDistributeSourceTypeEnum(DistributeSourceTypeEnum.CALL_RECORD);
        return realTimeUserDataSoleDTO;
    }

    /**
     * 封装电销扩展信息
     *
     * @param dto      拨打明细数据
     * @param phone    电话明文
     * @param cusBatch 批次号
     * @return {@link PhoneSaleExtendInfo }
     * @author senyang.zheng
     * @date 2023/09/25
     */
    private PhoneSaleExtendInfo buildPhoneSaleExtendInfo(CallRecordBO dto, String phone, String cusBatch) {
        PhoneSaleExtendInfo phoneSaleExtendInfo = new PhoneSaleExtendInfo();
        phoneSaleExtendInfo.setApiCode(dto.getApiCode());
        phoneSaleExtendInfo.setCustNum(dto.getCaseNum());
        phoneSaleExtendInfo.setCell(BrCipherMaker.getInstance().encode(phone));
        phoneSaleExtendInfo.setTaskId(cusBatch);
        phoneSaleExtendInfo.setAppletDate(dto.getCreateTime().toInstant().atZone(ZoneId.systemDefault())
            .toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
        phoneSaleExtendInfo.setAppletTime(dto.getCreateTime().toInstant().atZone(ZoneId.systemDefault())
            .toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        phoneSaleExtendInfo.setStatus("1");//情景1
        phoneSaleExtendInfo.setCreateTime(new Date());
        phoneSaleExtendInfo.setSourceId(dto.getId());
        phoneSaleExtendInfo.setPushDxTime(new Date());
        phoneSaleExtendInfo.setDxUserType("2");
        return phoneSaleExtendInfo;
    }


    /**
     * 封装推送人工单条接口参数
     *
     * @param phone    电话明文
     * @param syncUser 上传原始数据
     * @return {@link DassSingleImportAdapSoleDTO }
     * @author senyang.zheng
     * @date 2023/09/25
     */
    private DassSingleImportAdapSoleDTO buildDassSingleImportAdapDTO(String phone, MarketingSyncUser syncUser) {
        DassSingleImportAdapSoleDTO dassSingleImportAdapSoleDTO = new DassSingleImportAdapSoleDTO();
        DassSingleImportDataDTO dassSingleImportDataDTO = new DassSingleImportDataDTO();
        dassSingleImportDataDTO.setName("1");
        dassSingleImportDataDTO.setOrgname("weiedai");
        dassSingleImportDataDTO.setPhone(phone);
        dassSingleImportDataDTO.setUid(syncUser.getCustNum());
        dassSingleImportDataDTO.setUserType("2");
        dassSingleImportDataDTO.setSource("35");
        dassSingleImportAdapSoleDTO.setDassSingleImportDataDTO(dassSingleImportDataDTO);
        return dassSingleImportAdapSoleDTO;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        if (transmitFact instanceof CallRecordBO) {
            CallRecordBO bo = (CallRecordBO) transmitFact;
            String intentionGrade = bo.getDetail().getIntentionGrade();
            //判断intentionGrade意向等级
            Boolean gradeOK = pushDataService.isPushDassWithCallGrade(this.label(), intentionGrade);
            if (!gradeOK) {
                return false;
            }
            //判断有效期
            WeieDaiCollectDataImpl.WeieDaiNecessaryData ruleNecessaryData = (WeieDaiCollectDataImpl.WeieDaiNecessaryData) context.getRuleNecessaryData();
            Map<String, SyncUserValidityPeriodBO> syncUserPeriodMap = ruleNecessaryData.getSyncUserPeriodMap();
            SyncUserValidityPeriodBO syncUserData = syncUserPeriodMap.get(bo.getCaseNum());
            return syncUserData != null;
        }
        return false;
    }

    @Override
    public String label() {
        return "WeieDai_CallRecordData_PushDaas";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.ARTIFICIAL_REAL_TIME_USERDATA_SOLE.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return RuleDataCollectionEnum.WEIEDAI_DATA_COLLECTION.getCode();
    }
}
