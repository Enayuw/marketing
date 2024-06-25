package com.br.marketing.service.Impl.qifu;

import cn.hutool.core.collection.CollectionUtil;
import com.br.marketing.client.qifu.*;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.MarketingCustomizeDataValidConfigMapper;
import com.br.marketing.mapper.MarketingSyncUserMapper;
import com.br.marketing.mapper.QueryUserRealMessageMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @ClassName QiFuQrySleepUserRealMessageServiceImpl
 * @Description TODO
 * @Author kongbx
 * @Date 2024/6/25 15:51
 */
@Service
@Slf4j
public class QiFuQrySleepUserRealMessageServiceImpl implements QiFuQrySleepUserRealMessageService{

    @Resource
    private MarketingSyncUserMapper marketingSyncUserMapper;
    @Resource
    private MarketingCustomizeDataValidConfigMapper customizeDataValidConfigMapper;
    @Resource
    private QueryUserRealMessageMapper queryUserRealMessageMapper;
    @Autowired
    MarketingCommonConfig marketingCommonConfig;
    @Resource
    private QiFuClients qiFuClients;

    @Override
    public void process(String apiCode) {
        // 根据apiCode和日期获取有效期配置不分页
        LocalDate now = LocalDate.now();
        MarketingCustomizeDataValidConfigExample example = new MarketingCustomizeDataValidConfigExample();
        example.createCriteria().andApiCodeEqualTo(apiCode).andValidStartDateLessThanOrEqualTo(now.toString())
                .andValidEndDateGreaterThanOrEqualTo(now.toString());
        List<MarketingCustomizeDataValidConfig> configList = customizeDataValidConfigMapper.selectByExample(example);
        if (CollectionUtil.isEmpty(configList)) {
            log.warn("api_code:{}【该apiCode无有效期配置】", apiCode);
            return;
        }
        // 根据有效期内的上传数据taskIdSet 去上传明细表查询上传数据
        Set<String> taskIdSet = configList.stream().map(MarketingCustomizeDataValidConfig::getTaskId).collect(Collectors.toSet());
        List<MarketingSyncUser> preUserByTask = marketingSyncUserMapper.getSyncUserByCusBatch(apiCode, taskIdSet);

        // 拼装对应的上传数据 调用促动支用户信息接口
        if(preUserByTask.isEmpty()){
            return;
        }

        // 判断该手机号是否推送过
        QueryUserRealMessageExample queryUserRealMessageExample = new QueryUserRealMessageExample();
        queryUserRealMessageExample.createCriteria().andApiCodeEqualTo(apiCode).andCreateDateEqualTo(now.toString());
        List<QueryUserRealMessage> queryUserRealMessages = queryUserRealMessageMapper.selectByExample(queryUserRealMessageExample);
        Set<String> mobileMd5Set = queryUserRealMessages.stream().map(QueryUserRealMessage::getMobileMd5).collect(Collectors.toSet());

        // 根据cusBatch类型  批次调用
        // 使用Map来存储每种cusBatch类型的用户列表
        Map<String, List<MarketingSyncUser>> cusBatchMap = new HashMap<>();
        for (MarketingSyncUser marketingSyncUser : preUserByTask) {
            // 获取cusBatch类型
            String cusBatch = marketingSyncUser.getCusBatch();
            // 如果map中还没有这种cusBatch类型的列表，就创建一个新的
            cusBatchMap.putIfAbsent(cusBatch, new ArrayList<>());
            // 获取当前cusBatch类型的数据列表
            List<MarketingSyncUser> cusBatchUser = cusBatchMap.get(cusBatch);
            if(!mobileMd5Set.contains(marketingSyncUser.getCellMd5())){
                // 将用户添加到列表中
                cusBatchUser.add(marketingSyncUser);
            }
            // 检查是否达到了50条的限制
            if (cusBatchUser.size() >= 50) {
                action(cusBatchUser,apiCode);
                // 达到了，就保存这些用户并清空列表（或者创建新列表继续添加）
                cusBatchUser.clear(); // 或者从map中移除并重新添加一个新列表
            }
        }
        // 循环结束后，检查是否还有剩余的用户需要保存（因为可能最后一种cusBatch类型的用户数量小于50）
        for (List<MarketingSyncUser> cusBatchUser : cusBatchMap.values()) {
            if (!cusBatchUser.isEmpty()) {
                action(cusBatchUser,apiCode);
            }
        }
    }

