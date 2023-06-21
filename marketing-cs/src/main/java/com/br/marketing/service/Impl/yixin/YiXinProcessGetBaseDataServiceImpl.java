package com.br.marketing.service.Impl.yixin;

import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

/**
 * 宜信基础数据实现类
 * @author GuangChao.Zhang
 * @version 1.0
 * @date 2023/6/16 17:39
 */
@Service
@Slf4j
public class YiXinProcessGetBaseDataServiceImpl implements YiXinProcessGetBaseDataService{

    @Resource
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Override
    public List<MarketingTransferSyncUser> getMarketingTransferSyncUserList_A(String cid,Long idIndex) {
        // todo
        // 取apicode
        String apiCode = "";
        // 获取前一天的日期
        String yesterday = LocalDate.now().minusDays(1).toString();
        return marketingTransferSyncUserMapper.getYxTransferByApiCode_A(cid,apiCode,yesterday,idIndex);
    }

    @Override
    public List<MarketingTransferSyncUser> getMarketingTransferSyncUserList_B(String cid,Long idIndex) {
        String apiCode = marketingCommonConfig.getYiXinGetTransferToJueCeApiCode();
        if (StringUtils.isBlank(apiCode)) {
            return Collections.emptyList();
        }
        String requestDate = LocalDate.now().minusDays(30).toString();
        return marketingTransferSyncUserMapper.getYxTransferByApiCode_B_to_C_to_I(cid, marketingCommonConfig.getYiXinGetTransferToJueCeApiCode(), idIndex, requestDate, "12");
    }

    @Override
    public List<MarketingTransferSyncUser> getMarketingTransferSyncUserList_C_to_I(String cid, String actionType, Long idIndex) {
        String apiCode = marketingCommonConfig.getYiXinGetTransferToJueCeApiCode();
        if (StringUtils.isBlank(apiCode)) {
            return Collections.emptyList();
        }
        String requestDate = LocalDate.now().toString();
        return marketingTransferSyncUserMapper.getYxTransferByApiCode_B_to_C_to_I(cid, apiCode, idIndex, requestDate, actionType);
    }
}
