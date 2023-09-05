package com.br.marketing.service.Impl.transferfieldprocess;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.br.common.log.AlertLog;
import com.br.marketing.bo.SyncUserValidityPeriodBO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.TransferDataDTO;
import com.br.marketing.dto.TransferDataItemDTO;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.enums.ThreeKeyTypeEnum;
import com.br.marketing.mapper.MarketingSyncUserMapper;
import com.br.marketing.service.ICustomerConfigService;
import com.br.marketing.service.Impl.transferfieldprocess.dto.tongcheng.TransferDataItemByTongChengDTO;
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
public class TransferFieldProcessByTongChengFactory implements TransferFieldProcessFactory {


    @Override
    public String customerName() {
        return "tongcheng";
    }

    @Override
    public void fieldProcess(MarketingTransferSyncUser transferSyncUser, TransferDataItemDTO transferDataItemDTO) {
        TransferDataItemByTongChengDTO tongChengDto = (TransferDataItemByTongChengDTO) transferDataItemDTO;
        if (StringUtils.isNotBlank(tongChengDto.getApplyLoan())) {
            String reserveField1Str = "";
            String reserveField1 = transferSyncUser.getReserveField1();
            if (StringUtils.isNotBlank(reserveField1)) {
                try {
                    JSONObject jsonObject = JSON.parseObject(reserveField1);
                    jsonObject.put("applyLoan",tongChengDto.getApplyLoan());
                    reserveField1Str = jsonObject.toJSONString();
                }catch (Exception ex){
                    reserveField1Str += String.format("\"applyLoan\":\"s%\"",tongChengDto.getApplyLoan());
                }
            }else{
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("applyLoan",tongChengDto.getApplyLoan());
                reserveField1Str = jsonObject.toJSONString();
            }
            transferSyncUser.setReserveField1(reserveField1Str);
        }
    }

    @Override
    public Boolean isFormat() {
        return true;
    }

    @Override
    public TransferDataDTO formatTransferObj(String jsonData) {
        return JSON.parseObject(jsonData, new TypeReference<TransferDataDTO<TransferDataItemByTongChengDTO>>() {
        }.getType());
    }
}
