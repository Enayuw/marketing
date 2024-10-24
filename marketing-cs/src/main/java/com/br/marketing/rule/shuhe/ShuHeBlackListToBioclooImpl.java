package com.br.marketing.rule.shuhe;

import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.encryption.Md5Utils;
import com.br.common.util.BrCipherMaker;
import com.br.common.util.StringUtils;
import com.br.marketing.client.biocloo.input.DataSoleDTO;
import com.br.marketing.common.enums.SoleFieldEnum;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.mapper.MarketingSyncUserMapper;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.rule.shuhe.util.ShuHeBlackListUtil;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.InterfaceHandlerEnum;
import com.google.api.client.util.Sets;

import cn.hutool.core.collection.CollectionUtil;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * <a href="https://c.100credit.cn/pages/viewpage.action?pageId=178192891">【紧急】D20240906数禾促首借自动化转黑名单（营销→bkl）-3710166</a>
 *
 * @author senyang.zheng
 * @date 2024/09/07
 */
@Service
@Slf4j
public class ShuHeBlackListToBioclooImpl implements AssembleData<DataSoleDTO> {

    @Resource
    private MarketingSyncUserMapper marketingSyncUserMapper;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Override
    public DataSoleDTO assemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        MarketingTransferSyncUser transfer = (MarketingTransferSyncUser)transmitFact;
        String reserveField1 = transfer.getReserveField1();
        JSONObject json = JSON.parseObject(reserveField1);
        String expireDate = ShuHeBlackListUtil.getBlackDataExpireDate(transfer, marketingCommonConfig.getShuhePushBlackDay());
        if (StringUtils.isNotEmpty(expireDate)) {
            DataSoleDTO dataSoleDTO = new DataSoleDTO();
            dataSoleDTO.setStatus("0");
            JSONObject userTypeJson = marketingCommonConfig.getShuHeToBioclooUserTypeAndApiCodeMapping();
            JSONObject proxyJson = userTypeJson.getJSONObject("百可录");
            dataSoleDTO.setApiCode(proxyJson.getString(context.getApiCode()));
            dataSoleDTO.setCaseNum(transfer.getCustNum());
            dataSoleDTO.setDataId(String.valueOf(transfer.getId()));
            String decode = BrCipherMaker.getInstance().decode(json.getString("cell"));
            dataSoleDTO.setPhone(Md5Utils.cell32(decode));
            // 去重参数设置
            dataSoleDTO.setInitId(transfer.getId());
            dataSoleDTO.setSoleField(SoleFieldEnum.CELL_STATUS_SOLE.getValue());
            dataSoleDTO.setSoleType(1);
            if (!StringUtils.isEmpty(expireDate)) {
                dataSoleDTO.setExpireDate(expireDate);
            }
            log.warn("数禾促首借推送百可录黑名单,apiCode={},custNum={}", proxyJson.getString(context.getApiCode()), transfer.getCustNum());
            return dataSoleDTO;
        }
        return null;
    }

    @Override
    public boolean isNeedAssemble(Object transmitFact, ProcessHandlerContext context) throws Exception {
        if (transmitFact instanceof MarketingTransferSyncUser) {
            MarketingTransferSyncUser transfer = (MarketingTransferSyncUser)transmitFact;
            String userType = transfer.getUserType();
            JSONObject userTypeJson = marketingCommonConfig.getShuHeToBioclooUserTypeAndApiCodeMapping();
            if (!userTypeJson.containsKey(userType)) {
                return false;
            }
            JSONObject proxyJson = userTypeJson.getJSONObject("百可录");
            Set<String> custNumSet = Sets.newHashSet();
            custNumSet.add(transfer.getCustNum());
            List<MarketingSyncUser> syncUserList =
                marketingSyncUserMapper.getCellLastByCustNums(proxyJson.getString(context.getApiCode()), custNumSet);
            return !CollectionUtil.isEmpty(syncUserList);
        }
        return false;
    }

    @Override
    public String label() {
        return "ShuHe_BlackList_To_Biocloo";
    }

    @Override
    public Integer dataDirection() {
        return InterfaceHandlerEnum.BIOCLOO_BLACK_LIST.getCode();
    }

    @Override
    public Integer ruleDataCollection() {
        return null;
    }
}
