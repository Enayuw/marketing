package com.br.marketing.service.Impl.transferfieldprocess;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.bo.SyncUserValidityPeriodBO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.enums.ThreeKeyTypeEnum;
import com.br.marketing.mapper.MarketingSyncUserMapper;
import com.br.marketing.service.ICustomerConfigService;
import com.br.marketing.service.TransferDataValidityPeriodService;
import com.br.marketing.service.TransferFieldProcessFactory;
import com.br.marketing.util.EncAndDecUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Map;

@Service
@Slf4j
public class TransferFieldProcessByZhongAnFactory implements TransferFieldProcessFactory {

    @Resource
    MarketingSyncUserMapper syncUserMapper;

    @Autowired
    ICustomerConfigService iCustomerConfigService;

    @Resource
    private TransferDataValidityPeriodService transferDataValidityPeriodService;

    final static String cKey = "initCustNum";

    final static String uKey = "initUserType";

    final static String tKey = "uploadCreateTime";

    @Override
    public String customerName() {
        return "zhongan";
    }

    @Override
    public void fieldProcess(MarketingTransferSyncUser transferSyncUser) {
        String transferSyncUserCustNum = transferSyncUser.getCustNum();
        String cellByLog = transferSyncUserCustNum;
        String transferSyncUserUserType = transferSyncUser.getUserType();
        Result<String> cellRes = EncAndDecUtil.digestToLog(transferSyncUserCustNum, ThreeKeyTypeEnum.CELL, Boolean.FALSE);
//        Result<String> cellRes = iCustomerConfigService.getThreeKeyDigToLog(transferSyncUser.getApiCode(),transferSyncUserCustNum,ThreeKeyTypeEnum.CELL);
        if (ResultCode.SUCCESS.getValue().equals(cellRes.getCode())) {
            cellByLog = cellRes.getData();
        }
        /* 2023-07-13 11:30  业务逻辑变更，洗数前需判断有效期 */
        Map<String, SyncUserValidityPeriodBO> validityMap = transferDataValidityPeriodService
                .getValidityPeriodCellBatchFirstVersion(Collections.singleton(cellByLog), transferSyncUser.getApiCode()
                        , transferSyncUser.getRequestData());
        SyncUserValidityPeriodBO bo;
        MarketingSyncUser syncUser;
        if (CollectionUtils.isEmpty(validityMap)
                || (bo = validityMap.get(cellByLog)) == null
                || (syncUser = bo.getSyncUser()) == null) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_VALIDITY_PERIOD.getCode()
                    , "众安未配置任何有效期，请配置对应的有效期规则，apiCode:" + transferSyncUser.getApiCode()
                            + ";tcid:" + transferSyncUser.gettCid()
                            + ";cell:" + cellByLog
                    , transferSyncUser.getApiCode() + "众安转化数据清洗入库，"
                            + AlarmSendCodeEnum.EXCEPTION_VALIDITY_PERIOD.getMessage()));
            return;
        }
        transferSyncUser.setCustNum(syncUser.getCustNum());
        transferSyncUser.setUserType(syncUser.getUserType());

        JSONObject jb = new JSONObject();
        if (StringUtils.isNotBlank(transferSyncUser.getReserveField1())) {
            try {
                jb = JSON.parseObject(transferSyncUser.getReserveField1());
            } catch (Exception ex) {
                jb.put("tmpKey", transferSyncUser.getReserveField1());
            }
        }
        jb.put(cKey, transferSyncUserCustNum);
        jb.put(uKey, transferSyncUserUserType);
        jb.put(tKey, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(syncUser.getCreateTime()));
        transferSyncUser.setReserveField1(jb.toJSONString());
    }
}
