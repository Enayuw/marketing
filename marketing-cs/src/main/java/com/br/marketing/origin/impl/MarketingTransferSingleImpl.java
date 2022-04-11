package com.br.marketing.origin.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.context.ProcessHandlerContext;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUserExample;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.origin.DataLoadingHandlerService;
import com.br.marketing.origin.MqFact;
import com.br.marketing.origin.OriginDataService;
import com.br.marketing.origin.TransferSource;
import lombok.extern.slf4j.Slf4j;
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
 * @Description : 用于转化详情记录的查询
 * ---------------------------------
 * @Author : jilong.xu
 * @Date : Create in 2022/4/11 16:52
 */
@Service
@Slf4j
public class MarketingTransferSingleImpl implements OriginDataService {

    @Resource
    private DataLoadingHandlerService handlerService;

    @Resource
    MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;

    @Override
    public List<Object> collect(MqFact mqFact, ProcessHandlerContext context) {


        try {
            JSONObject message = JSON.parseObject(mqFact.getMessage());
            String apiCode = message.getString("apiCode");


            /**
             * 2  遍历数据 根据客户apiCode 及原始详情表数据封装到 map <具体的接口枚举,接口所需对应的参数类列表>
             *     如 { 1:List<BlackListDTO>,4:List<ConversionData>}
             */

            String tcId = handlerService.getTcIdFromRedis(apiCode);
            MarketingTransferSyncUserExample example = new MarketingTransferSyncUserExample();
            example.createCriteria().andApiCodeEqualTo(apiCode).andIdEqualTo(mqFact.getSourceId());
            example.settCid(tcId);
            List<MarketingTransferSyncUser> transferList = marketingTransferSyncUserMapper.selectByExample(example);
            /**
             * 将查询信息放入全局上下文中
             */
            context.setTransferInfoId(message.getLong("transferInfoId"));
            context.setApiCode(apiCode);

            return new ArrayList<>(transferList);
        } catch (Exception e) {
            log.error("转化详情记录查询失败 -- {} -- ", mqFact, e);
        }
        return new ArrayList<>();
    }

    @Override
    public TransferSource source() {
        return TransferSource.UNIVERSAL_TRANSFER_SINGLE_PROCESS;
    }
}
