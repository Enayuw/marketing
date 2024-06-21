package com.br.marketing.rule.xiecheng;

import java.util.List;

import javax.annotation.Resource;

import com.br.marketing.strategy.InterfaceHandlerEnum;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.dto.XiechengCollidingDataEliminationDTO;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.XiechengCollidingDataElimination;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.speedconfig.MarketingCommonConfig;

import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 携程CPA撞库过滤转化接口convType=107或105
 *
 * @author senyang.zheng
 * @date 2024/06/18
 */
@Service
@Slf4j
public class XieChengTransferDataToCpaFilter implements AssembleData<XiechengCollidingDataEliminationDTO> {

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Override
    public XiechengCollidingDataEliminationDTO assemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser)transmitFact;
        String reserveField1 = transfer.getReserveField1();
        JSONObject json = JSON.parseObject(reserveField1);
        Integer convType = json.getInteger("convType");
        XiechengCollidingDataElimination xiechengCollidingDataElimination = new XiechengCollidingDataElimination();
        xiechengCollidingDataElimination.setBizId(transfer.getId());
        xiechengCollidingDataElimination.setBizType(0);
        xiechengCollidingDataElimination.setCellSha256CodeList(transfer.getCustNum());
        xiechengCollidingDataElimination.setRemark("转化数据包含" + convType);
        XiechengCollidingDataEliminationDTO dto = new XiechengCollidingDataEliminationDTO();
        dto.setXiechengCollidingDataElimination(xiechengCollidingDataElimination);
        return dto;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        boolean flag = Boolean.FALSE;
        if (transmitFact instanceof MarketingTransferSyncUser) {
            MarketingTransferSyncUser transfer = (MarketingTransferSyncUser)transmitFact;
            String reserveField1 = transfer.getReserveField1();
            if (StringUtils.isNotEmpty(reserveField1)) {
                JSONObject json = JSON.parseObject(reserveField1);
                Integer convType = json.getInteger("convType");
                // 从配置中心获取convType
                List<String> convTypeSet = marketingCommonConfig.getXieChengCpaExcludeConvTypeConfig();
                flag = ObjectUtil.isNotEmpty(convType) && convTypeSet.contains(convType.toString());
            }
        }
        return flag;
    }

    @Override
    public String label() {
        return "XieCheng_Transfer_Data_To_Cpa_Filter";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.XIE_CHENG_CPA_FILTER_INSERT_DB.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return null;
    }
}
