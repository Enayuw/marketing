package com.br.marketing.service.Impl.transferfieldprocess;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.enums.ThreeKeyEncryptEnum;
import com.br.marketing.enums.ThreeKeyTypeEnum;
import com.br.marketing.mapper.MarketingSyncUserMapper;
import com.br.marketing.service.ICustomerConfigService;
import com.br.marketing.service.TransferFieldProcessFactory;
import com.br.marketing.util.EncAndDecUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;

@Service
@Slf4j
public class TransferFieldProcessByZhongAnFactory implements TransferFieldProcessFactory {

    @Resource
    MarketingSyncUserMapper syncUserMapper;

    @Autowired
    ICustomerConfigService iCustomerConfigService;

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
        MarketingSyncUser syncUser = syncUserMapper.selectSynsUserByCellLast(transferSyncUser.getApiCode(), cellByLog);
        if (syncUser != null) {
            transferSyncUser.setCustNum(syncUser.getCustNum());
            transferSyncUser.setUserType(syncUser.getUserType());

            JSONObject jb = new JSONObject();
            if (StringUtils.isNotBlank(transferSyncUser.getReserveField1())) {
                try {
                    jb = JSON.parseObject(transferSyncUser.getReserveField1());
                } catch (Exception ex) {
                    jb.put("tmpKey",transferSyncUser.getReserveField1());
                }
                jb.put(cKey, transferSyncUserCustNum);
                jb.put(uKey, transferSyncUserUserType);
                jb.put(tKey, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(syncUser.getCreateTime()));
                transferSyncUser.setReserveField1(jb.toJSONString());
            }
        }
    }
}
