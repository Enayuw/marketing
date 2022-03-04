package com.br.marketing.rule.yixin;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.dassservice.input.black.BlackListDTO;
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
public class YiXinArtificialBlackListImpl implements AssembleData<BlackListDTO> {
    @Override
    public BlackListDTO assemble(MarketingTransferSyncUser transfer) {
        BlackListDTO blackListDTO = new BlackListDTO();
        blackListDTO.setDataId(transfer.getId().toString());
        blackListDTO.setUid(transfer.getCustNum());
        blackListDTO.setOrgName("yixin");
        blackListDTO.setApiCode(transfer.getApiCode());
        return blackListDTO;
    }

    @Override
    public boolean isNeedAssemble(MarketingTransferSyncUser transferSyncUser) {
        /**
         * 失效数据需要转化
         */
        return "0".equals(transferSyncUser.getCaseEffective());
    }

    @Override
    public String label() {
        return "YiXin_OverdueData_ArtificialBlackList";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.ARTIFICIAL_BLACK_LIST.getCode();
    }
}
