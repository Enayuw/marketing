package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.bo.PeriodOfValidityBO;
import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUserExample;
import com.br.marketing.mapper.MarketingSyncUserMapper;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.service.IPeriodOfValidityService;
import com.br.marketing.service.IRongShuPushDaasService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
/**
 * @Description:榕树推电销service
 * -------------------------------
 * @Author: Lizhen
 * ------------------------------
 */

@Service
@Slf4j
public class IRongShuPushDaasServiceImpl implements IRongShuPushDaasService {

    @Autowired
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private IPeriodOfValidityService iPeriodOfValidityService;

    @Resource
    private MarketingSyncUserMapper marketingSyncUserMapper;

    @Resource
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;

    @Override
    public boolean isFilter(String apiCode, String custNum, String tcId) {
        MarketingSyncUser marketingSyncUser = marketingSyncUserMapper.selectSynsUserByCustNumLast(apiCode, custNum);
        //上传数据不在有效期内，已过期
        if (iPeriodOfValidityService.isExpire(new Date(),
                marketingCommonConfig.getRsValidityDay(), marketingSyncUser.getCreateTime())) {
            return true;
        }
        //剔除有效期内 转化数据userType=4||userType=5||applyLoan=1
        MarketingTransferSyncUserExample example = new MarketingTransferSyncUserExample();
        example.settCid(tcId);
        example.createCriteria()
                .andApiCodeEqualTo(marketingSyncUser.getApiCode())
                .andCustNumEqualTo(marketingSyncUser.getCustNum())
                .andRequestDataGreaterThanOrEqualTo(marketingSyncUser.getAppletDate());
        List<MarketingTransferSyncUser> marketingTransferSyncUsers = marketingTransferSyncUserMapper.selectByExample(example);
        for (MarketingTransferSyncUser marketingTransferSyncUser : marketingTransferSyncUsers) {
            if (StringUtils.isNotEmpty(marketingTransferSyncUser.getReserveField1())) {
                JSONObject json = JSON.parseObject(marketingTransferSyncUser.getReserveField1());
                if ("1".equals(json.getString("applyLoan"))) {
                    return true;
                }
                if (("4").equals(marketingTransferSyncUser.getUserType()) || ("5").equals(marketingTransferSyncUser.getUserType())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isFilterUserUserType(String apiCode, String custNum, String tcId, SyncUserValidityPeriodsBO boMap) {
        MarketingSyncUser marketingSyncUser = boMap.getSyncUsers().get(0);
        List<PeriodOfValidityBO.Builder> builders = boMap.getBuilders();
        for (PeriodOfValidityBO.Builder builder : builders) {
            PeriodOfValidityBO periodOfValidityBO = builder.addDateString().builder();
            String beginDateStr = periodOfValidityBO.getBeginDateStr();
            String enDateStr = periodOfValidityBO.getEnDateStr();
            //剔除有效期内 转化数据userType=4||userType=5
            MarketingTransferSyncUserExample example = new MarketingTransferSyncUserExample();
            example.settCid(tcId);
            example.createCriteria()
                    .andApiCodeEqualTo(marketingSyncUser.getApiCode())
                    .andCustNumEqualTo(marketingSyncUser.getCustNum())
                    .andRequestDataGreaterThanOrEqualTo(beginDateStr)
                    .andRequestDataLessThanOrEqualTo(enDateStr);
            List<MarketingTransferSyncUser> marketingTransferSyncUsers = marketingTransferSyncUserMapper.selectByExample(example);
            for (MarketingTransferSyncUser marketingTransferSyncUser : marketingTransferSyncUsers) {
                String userType = marketingTransferSyncUser.getUserType();
                boolean flag = ("4").equals(userType) || ("5").equals(userType);
                if (flag) {
                    return true;
                }
            }
        }
        return false;
    }
}

