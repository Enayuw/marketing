package com.br.marketing.service.Impl.yixin;

import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 宜信基础数据实现类
 *
 * @author GuangChao.Zhang
 * @version 1.0
 * @date 2023/6/16 17:39
 */
@Service
@Slf4j
public class YiXinProcessGetBaseDataServiceImpl implements YiXinProcessGetBaseDataService {

    @Resource
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;


    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Override
    public List<MarketingTransferSyncUser> getMarketingTransferSyncUserListA(String tCid,
                                                                             String apiCode,
                                                                             String requestDate,
                                                                             Long indexId ) {

        // 获取前一天的日期yyyy-MM-dd
        return marketingTransferSyncUserMapper
                .getYxTransferByApiCodeAtikv_(
                        tCid,
                        apiCode,
                        requestDate,
                        indexId,
                        marketingCommonConfig.getYiXinSearchPageSize()
                );
    }

    @Override
    public List<MarketingTransferSyncUser> getMarketingTransferSyncUserListBtoCtoI(String tCid,
                                                                                   String apiCode,
                                                                                   String type,
                                                                                   String requestDate,
                                                                                   Long indexId) {

        return marketingTransferSyncUserMapper
                .getYxTransferByApiCodeBtoCtoItikv_(
                        tCid,
                        apiCode,
                        requestDate,
                        type,
                        indexId,
                        marketingCommonConfig.getYiXinSearchPageSize()
                );
    }
    @Override
    public List<MarketingTransferSyncUser> getMarketingTransferSyncUserListCJK(String tCid,
                                                                                   String apiCode,
                                                                                   String type,
                                                                                   String requestDate,
                                                                                   Long indexId,String registerChannel) {

        return marketingTransferSyncUserMapper
                .getYxTransferByApiCodeCJKtikv_(
                        tCid,
                        apiCode,
                        requestDate,
                        type,
                        indexId,registerChannel,
                        marketingCommonConfig.getYiXinSearchPageSize()
                );
    }

    public List<MarketingTransferSyncUser> getMarketingTransferSyncUserListL(String tCid,
                                                                             String apiCode,
                                                                             String requestDate,String applyDtStart,String applyDtEnd,
                                                                             Long indexId) {

        // 获取前一天的日期yyyy-MM-dd
        return marketingTransferSyncUserMapper
                .getYxTransferByApiCodeLtikv_(
                        tCid,
                        apiCode,
                        requestDate,applyDtStart,applyDtEnd,
                        indexId,
                        marketingCommonConfig.getYiXinSearchPageSize()
                );
    }


    @Override
    public List<MarketingTransferSyncUser> getYXMarketingTransferByLiveType(String tCid
            , String apiCode
            , String requestDate
            , Long indexId
            , String liveType) {

        return marketingTransferSyncUserMapper
                .getYXMarketingTransferByLiveTypetikv_(
                        tCid,
                        apiCode,
                        requestDate,
                        indexId,liveType,
                        marketingCommonConfig.getYiXinSearchPageSize()
                );
    }
}