    private void action(List<MarketingSyncUser> cusBatchUser,String apiCode){
        int i = marketingCommonConfig.getQiFuQryUserMessageThreadNum() == null ? 10 : marketingCommonConfig.getQiFuQryUserMessageThreadNum();
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(i, i);

        if(cusBatchUser.isEmpty()){
            return;
        }
        String cusBatch;
        ArrayList<RealDataesReq> list = new ArrayList<>();
        for (MarketingSyncUser marketingSyncUser : cusBatchUser) {
            RealDataesReq realDataesReq = new RealDataesReq();
            realDataesReq.setUniqueReqNo(marketingSyncUser.getCustNum());
            realDataesReq.setMobileMd5(marketingSyncUser.getCellMd5());
            list.add(realDataesReq);
        }
        // 根据批次号推送数据
        QrySleepUserRealMessageReq qrySleepUserRealMessageReq = new QrySleepUserRealMessageReq();
        String uuid = UUID.randomUUID().toString().replaceAll("-", "") + System.nanoTime();
        qrySleepUserRealMessageReq.setRequestNo(uuid);
        if(cusBatchUser.size() > 0){
            cusBatch = cusBatchUser.get(0).getCusBatch();
        } else {
            cusBatch = "";
        }
        qrySleepUserRealMessageReq.setBatchNo(cusBatchUser.get(0).getCusBatch());
        qrySleepUserRealMessageReq.setInitiatingType("noArt");
        qrySleepUserRealMessageReq.setPartner("bairong");
        qrySleepUserRealMessageReq.setRealDataes(list);

        threadPool.execute(() -> {
            try {
                // 调用奇富查询用户信息接口
                Result<ResponseData<QrySleepUserRealMessageResp>> dataResult = qiFuClients.qrySleepUserRealMessage(qrySleepUserRealMessageReq);
                if (ResultCode.SUCCESS.getValue().equals(dataResult.getCode())) {
                    ResponseData<QrySleepUserRealMessageResp> data = dataResult.getData();
                    QrySleepUserRealMessageResp qrySleepUserRealMessageResp = data.getData().getT();
                    List<QryUserRealMessage> realDetails = qrySleepUserRealMessageResp.getRealDetails();
                    for (QryUserRealMessage qryUserRealMessage : realDetails) {
                        // 保存返回数据
                        QueryUserRealMessage queryUserRealMessage = new QueryUserRealMessage();
                        queryUserRealMessage.setApiCode(apiCode);
                        queryUserRealMessage.setBatchNo(cusBatch);
                        queryUserRealMessage.setUniqueReqNo(qryUserRealMessage.getUniqueReqNo());
                        queryUserRealMessage.setMobileMd5(qryUserRealMessage.getMobileMd5());
                        queryUserRealMessage.setStopMarketingSign(qryUserRealMessage.getStopMarketingSign());
                        if(!qryUserRealMessage.getUserMessageRes().isEmpty()){
                            queryUserRealMessage.setUserMessage(qryUserRealMessage.getUserMessageRes().toString());
                        }
                        if(!qryUserRealMessage.getRiskMessageRes().isEmpty()){
                            queryUserRealMessage.setRiskMessage(qryUserRealMessage.getRiskMessageRes().toString());
                        }
                        if(!qryUserRealMessage.getTradeMessageRes().isEmpty()){
                            queryUserRealMessage.setTradeMessage(qryUserRealMessage.getTradeMessageRes().toString());
                        }
                        queryUserRealMessage.setCreateDate(LocalDate.now().toString());
                        queryUserRealMessage.setCreateTime(new Date());
                        queryUserRealMessageMapper.insertSelective(queryUserRealMessage);
                    }
                }
            } catch (Exception e) {
                log.error("目标方法执行异常",e);
            }
        });
    }

}
