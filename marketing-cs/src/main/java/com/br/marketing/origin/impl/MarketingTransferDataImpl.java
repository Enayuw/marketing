package com.br.marketing.origin.impl;

import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.MarketingTransferInfo;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUserExample;
import com.br.marketing.mapper.MarketingSyncInfoMapper;
import com.br.marketing.mapper.MarketingTransferInfoMapper;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.origin.DataLoadingHandlerService;
import com.br.marketing.origin.MqFact;
import com.br.marketing.origin.OriginDataService;
import com.br.marketing.origin.TransferSource;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * code is far away from bug with the animal protecting
 * ┏┓　　　┏┓
 * ┏┛┻━━━┛┻┓
 * ┃　　　　　　　┃
 * ┃　　　━　　　┃
 * ┃　┳┛　┗┳　┃
 * ┃　　　　　　　┃
 * ┃　　　┻　　　┃
 * ┃　　　　　　　┃
 * ┗━┓　　　┏━┛
 * 　　┃　　　┃神兽保佑
 * 　　┃　　　┃代码无BUG！
 * 　　┃　　　┗━━━┓
 * 　　┃　　　　　　　┣┓
 * 　　┃　　　　　　　┏┛
 * 　　┗┓┓┏━┳┓┏┛
 * 　　　┃┫┫　┃┫┫
 * 　　　┗┻┛　┗┻┛
 *
 * @Description : 数据来源于营销转化表
 * ---------------------------------
 * @Author : jilong.xu
 * @Date : Create in 2022/3/12 14:54
 */

@Service
public class MarketingTransferDataImpl implements OriginDataService {

    @Resource
    private MarketingTransferInfoMapper marketingTransferInfoMapper;

    @Resource
    private DataLoadingHandlerService handlerService;

    @Resource
    MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;

    @Resource
    private MarketingSyncInfoMapper marketingSyncInfoMapper;


    @Override
    public List<Object> collect(MqFact mqFact, ProcessHandlerContext context) {

        List<Object> list = new ArrayList<>();
        // 1 根据保存到队列的ID查询记录对应的ApiCode、RequestId
        List<MarketingTransferInfo> transferInfos = marketingTransferInfoMapper.findApiCodeRequestIdByIdList(mqFact.getSourceId());
        MarketingTransferInfo transferInfo = transferInfos.get(0);
        transferInfo.setId(mqFact.getSourceId());


        /**
         * 2  遍历数据 根据客户apiCode 及原始详情表数据封装到 map <具体的接口枚举,接口所需对应的参数类列表>
         *     如 { 1:List<BlackListDTO>,4:List<ConversionData>}
         */

        String tcId = handlerService.getTcIdFromRedis(transferInfo.getApiCode());
        MarketingTransferSyncUserExample example = new MarketingTransferSyncUserExample();
        example.createCriteria().andApiCodeEqualTo(transferInfo.getApiCode()).
                andRequestIdEqualTo(transferInfo.getRequestId());
        example.settCid(tcId);
        List<MarketingTransferSyncUser> transferList = marketingTransferSyncUserMapper.selectByExample(example);

        /**
         * 将查询信息放入全局上下文中
         */
        context.setTransferInfoId(transferInfo.getId());
        context.setApiCode(transferInfo.getApiCode());

        list.addAll(transferList);
        return list;
    }

    @Override
    public TransferSource source() {
        return TransferSource.UNIVERSAL_TRANSFER_PROCESS;
    }

}
