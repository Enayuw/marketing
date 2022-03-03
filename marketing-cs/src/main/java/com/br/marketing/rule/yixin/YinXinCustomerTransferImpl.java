package com.br.marketing.rule.yixin;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.common.util.DateUtils;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import com.br.marketing.vo.TransferSyncUserToRobotAiVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * code is far away from bug with the animal protecting
 * ┏┓　　　┏┓
 * ┏┛┻━━━┛┻┓
 * ┃　　　　　　　┃
 * ┃　　　━　　　┃
 * ┃　┳┛　┗┳　┃
 * ┃　　　　　　　┃
 * ┃　　　┻　　　┃
 * ┃　　　　　　　┃
 * ┗━┓　　　┏━┛
 * 　　┃　　　┃神兽保佑
 * 　　┃　　　┃代码无BUG！
 * 　　┃　　　┗━━━┓
 * 　　┃　　　　　　　┣┓
 * 　　┃　　　　　　　┏┛
 * 　　┗┓┓┏━┳┓┏┛
 * 　　　┃┫┫　┃┫┫
 * 　　　┗┻┛　┗┻┛
 *
 * @Description : 银杏客服转化规则
 * ---------------------------------
 * @Author : jilong.xu
 * @Date : Create in 2022/3/1 15:28
 */
@Service
public class YinXinCustomerTransferImpl implements AssembleData<ConversionData> {

    final static String hasTransfer = "1";

    final static String noHasTransfer = "0";

    @Override
    public ConversionData assemble(MarketingTransferSyncUser transfer) {
        ConversionData conversionData = new ConversionData();
        conversionData.setDataId(transfer.getId().toString());
        conversionData.setCid(transfer.getCid());
        conversionData.setCaseNum(transfer.getCustNum());
        conversionData.setGroupType(transfer.getUserType());
        conversionData.setInversionStatus(hasTransfer.equals(transfer.getIfTransform())
                ? "0"
                : (noHasTransfer.equals(transfer.getIfTransform()) ? "1" : transfer.getIfTransform()));
        conversionData.setPartnerProcessDate(DateUtils.format(transfer.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
        /*if (map.containsKey(transfer.getCustNum())) {
            MarketingSyncUser marketingSyncUser = map.get(transfer.getCustNum());
            conversionData.setPhone(BrCipherMaker.getInstance().decode(marketingSyncUser.getCell()));
            conversionData.setTaskId(marketingSyncUser.getCusBatch());
        } else {
            conversionData.setPhone("");
            conversionData.setTaskId("");
        }*/
        TransferSyncUserToRobotAiVO vo = new TransferSyncUserToRobotAiVO();
        BeanUtils.copyProperties(transfer, vo);
        conversionData.setInversionInfo(JSON.toJSONString(vo));
        return conversionData;
    }

    @Override
    public String belongTo() {
        return "5000126";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.CUSTOMER_TRANSFER.getCode();
    }
}
