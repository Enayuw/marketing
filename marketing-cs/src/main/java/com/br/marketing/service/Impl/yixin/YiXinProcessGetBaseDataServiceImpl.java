package com.br.marketing.service.Impl.yixin;

import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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
        return null;
    }

    @Override
    public List<MarketingTransferSyncUser> getMarketingTransferSyncUserList_B(String cid,Long idIndex) {

        return marketingTransferSyncUserMapper.getYxTransferByApiCode_B(cid, marketingCommonConfig.getYiXinTransferToJueCeApiCode(), idIndex);
    }

    @Override
    public List<MarketingTransferSyncUser> getMarketingTransferSyncUserList_C_to_I(String cid,String actionType,Long idIndex) {
        return null;
    }
}
