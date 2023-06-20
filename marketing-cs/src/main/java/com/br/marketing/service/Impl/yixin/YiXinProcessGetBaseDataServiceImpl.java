package com.br.marketing.service.Impl.yixin;

import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUserCell;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

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
        String requestDate = LocalDateTime.now().minus(30,ChronoUnit.DAYS).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return marketingTransferSyncUserMapper.getYxTransferByApiCode_B(cid, marketingCommonConfig.getYiXinTransferToJueCeApiCode(), idIndex, requestDate);
    }

    @Override
    public List<MarketingTransferSyncUser> getMarketingTransferSyncUserList_C_to_I(String cid,String actionType,Long idIndex) {
        return null;
    }
}
