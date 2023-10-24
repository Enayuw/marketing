package com.br.marketing.rule.xiecheng;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.util.DateUtils;
import com.br.marketing.client.robotaiapi.input.ConvTypeConfigConversionData;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.rpcclient.RpcClientProxy;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import com.br.marketing.vo.TransferSyncUserToRobotAiVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.Set;

/**
 *
 * @Description : 携程客服转化规则
 * http://c.100credit.cn/pages/viewpage.action?pageId=92047371
 * ---------------------------------
 * @Author : juanjuan.song
 * @Date : Create in 2022/12/05 10:28
 * 客服转化接口案件编号和手机号二选一必填，不满足则接收转化数据失败
 */
@Service
@Slf4j
public class XieChengCustomerTransferAImpl implements AssembleData<ConversionData> {

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Override
    public ConversionData assemble(Object transmitFact, ProcessHandlerContext context) {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        log.warn("携程推客服转化,apicode={}", transfer.getApiCode());
        ConvTypeConfigConversionData conversionData = new ConvTypeConfigConversionData();
        // 设置convType
        String reserveField1 = transfer.getReserveField1();
        if (StringUtils.hasText(reserveField1)) {
            JSONObject json = JSON.parseObject(reserveField1);
            String convType = json.getInteger("convType").toString();
            conversionData.setConvType(convType);
        }
        conversionData.setDataId(transfer.getId().toString());
        conversionData.setCid(transfer.getCid());
        conversionData.setInversionStatus("0");
        String query = RpcClientProxy.decode(transfer.getCustNum(), "cell", "sha", "");
        conversionData.setPhone(query);
        if (!StringUtils.isEmpty(transfer.getCreateTime())) {
            conversionData.setPartnerProcessDate(DateUtils.format(transfer.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
        }
        TransferSyncUserToRobotAiVO vo = new TransferSyncUserToRobotAiVO();
        BeanUtils.copyProperties(transfer, vo);
        conversionData.setInversionInfo(JSON.toJSONString(vo));
        return conversionData;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) {
        boolean flag = Boolean.FALSE;
        if (transmitFact instanceof MarketingTransferSyncUser) {
            MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
            String reserveField1 = transfer.getReserveField1();
            if (StringUtils.hasText(reserveField1)) {
                JSONObject json = JSON.parseObject(reserveField1);
                Integer convType = json.getInteger("convType");
//                flag = !StringUtils.isEmpty(convType) && 107 == convType;

                // 从配置中心获取convType
                Set<String> convTypeSet = marketingCommonConfig.getPushConvTypeConfig().get(transfer.getApiCode()).keySet();
                flag = !StringUtils.isEmpty(convType) && convTypeSet.contains(convType.toString());
            }
        }
        return flag;
    }

    @Override
    public String label() {
        return "XieCheng_TransferData_CustomerTransfer";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.CUSTOMER_TRANSFER_BY_CONVTYPE.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return null;
    }
}
