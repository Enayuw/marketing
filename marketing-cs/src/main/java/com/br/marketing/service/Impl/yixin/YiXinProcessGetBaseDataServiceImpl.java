package com.br.marketing.service.Impl.yixin;

import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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



    @Override
    public List<MarketingTransferSyncUser> getMarketingTransferSyncUserListCustNumA(String tCid, String apiCode,  Long indexId,String requestDate) {
        return marketingTransferSyncUserMapper.getCustNumByIdA(tCid, apiCode,  requestDate, indexId);

    }
    @Override
    public List<MarketingTransferSyncUser> getMarketingTransferSyncUserListCustNum(String tCid, String apiCode, String type, Long indexId,String requestDate) {
        return marketingTransferSyncUserMapper.getCustNumById(tCid, apiCode, type, requestDate, indexId);

    }
    @Override
    public List<MarketingTransferSyncUser> getMarketingTransferSyncUserListA(String tCid,String apiCode,  String requestDate,Long indexId
                                                                             ) {

//        Set<String> custNumSet = getCustNumSet(marketingTransferSyncUserList);
        // 获取前一天的日期yyyy-MM-dd
        return marketingTransferSyncUserMapper
                .getYxTransferByApiCodeAtikv_(
                        tCid,
                        apiCode,
                        requestDate,
                        indexId
                );
    }

    @Override
    public List<MarketingTransferSyncUser> getMarketingTransferSyncUserListBtoCtoI(String tCid,String apiCode, String type, String requestDate,Long indexId) {
//        Set<String> custNumSet = getCustNumSet(marketingTransferSyncUserList);
        return marketingTransferSyncUserMapper
                .getYxTransferByApiCodeBtoCtoItikv_(
                        tCid,
                        apiCode,
                        requestDate,
                        type,
                        indexId
                );
    }

    private static Set<String> getCustNumSet(List<MarketingTransferSyncUser> marketingTransferSyncUserList) {
        return marketingTransferSyncUserList.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toSet());
    }

}
