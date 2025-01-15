package com.br.marketing.rule.yixin;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.common.util.BrCipherMaker;
import com.br.common.util.DateUtils;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.SoleFieldEnum;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.context.RuleDataCollectionEnum;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.mapper.MarketingSyncUserMapper;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import com.br.marketing.vo.TransferSyncUserToRobotAiVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;


/**
 * 宜信实时转化黑名单推客服
 * @author guangxiu.li
 * @date 2025/1/13
 * @description
 */

@Service
@Slf4j
public class YiXinRealtimeBlackDataToCustomer implements AssembleData<ConversionData> {

    @Resource
    MarketingSyncUserMapper marketingSyncUserMapper;

    @Override
    public ConversionData assemble(Object transmitFact, ProcessHandlerContext context) {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser)transmitFact;
        String custNum = transfer.getCustNum();
        String apiCode = transfer.getApiCode();
        ConversionData conversionData = new ConversionData();
        conversionData.setDataId(transfer.getId().toString());
        conversionData.setCid(transfer.getCid());
        conversionData.setCaseNum(custNum);
        conversionData.setInversionStatus("2");
        conversionData.setGroupType(transfer.getUserType());
        conversionData.setInversionDate(transfer.getTransformTime());
        String reserveField1 = transfer.getReserveField1();
        if (StringUtils.hasText(reserveField1)) {
            JSONObject json = JSON.parseObject(reserveField1);
            String transformType = json.getString("transformType");
            transformType = StringUtils.isEmpty(transformType) ? "" :transformType;
            conversionData.setTransformType(transformType);
        }
        MarketingSyncUser syncUser = marketingSyncUserMapper.getCellLatestByCustNum(apiCode, custNum);
        String cell = ObjectUtil.isEmpty(syncUser) ? "" : syncUser.getCell();
        if (ObjectUtil.isNotEmpty(cell)) {
            conversionData.setPhone(BrCipherMaker.getInstance().decode(cell));
        } else {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.YIXIN_SERVICEERROR.getCode()
                    , "宜信实时转化黑名单推客服上传手机号为空！custNum：" + custNum));
            return null;
        }
        conversionData.setTaskId(context.getTransferInfoId().toString());
        conversionData.setEffectiveDate(transfer.getRequestTime());
        if (!StringUtils.isEmpty(transfer.getCreateTime())) {
            conversionData.setPartnerProcessDate(DateUtils.format(transfer.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
        }
        TransferSyncUserToRobotAiVO vo = new TransferSyncUserToRobotAiVO();
        BeanUtils.copyProperties(transfer, vo);
        conversionData.setInversionInfo(JSON.toJSONString(vo));
        // 去重设置
        conversionData.setSoleField(SoleFieldEnum.CUST_NUM_SOLE.getValue());
        conversionData.setSoleType(1);
        return conversionData;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) {
        boolean flag = Boolean.FALSE;
        if(transmitFact instanceof MarketingTransferSyncUser){
            //取指定cid下isBlack为1的custNum，同一custNum当天仅推送一次
            MarketingTransferSyncUser transfer = (MarketingTransferSyncUser)transmitFact;
            String reserveField1 = transfer.getReserveField1();
            if (StringUtils.hasText(reserveField1)){
                JSONObject json = JSON.parseObject(reserveField1);
                Integer isBlack = json.getInteger("isBlack");
                Integer transformType = json.getInteger("transformType");
                boolean isBlackResult = !StringUtils.isEmpty(isBlack) && 1 == isBlack;
                boolean isTransfromType = !StringUtils.isEmpty(transformType) && 1 == transformType;
                if(isBlackResult && isTransfromType){
                    flag = true;
                }
            }
        }
        return flag;
    }

    @Override
    public String label() {
        return "YiXin_RealtimeBlackData_CustomerTransfer";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.CUSTOMER_TRANSFER_SOLE.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return RuleDataCollectionEnum.YI_XIN_DATA_COLLECTION.getCode();
    }
}
