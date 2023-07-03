package com.br.marketing.service.Impl.yixin;

import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
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
    public List<MarketingTransferSyncUser> getMarketingTransferSyncUserListA(String cid, String type, Integer pageNum) {
        String apiCode = marketingCommonConfig.getYiXinGetTransferToJueCeApiCode();
        // 获取前一天的日期yyyy-MM-dd
        return marketingTransferSyncUserMapper
                .getYxTransferByApiCodeAtikv_(
                        cid,
                        apiCode,
                        LocalDate.now().minusDays(1).toString(),
                        pageNum
                );
    }

    @Override
    public List<MarketingTransferSyncUser> getMarketingTransferSyncUserListB(String cid,String type, Integer pageNum) {
        String apiCode = marketingCommonConfig.getYiXinGetTransferToJueCeApiCode();
        return marketingTransferSyncUserMapper
                .getYxTransferByApiCodeBtoCtoItikv_(
                        cid,
                        apiCode,
                        LocalDate.now().minusDays(30).toString(),
                        type,
                        pageNum
                );
    }

    @Override
    public List<MarketingTransferSyncUser> getMarketingTransferSyncUserListCtoI(String cid,String type, Integer pageNum) {
        String apiCode = marketingCommonConfig.getYiXinGetTransferToJueCeApiCode();
        return marketingTransferSyncUserMapper
                .getYxTransferByApiCodeBtoCtoItikv_(
                        cid,
                        apiCode,
                        LocalDate.now().toString(),
                        type,
                        pageNum
                );
    }

    private Long getIdIndex(String cid, Long idIndex, String requestDate, String apiCode, String type) {
        return idIndex == null ? marketingTransferSyncUserMapper.getYiXinMinAtoBtoCtoI(cid, apiCode, requestDate, type) : idIndex;
    }
}
