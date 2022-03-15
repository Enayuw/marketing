package com.br.marketing.origin.impl;

import com.br.marketing.entity.MarketingTransferInfo;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUserExample;
import com.br.marketing.mapper.MarketingTransferInfoMapper;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.origin.*;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.InterfaceHandlerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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
public class MarketingTransferDataImpl implements OriginData {

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private MarketingTransferInfoMapper marketingTransferInfoMapper;

    @Resource
    private TableCreateServiceImpl tableCreateService;

    @Resource
    MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;

    @Override
    public List<TransmitFact> collect(MqFact mqFact, ProcessHandlerContext context) {

        List<TransmitFact> list = new ArrayList<>();
        // 1 根据保存到队列的ID查询记录对应的ApiCode、RequestId
        List<MarketingTransferInfo> transferInfos = marketingTransferInfoMapper.findApiCodeRequestIdByIdList(mqFact.getTransferInfoId());
        MarketingTransferInfo transferInfo = transferInfos.get(0);
        transferInfo.setId(mqFact.getTransferInfoId());


        /**
         * 2  遍历数据 根据客户apiCode 及原始详情表数据封装到 map <具体的接口枚举,接口所需对应的参数类列表>
         *     如 { 1:List<BlackListDTO>,4:List<ConversionData>}
         */

        String tcId = tableCreateService.getTcId(transferInfo.getApiCode());
        MarketingTransferSyncUserExample example = new MarketingTransferSyncUserExample();
        example.createCriteria().andApiCodeEqualTo(transferInfo.getApiCode()).
                andRequestIdEqualTo(transferInfo.getRequestId());
        example.settCid(tcId);
        List<MarketingTransferSyncUser> transferList = marketingTransferSyncUserMapper.selectByExample(example);
        for (MarketingTransferSyncUser transferSyncUser : transferList) {
            list.add(new TransmitFact(transferSyncUser));
        }
        return list;
    }

    @Override
    public TransferSource source() {
        return TransferSource.UNIVERSAL_TRANSFER_PROCESS;
    }


    /**
     *  获取该数据流程，数据需要匹配的规则
     * @param mqFact
     * @param context
     * @return
     */
    @Override
    public List<AssembleData> patternMatch(MqFact mqFact, ProcessHandlerContext context) {

        HashMap<String, String> customerRuleMapping = marketingCommonConfig.getCustomerRuleMapping();
        /**
         * 1、获取 apiCode获取所需的规则匹配方法
         */
        List<AssembleData> assembleDataList = new ArrayList<>();
        Collection<AssembleData> values = InterfaceHandlerFactory.assembleDataMap.values();
        for (AssembleData assembleData : values) {
            if (assembleData.label().startsWith(customerRuleMapping.get(context.getApiCode()))){
                assembleDataList.add(assembleData);
            }
        }
        return assembleDataList;
    }
}
