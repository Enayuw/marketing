package com.br.marketing.service.Impl.transferfieldprocess;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.dto.TransferDataDTO;
import com.br.marketing.dto.TransferDataItemDTO;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.enums.ThreeKeyEncryptEnum;
import com.br.marketing.enums.ThreeKeyTypeEnum;
import com.br.marketing.mapper.MarketingSyncUserMapper;
import com.br.marketing.service.Impl.transferfieldprocess.dto.qifu.TransferDataItemByQiFuDTO;
import com.br.marketing.service.TransferFieldProcessFactory;
import com.br.marketing.util.EncAndDecUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class TransferFieldProcessByQiFuFactory implements TransferFieldProcessFactory {
    @Override
    public String customerName() {
        return "360";
    }

    @Resource
    MarketingSyncUserMapper marketingSyncUserMapper;

    @Override
    public Boolean isFormat() {
        return true;
    }


    @Override
    public void fieldProcess(MarketingTransferSyncUser transferSyncUser, TransferDataItemDTO transferDataItemDTO) {

        TransferDataItemByQiFuDTO qiFuDto = (TransferDataItemByQiFuDTO) transferDataItemDTO;
        if (StringUtils.isNotBlank(qiFuDto.getIsAttribution())) {
            String reserveField1Str = "";
            String reserveField1 = transferSyncUser.getReserveField1();
            if (StringUtils.isNotBlank(reserveField1)) {
                try {
                    JSONObject jsonObject = JSON.parseObject(reserveField1);
                    jsonObject.put("isAttribution",qiFuDto.getIsAttribution());
                    reserveField1Str = jsonObject.toJSONString();
                }catch (Exception ex){
                    reserveField1Str += String.format("\"isAttribution\":\"s%\"",qiFuDto.getIsAttribution());
                }
            }else{
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("isAttribution",qiFuDto.getIsAttribution());
                reserveField1Str = jsonObject.toJSONString();
            }
            transferSyncUser.setReserveField1(reserveField1Str);
        }

        if(StringUtils.isNotBlank(transferSyncUser.getCustNum()) && transferSyncUser.getCustNum().length() >= 32){
            String initCustNum = transferSyncUser.getCustNum();
            Result<String> logResult = EncAndDecUtil.digestToLog(transferSyncUser.getCustNum(), ThreeKeyTypeEnum.CELL, ThreeKeyEncryptEnum.md5);
            if(ResultCode.SUCCESS.getValue().equals(logResult.getCode())){
                MarketingSyncUser userLastByCell = marketingSyncUserMapper
                        .getUserLastByCell(transferSyncUser.getApiCode(),logResult.getData());
                if(userLastByCell == null){
                    return;
                }
                transferSyncUser.setCustNum(userLastByCell.getCustNum());
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
    }

    @Override
    public TransferDataDTO formatTransferObj(String jsonData) {
        return JSON.parseObject(jsonData, new TypeReference<TransferDataDTO<TransferDataItemByQiFuDTO>>() {
        }.getType());
    }

}
