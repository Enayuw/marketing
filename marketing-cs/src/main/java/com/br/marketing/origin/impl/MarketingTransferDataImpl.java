package com.br.marketing.origin.impl;

import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferInfo;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUserExample;
import com.br.marketing.mapper.MarketingSyncInfoMapper;
import com.br.marketing.mapper.MarketingTransferInfoMapper;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.origin.*;
import com.br.marketing.rule.AssembleData;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.InterfaceHandlerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

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
    private MarketingCommonConfig marketingCommonConfig;

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


        Set<String> set = transferList.stream().map(t -> t.getCustNum()).collect(Collectors.toSet());
        List<MarketingSyncUser> preUserByTask = marketingSyncInfoMapper.getPreUserByInCust(transferInfo.getApiCode(), set);
        Map<String, MarketingSyncUser> collect = preUserByTask.stream().collect(
                Collectors.groupingBy(MarketingSyncUser::getCustNum
                        , Collectors.collectingAndThen(
                                Collectors.reducing((v1, v2) ->
                                        v1.getCreateTime().compareTo(v2.getCreateTime()) > 0 ? v1 : v2)
                                , Optional::get)));
        /**
         * 将查询信息放入全局上下文中
         */
        context = new ProcessHandlerContext(transferInfo.getApiCode(),transferInfo.getId(),collect);

        list.addAll(transferList);
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
