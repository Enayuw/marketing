package com.br.marketing.service.Impl.zhongbang;

import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Description ZhongBangToDassFilterGetDataServiceImpl
 * @Author hong.chen
 * @CreateTime 2023/08/23
 */
@Service
@Slf4j
public class ZhongBangToDassFilterGetDataServiceImpl implements ZhongBangToDassFilterGetDataService {
    @Resource
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;

    @Override
    public List<MarketingTransferSyncUser> getMarketingTransferSyncUserListFirst(String tCid, String apiCode, String requestDate, Long indexId) {
        return marketingTransferSyncUserMapper
                .getZhongBangTransferByApiCodetikv_(
                        tCid,
                        apiCode,
                        requestDate,
                        indexId
                );
    }

    @Override
    public List<MarketingTransferSyncUser> getNoFirstCuShen(String tCid, String apiCode, String requestDate, String lastDateStart,
                                                            String lastDateEnd, Long indexId) {
        return marketingTransferSyncUserMapper
                .getZhongBangNoFirstCuShentikv_(
                        tCid,
                        apiCode,
                        requestDate,
                        lastDateStart, lastDateEnd, indexId
                );
    }

    @Override
    public List<MarketingTransferSyncUser> getNoFirstCuTi(String tCid, String apiCode, String requestDate, String lastDateStart, String lastDateEnd
            , Long indexId) {
        return marketingTransferSyncUserMapper
                .getZhongBangNoFirstCuTitikv_(
                        tCid,
                        apiCode,
                        requestDate,
                        lastDateStart, lastDateEnd, indexId
                );
    }
}
