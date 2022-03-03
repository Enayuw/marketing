package com.br.marketing.strategy;

import com.br.marketing.entity.MarketingTransferInfo;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.mapper.MarketingTransferInfoMapper;
import com.br.marketing.rule.InterfaceParams;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;


@Service
public class InterfaceHandlerService {

    @Resource
    private InterfaceHandlerFactory interfaceHandlerFactory;

    @Resource
    private MarketingTransferInfoMapper marketingTransferInfoMapper;

    /**
     *  处理数据流向
     *  1、根据原始表id，查询该批次中传送数据
     *  2、遍历所有数据，按照不同调用接口逻辑将数据分类
     *  3、不同数据调用不同的接口处理
     */

    public void handleDataDirection(long infoId){

        // 1 根据保存到队列的ID查询记录对应的ApiCode、RequestId
        List<MarketingTransferInfo> list = marketingTransferInfoMapper.findApiCodeRequestIdByIdList(infoId);
        MarketingTransferInfo marketingTransferInfo = list.get(0);
        marketingTransferInfo.setId(infoId);

        List<MarketingTransferSyncUser> transferList = new ArrayList<>();
        Map<Integer, List<InterfaceParams>> map =
                interfaceHandlerFactory.assembleData(marketingTransferInfo.getApiCode(), transferList);

        Set<Integer> set = map.keySet();
        for (Integer enumFlag : set) {
            interfaceHandlerFactory.handler(enumFlag,map.get(enumFlag), marketingTransferInfo);
        }

    }

}
