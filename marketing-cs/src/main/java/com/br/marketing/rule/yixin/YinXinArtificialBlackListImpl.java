package com.br.marketing.rule.yixin;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.strategy.InterfaceHandlerEnum;
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
 * @Description : 银杏黑名单转化规则
 * ---------------------------------
 * @Author : jilong.xu
 * @Date : Create in 2022/3/1 15:28
 */
@Service
public class YinXinArtificialBlackListImpl implements AssembleData<ConversionData> {
    @Override
    public ConversionData assemble(MarketingTransferSyncUser transferSyncUser) {
        return null;
    }

    @Override
    public String belongTo() {
        return "7421067";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.ARTIFICIAL_BLACK_LIST.getCode();
    }
}
