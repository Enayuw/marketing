package com.br.marketing.service.Impl.yixin;

import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
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
    public List<MarketingTransferSyncUser> getMarketingTransferSyncUserList_A(String cid, Long idIndex) {
        String apiCode = marketingCommonConfig.getYiXinGetTransferToJueCeApiCode();
        // 获取前一天的日期yyyy-MM-dd
        String yesterday = LocalDate.now().minusDays(1).toString();
        idIndex = idIndex == null ? marketingTransferSyncUserMapper.getYiXinMin_A_to_B_to_C_to_I(cid, apiCode, yesterday, null) : idIndex;
        return marketingTransferSyncUserMapper.getYxTransferByApiCode_Atikv_(cid, apiCode, yesterday, idIndex);
    }

    @Override
    public List<MarketingTransferSyncUser> getMarketingTransferSyncUserList_B(String cid, Long idIndex) {
        String apiCode = marketingCommonConfig.getYiXinGetTransferToJueCeApiCode();
        String requestDate = LocalDate.now().minusDays(30).toString();
        idIndex = idIndex == null ? marketingTransferSyncUserMapper.getYiXinMin_A_to_B_to_C_to_I(cid, apiCode, requestDate, "12") : idIndex;
        return marketingTransferSyncUserMapper.getYxTransferByApiCode_B_to_C_to_Itikv_(cid, apiCode, idIndex, requestDate, "12");
    }

    @Override
    public List<MarketingTransferSyncUser> getMarketingTransferSyncUserList_C_to_I(String cid, String actionType, Long idIndex) {
        String requestDate = LocalDate.now().toString();
        String apiCode = marketingCommonConfig.getYiXinGetTransferToJueCeApiCode();

        String type = "";
        switch (actionType) {
            case "C":
                type = "13";
                break;
            case "D":
                type = "23";
                break;
            case "E":
                type = "20";
                break;
            case "F":
                type = "21";
                break;
            case "G":
                type = "8";
                break;
            case "H":
                type = "15";
                break;
            case "I":
                type = "6";
                break;
            default:
                break;
        }
        idIndex = idIndex == null ? marketingTransferSyncUserMapper.getYiXinMin_A_to_B_to_C_to_I(cid, apiCode, requestDate, type) : idIndex;
        return marketingTransferSyncUserMapper.getYxTransferByApiCode_B_to_C_to_Itikv_(cid, apiCode, idIndex, requestDate, type);
    }
}
