package com.br.marketing.origin.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.MarketingTransferInfo;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUserExample;
import com.br.marketing.mapper.MarketingTransferInfoMapper;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.origin.DataLoadingHandlerService;
import com.br.marketing.origin.MqFact;
import com.br.marketing.origin.OriginDataService;
import com.br.marketing.origin.TransferSource;
import org.springframework.stereotype.Service;
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
 * @Description : 宜信实时转化数据流程
 * ---------------------------------
 * @Author : jilong.xu
 * @Date : Create in 2022/3/12 14:54
 */

@Service
public class MarketingRealTimeTransferDataImpl implements OriginDataService {

    @Resource
    private MarketingTransferInfoMapper marketingTransferInfoMapper;

    @Resource
    private DataLoadingHandlerService handlerService;

    @Resource
    MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;


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

        Predicate<MarketingTransferSyncUser> predicate = syncUser ->  {
                String reserveField1 = syncUser.getReserveField1();
                if (StringUtils.hasText(reserveField1)){
                    JSONObject json = JSON.parseObject(reserveField1);
                    return 1 == json.getInteger("transformType");
                }
                return false;
        };
        List<MarketingTransferSyncUser> collect = transferList.stream().filter(predicate).collect(Collectors.toList());

        /**
         * 将查询信息放入全局上下文中
         */
        context.setTransferInfoId(transferInfo.getId());
        context.setApiCode(transferInfo.getApiCode());


        return new ArrayList<>(collect);
    }

    @Override
    public TransferSource source() {
        return TransferSource.REALTIME_TRANSFER_PROCESS;
    }

}
