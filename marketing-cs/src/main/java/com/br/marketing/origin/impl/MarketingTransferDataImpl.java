package com.br.marketing.origin.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.dto.customer.CallRecordBO;
import com.br.marketing.entity.MarketingTransferInfo;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUserExample;
import com.br.marketing.mapper.MarketingTransferInfoMapper;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.origin.DataLoadingHandlerService;
import com.br.marketing.origin.MqFact;
import com.br.marketing.origin.OriginDataService;
import com.br.marketing.origin.TransferSource;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
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
    private MarketingTransferInfoMapper marketingTransferInfoMapper;

    @Resource
    private DataLoadingHandlerService handlerService;

    @Resource
    MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;


    @Override
    public List<Object> collect(MqFact mqFact, ProcessHandlerContext context) {

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

        /**
         * 宜信特殊逻辑处理
         * 宜信失效数据流程和实时数据流程走通用转化逻辑
         */
        List<String> yiXinApiCode = marketingCommonConfig.getYiXinApiCode();
        if (!CollectionUtils.isEmpty(yiXinApiCode) && yiXinApiCode.contains(transferInfo.getApiCode())) {
            Predicate<MarketingTransferSyncUser> predicate = syncUser -> {
                boolean overdueData = "0".equals(syncUser.getCaseEffective());
                boolean realTime = false;
                String reserveField1 = syncUser.getReserveField1();
                if (StringUtils.hasText(reserveField1)) {
                    JSONObject json = JSON.parseObject(reserveField1);
                    realTime = "1".equals(json.getString("transformType"));
                }
                return overdueData || realTime;
            };
            List<MarketingTransferSyncUser> collect = transferList.stream().filter(predicate).collect(Collectors.toList());
            return new ArrayList<>(collect);

        }
        return new ArrayList<>(transferList);
    }

    @Override
    public TransferSource source() {
        return TransferSource.UNIVERSAL_TRANSFER_PROCESS;
    }

    @Override
    public List<Long> getIdList(List<Object> collect) {
        List<Long> idList = new ArrayList<>();
        for (int i = 0; i < collect.size(); i++) {
            MarketingTransferSyncUser transferSyncUser = (MarketingTransferSyncUser)collect.get(i);
            idList.add(transferSyncUser.getId());
        }
        return idList;
    }

}
