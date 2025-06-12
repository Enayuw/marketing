package com.br.marketing.rule.xiecheng;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.bo.SyncUserValidityPeriodBO;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.dto.XieChengDataDTO;
import com.br.marketing.dto.customer.CallRecordBO;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.XieChengData;
import com.br.marketing.entity.XieChengJudgeConvTypeValue;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.mapper.XieChengDataMapper;
import com.br.marketing.origin.MqFact;
import com.br.marketing.rpcclient.RpcClientProxy;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.service.TransferDataValidityPeriodService;
import com.br.marketing.service.XieChengJudgeConvTypeService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 通话明细推送携程(3710090/3710091)
 *
 * @author chenh
 * @dateTime 2023/09/15 16:50
 */
@Service
@Slf4j
public class XieChengCallRecordInsertDbvtImpl implements AssembleData<XieChengDataDTO> {
    @Resource
    private XieChengDataMapper xieChengDataMapper;
    @Resource
    private TransferDataValidityPeriodService transferDataValidityPeriodService;
    @Resource
    MarketingCommonConfig marketingCommonConfig;
    @Resource
    private XieChengJudgeConvTypeService xieChengJudgeConvTypeService;

    @Resource
    private TableCreateServiceImpl tableCreateService;

    @Resource
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;

    private List<Integer> callStatusFail = Arrays.asList(13, 15);

    private List<Integer> callStatusIsBlack = Arrays.asList(12);


    @Override
    public XieChengDataDTO assemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        CallRecordBO bo = (CallRecordBO) transmitFact;
        XieChengDataDTO xieChengDataDTO = new XieChengDataDTO();
        XieChengData xieChengData = new XieChengData();
        xieChengDataDTO.setXieChengData(xieChengData);
        xieChengData.setApiCode(bo.getApiCode());
        xieChengData.setActionType("IVR");
        xieChengDataDTO.setInitId(bo.getId());
        xieChengData.setSha256Tel(bo.getCaseNum());
        // 保存通话明细扩展字段
        xieChengData.setExtend(bo.getDetail().getUserProperties());
        // 来自延迟队列，且已判断过有106，进入携程队列(toDelay==true:要进延迟队列，toDelay==false:要进携程队列)
        MqFact mqFact = context.getMqFact();
        Integer isDelay = mqFact.getIsDelay();
        if (isDelay != null && isDelay == 1) {
            xieChengDataDTO.setToDelay(false);
        } else {
            // sha256解密，log加密
            String phone = RpcClientProxy.decode(bo.getCaseNum(), "cell", "sha", "");
            String encode = BrCipherMaker.getInstance().encode(phone);
            // 加解密失败，不推送
            if (StringUtils.isEmpty(encode)) {
                log.error("携程通话明细数据推送客户接口(3710090/3710091)，sha256解密log加密失败。custNum：{}，log加密后：{}", bo.getCaseNum(), encode);
                return null;
            }

            // 查询有效期使用的apiCode
            String mainApiCode = xieChengJudgeConvTypeService.getCondition(context.getApiCode()).getString("mainApiCode");
            Set<String> syncSet = new HashSet<>();
            syncSet.add(encode);
            Map<String, SyncUserValidityPeriodBO> syncUser =
                    transferDataValidityPeriodService.getValidityPeriodCellBatchFirstVersion(syncSet, mainApiCode, new Date());
            List<XieChengJudgeConvTypeValue> xieChengJudgeConvType = xieChengJudgeConvTypeService.getJudgeConvType(context.getApiCode(),
                    bo.getCaseNum());

            SyncUserValidityPeriodBO periodBO = syncUser.get(encode);

            // 该custNum不在有效期内，或用通话明细custNum没查到转化明细：进携程队列
            if (periodBO == null || CollectionUtils.isEmpty(xieChengJudgeConvType)) {
                log.warn("携程通话明细数据推送客户接口，该custNum不在有效期内，或根据custNum和有效期范围没查询到转化数据，该数据直接进入推送携程队列，custNum：{}", bo.getCaseNum());
                xieChengDataDTO.setToDelay(false);
                return xieChengDataDTO;
            }

            // 有110且没有106的进入延迟队列，否则进携程队列（1：没有110、2：有110且有106）
            // 有110
            XieChengJudgeConvTypeValue value = xieChengJudgeConvType.get(0);
            Boolean hasRiskControl = value.getHasRiskControl();
            // 有106
            Boolean hasApplySuccess = value.getHasApplySuccess();
            if (hasRiskControl && !hasApplySuccess) {
                xieChengDataDTO.setToDelay(true);
            } else {
                xieChengDataDTO.setToDelay(false);
            }
        }

