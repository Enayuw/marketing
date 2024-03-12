package com.br.marketing.service.Impl.transferfieldprocess;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.dto.TransferDataDTO;
import com.br.marketing.dto.TransferDataItemDTO;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.service.TransferFieldProcessFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class TransferFieldProcessByXiechengFactory implements TransferFieldProcessFactory {
    @Override
    public String customerName() {
        return "xiecheng";
    }

    @Override
    public void fieldProcess(MarketingTransferSyncUser transferSyncUser, TransferDataItemDTO transferDataItemDTO) {
        Boolean isBlack = Boolean.FALSE;
        if (StringUtils.isNotBlank(transferSyncUser.getReserveField1())&&transferSyncUser.getReserveField1().contains("isBlack")) {
            JSONObject extendJb = JSONObject.parseObject(transferSyncUser.getReserveField1());
            if ("1".equals(extendJb.getString("isBlack"))) {
                isBlack = Boolean.TRUE;
            }
        }
        if(!isBlack && StringUtils.isNotBlank(transferSyncUser.getCustNum()) && transferSyncUser.getCustNum().length() > 18){
            String initCustNum = transferSyncUser.getCustNum();
            String newCustNum = transferSyncUser.getCustNum().substring(18);
            transferSyncUser.setCustNum(newCustNum);
            String reserveField1 = transferSyncUser.getReserveField1();
            if (StringUtils.isNotBlank(reserveField1)) {
                try {
                    JSONObject json = JSON.parseObject(reserveField1);
                    json.put("initCustNum", initCustNum);
                    transferSyncUser.setReserveField1(JSON.toJSONString(json));
                } catch (Exception e) {
                    transferSyncUser.setReserveField1(reserveField1 + "," + initCustNum);
                }
            } else {
                JSONObject json = new JSONObject();
                json.put("initCustNum", initCustNum);
                transferSyncUser.setReserveField1(JSON.toJSONString(json));
            }
        }
    }

    @Override
    public TransferDataDTO formatTransferObj(String jsonData) {
        return null;
    }
}
