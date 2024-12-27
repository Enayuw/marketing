package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.service.MergeFieldService;
import com.br.marketing.service.customertagsprocess.valobj.CustomerTagsValue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Merge Field Service
 * @Date: 2024-12-05
 */
@Slf4j
@Service
public class MergeFieldServiceImpl implements MergeFieldService {

    public static final Set<String> FILTER_KEY_SET = new HashSet<>(Arrays.asList("id","apiCode","reserveField1","reserveField2"
            ,"createTime","updateTime","status","failType","isTask","taskTime","isRepeat","cid","tCid"));
    public static final Set<String> THREE_KEY_SET = new HashSet<>(Arrays.asList("idCard","name","cell"));

    public static final String SSS = ":000";

    @Override
    public void mergeUploadAndTransfer(JSONObject result, MarketingTransferSyncUser transfer, MarketingSyncUser syncUser
            , Integer encryptionType) {
        try{
            JSONObject transferObject = (JSONObject)JSON.toJSON(transfer);
            JSONObject syncUserObject =(JSONObject)JSON.toJSON(syncUser);
            if(null != transferObject && !transferObject.isEmpty()){
                mergeJSONObject(result, transferObject, encryptionType);
                mergeReserveField1(result, transferObject);
            }
            if(null != syncUserObject && !syncUserObject.isEmpty()){
                mergeReserveField1(result, syncUserObject);
                mergeJSONObject(result, syncUserObject, encryptionType);
            }
        }catch (Exception e){
            log.warn("transfer and marketingSyncUser merge error,transfer:{}--marketingSyncUser:{}--"
                    , JSON.toJSONString(transfer), JSON.toJSONString(syncUser), e);
        }
    }

    private void mergeReserveField1(JSONObject result, JSONObject jsonObject){
        JSONObject reserveField1 = jsonObject.getJSONObject("reserveField1");
        if(null != reserveField1 && !reserveField1.isEmpty()){
            for (Map.Entry<String, Object> reserveField1Entry : reserveField1.entrySet()){
                String field1Key = reserveField1Entry.getKey();
                Object field1Value = reserveField1Entry.getValue();
                if(null != result.get(field1Key)){
                    if(null != field1Value && StringUtils.isNotBlank(field1Value.toString())){
                        result.put(field1Key,field1Value);
                    }
                }else{
                    result.put(field1Key,field1Value);
                }
            }
        }
    }
    private void mergeJSONObject(JSONObject result, JSONObject jsonObject, Integer encryptionType){
        for (Map.Entry<String, Object> entry : jsonObject.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if(!FILTER_KEY_SET.contains(key)){
                if(THREE_KEY_SET.contains(key) && null != value){
                    result.put(key,get3keyValue(value.toString(), encryptionType));
                }else{
                    if(null != result.get(key)){
                        if(null != value && StringUtils.isNotBlank(value.toString())){
                            result.put(key,value);
                        }
                    }else{
                        result.put(key,value);
                    }
                }
            }
        }
    }
    @Override
    public void formatSSSTransfer(MarketingTransferSyncUser transfer){
        if(StringUtils.isNotBlank(transfer.getRegisterTime())){
            transfer.setRegisterTime(transfer.getRegisterTime().replace(SSS,""));
        }
        if(StringUtils.isNotBlank(transfer.getLoginTime())){
            transfer.setLoginTime(transfer.getLoginTime().replace(SSS,""));
        }
        if(StringUtils.isNotBlank(transfer.getApplyDt())){
            transfer.setApplyDt(transfer.getApplyDt().replace(SSS,""));
        }
        if(StringUtils.isNotBlank(transfer.getApplyTime())){
            transfer.setApplyTime(transfer.getApplyTime().replace(SSS,""));
        }
        if(StringUtils.isNotBlank(transfer.getRefuseTime())){
            transfer.setRefuseTime(transfer.getRefuseTime().replace(SSS,""));
        }
        if(StringUtils.isNotBlank(transfer.getAuditTime())){
            transfer.setAuditTime(transfer.getAuditTime().replace(SSS,""));
        }
        if(StringUtils.isNotBlank(transfer.getLentTime())){
            transfer.setLentTime(transfer.getLentTime().replace(SSS,""));
        }
        if(StringUtils.isNotBlank(transfer.getSettleTime())){
            transfer.setSettleTime(transfer.getSettleTime().replace(SSS,""));
        }
        if(StringUtils.isNotBlank(transfer.getTransformTime())){
            transfer.setTransformTime(transfer.getTransformTime().replace(SSS,""));
        }
        if(StringUtils.isNotBlank(transfer.getInsertTime())){
            transfer.setInsertTime(transfer.getInsertTime().replace(SSS,""));
        }
    }
    @Override
    public String get3keyValue(String content, Integer encryptionType) {
        if (StringUtils.isBlank(content)) {
            return content;
        }
        String decode = BrCipherMaker.getInstance().decode(content);
        if (CustomerTagsValue.PushJc3keyTypeEnum.MD5_ALL.getValue().equals(encryptionType)) {
            return StringUtils.isNotBlank(decode) ? DigestUtils.md5DigestAsHex(decode.getBytes()) : content;
        }else if(CustomerTagsValue.PushJc3keyTypeEnum.INIT.getValue().equals(encryptionType)){
            return content;
        }
        return content;
    }

}