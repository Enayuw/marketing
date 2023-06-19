package com.br.marketing.service.Impl.YiXin;

import com.br.marketing.entity.MarketingTransferSyncUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
    @Override
    public List<MarketingTransferSyncUser> getMarketingTransferSyncUserList_A() {
        return null;
    }

    @Override
    public List<MarketingTransferSyncUser> getMarketingTransferSyncUserList_B() {
        return null;
    }

    @Override
    public List<MarketingTransferSyncUser> getMarketingTransferSyncUserList_C_to_I(String actionType) {
        return null;
    }
}
