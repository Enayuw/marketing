package com.br.marketing.rule.yixin;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.dassservice.input.black.BlackListDTO;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

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
@Slf4j
public class YiXinArtificialBlackListImpl implements AssembleData<BlackListDTO> {
    @Override
    public BlackListDTO assemble(Object transmitFact, ProcessHandlerContext context) {
        try {
            MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
            BlackListDTO blackListDTO = new BlackListDTO();
            blackListDTO.setDataId(transfer.getId().toString());
            blackListDTO.setUid(transfer.getCustNum());
            blackListDTO.setOrgName("yixin");
            blackListDTO.setApiCode(transfer.getApiCode());
            String reserveField1 = transfer.getReserveField1();
            if (StringUtils.hasText(reserveField1)){
                JSONObject json = JSON.parseObject(reserveField1);
                Date expirationDate = json.getDate("expiration_date");
                if (!StringUtils.isEmpty(expirationDate)){
                    String format = new SimpleDateFormat("yyyy-MM-dd").format(expirationDate);
                    blackListDTO.setExpiration_date(format);
                }
            }
            return blackListDTO;
        } catch (Exception e) {
            log.error("封装电销黑名单数据失败 -- ",e);
        }
        return null;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser) transmitFact;
        /**
         * 失效数据需要转化
         */
        return "0".equals(transfer.getCaseEffective());
    }

    @Override
    public String label() {
        return "YiXin_OverdueData_ArtificialBlackList";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.ARTIFICIAL_BLACK_LIST.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return null;
    }
}