        return xieChengDataDTO;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        MqFact mqFact = context.getMqFact();
        Integer isDelay = mqFact.getIsDelay();
        if (transmitFact instanceof CallRecordBO) {
            CallRecordBO bo = (CallRecordBO) transmitFact;
            if (bo.getDetail() != null && callStatusFail.contains(bo.getDetail().getCallStatus())) {
                keepRecord(bo, String.format("CallStatus状态是：%d", bo.getDetail().getCallStatus()));
                return false;
            }
            if (bo.getDetail() != null && callStatusIsBlack.contains(bo.getDetail().getCallStatus())) {
                String tcid = tableCreateService.getTcId(bo.getApiCode());
                Map<String, JSONObject> pushCondition = marketingCommonConfig.getXieChengCallPushCondition();
                JSONObject condition = pushCondition.get(bo.getApiCode());
                JSONArray isBlackApiCodes = condition.getJSONArray("isBlackApiCodes");
                MarketingTransferSyncUser xcTransferTodayNoAdDataByOnlyBlack = marketingTransferSyncUserMapper.getXcTransferTodayNoAdDataByOnlyBlack(
                        tcid, bo.getCaseNum(), isBlackApiCodes);
                if (xcTransferTodayNoAdDataByOnlyBlack != null) {
                    keepRecord(bo, String.format("CallStatus状态是：%d,且当天转化isBlack='1'", bo.getDetail().getCallStatus()));
                    return false;
                }
            }
            // 是延迟队列且没有106：剔除
            if (isDelay != null && isDelay == 1) {
                List<XieChengJudgeConvTypeValue> xieChengJudgeConvType = xieChengJudgeConvTypeService.getJudgeConvType(context.getApiCode(),
                        bo.getCaseNum());
                // 通话明细custNum没查到转化明细(再校验一次)
                if (CollectionUtils.isEmpty(xieChengJudgeConvType)) {
                    return true;
                }

                Boolean hasApplySuccess = xieChengJudgeConvType.get(0).getHasApplySuccess();
                // 转化数据convType没有106
                if (!hasApplySuccess) {
                    log.warn("携程通话明细数据推送客户接口，有效期内命中convType110且一小时内没有命中106，该数据不推送客户接口。custNum：{}", bo.getCaseNum());
                    // 剔除数据也记录到表：b_xiecheng_data
                    keepRecord(bo, "有效期内命中convType110且一小时内没有命中106");
                    return false;
                }
            }

            return true;
        }
        return false;
    }

    private void keepRecord(CallRecordBO bo, String errorMsg) {
        XieChengData xieChengData = new XieChengData();
        xieChengData.setApiCode(bo.getApiCode());
        xieChengData.setActionType("IVR");
        xieChengData.setSha256Tel(bo.getCaseNum());

        xieChengData.setCreateTime(new Date());
        xieChengData.setCreateDate(Integer.parseInt(LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)));
        xieChengData.setLocalId(bo.getId());
        xieChengData.setPushStatus(1);
        xieChengData.setType("1");

        xieChengData.setDataMessage(errorMsg);
        xieChengData.setStatus(2);
        xieChengDataMapper.insertSelective(xieChengData);
    }

    @Override
    public String label() {
        return "XieCheng_CallRecord_Insert_DB_VT";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.XIE_CHENG_CALL_RECORD_INSERT_DB_VT.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return null;
    }
}
