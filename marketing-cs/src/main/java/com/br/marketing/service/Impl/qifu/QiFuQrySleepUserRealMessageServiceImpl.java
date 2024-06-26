package com.br.marketing.service.Impl.qifu;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.qifu.*;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.MarketingCustomizeDataValidConfigMapper;
import com.br.marketing.mapper.MarketingSyncUserMapper;
import com.br.marketing.mapper.QueryUserRealMessageMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @ClassName QiFuQrySleepUserRealMessageServiceImpl
 * @Description TODO
 * @Author kongbx
 * @Date 2024/6/25 15:51
 */
@Service
@Slf4j
public class QiFuQrySleepUserRealMessageServiceImpl implements QiFuQrySleepUserRealMessageService {

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

    private final static String TITLE = "【奇富批量接口用户查询】";


    @Override
    public void process(String apiCode) {
        // 根据apiCode和日期获取有效期配置不分页
        String now = LocalDate.now().toString();
        MarketingCustomizeDataValidConfigExample example = new MarketingCustomizeDataValidConfigExample();
        example.createCriteria().andApiCodeEqualTo(apiCode).andValidStartDateLessThanOrEqualTo(now)
                .andValidEndDateGreaterThanOrEqualTo(now).andIsDelEqualTo(1);
        List<MarketingCustomizeDataValidConfig> configList = customizeDataValidConfigMapper.selectByExample(example);
        if (CollectionUtil.isEmpty(configList)) {
            log.warn("api_code:{}【该apiCode无有效期配置】", apiCode);
            return;
        }

        int i = marketingCommonConfig.getQiFuQryUserMessageThreadNum() == null ? 5 : marketingCommonConfig.getQiFuQryUserMessageThreadNum();
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(i, i);
        Integer pageSize = marketingCommonConfig.getQiFuQryUserMessageSize();

        Set<String> taskIdSet = configList.stream().map(MarketingCustomizeDataValidConfig::getTaskId).collect(Collectors.toSet());
        for (String tskId : taskIdSet) {
            Long indexId = null;
            while (true) {
                // 循环获取条件数据，每次2000条
                // 根据手机号 筛选 未推送过的数据
                final List<MarketingSyncUser> pageList = marketingSyncUserMapper.getSyncUserByCusBatch(
                        apiCode, tskId, indexId, now, pageSize);
                if (CollectionUtils.isEmpty(pageList)) {
                    break;
                }
                indexId = pageList.get(pageList.size() - 1).getId();

                List<List<MarketingSyncUser>> partition = ListUtils.partition(pageList, 50);

                partition.forEach(marketingSyncUsers -> {
                    threadPool.submit(() -> action(marketingSyncUsers, apiCode, tskId));
                });
            }
        }
        long taskCount = -1;
        threadPool.shutdown();
        try {
            while (!threadPool.awaitTermination(30, TimeUnit.SECONDS)) {
                long completedTask2Count = threadPool.getCompletedTaskCount();
                if (taskCount == completedTask2Count) {
                    log.warn(TITLE + "业务线程等待超时, apiCode{}", apiCode);
                    break;
                }
                taskCount = completedTask2Count;
            }
        } catch (InterruptedException e) {
            log.error(AlertLog.buildErrorMessage(AlarmSendCodeEnum.ERROR_UNKNOWN.getCode(), e.getMessage()
                    , TITLE), e);
            Thread.currentThread().interrupt();
        }

    }

    private Result<String> action(List<MarketingSyncUser> pageList, String apiCode, String tskId) {
        Result<String> result = new Result<>();
        result.setCode(ResultCode.FAIL.getValue());
        try {
            ArrayList<RealDataesReq> list = new ArrayList<>();
            for (MarketingSyncUser marketingSyncUser : pageList) {
                RealDataesReq realDataesReq = new RealDataesReq();
                realDataesReq.setUniqueReqNo(marketingSyncUser.getCustNum());
                realDataesReq.setMobileMd5(marketingSyncUser.getCellMd5());
                list.add(realDataesReq);
            }
            // 根据批次号推送数据
            QrySleepUserRealMessageReq qrySleepUserRealMessageReq = new QrySleepUserRealMessageReq();

            String uuid = UUID.randomUUID().toString();
            qrySleepUserRealMessageReq.setRequestNo(uuid);
            qrySleepUserRealMessageReq.setBatchNo(tskId);
            qrySleepUserRealMessageReq.setInitiatingType("noArt");
            qrySleepUserRealMessageReq.setPartner("bairong");
            qrySleepUserRealMessageReq.setRealDataes(list);

            // 获取挡板开关
            HashMap<String, Object> mock = marketingCommonConfig.getQifuQryUserMessageMock();
            if (mock.get("switch") == Boolean.TRUE) {
                boolean flg = true;
                for (MarketingSyncUser marketingSyncUser : pageList) {
                    QueryUserRealMessage queryUserRealMessage = new QueryUserRealMessage();
                    queryUserRealMessage.setApiCode(apiCode);
                    queryUserRealMessage.setBatchNo(tskId);
                    queryUserRealMessage.setUniqueReqNo(marketingSyncUser.getCustNum());
                    queryUserRealMessage.setMobileMd5(marketingSyncUser.getCellMd5());
                    queryUserRealMessage.setStopMarketingSign("N");
                    queryUserRealMessage.setUserMessage("{\"age\":\"[28,35]\",\"lastLoginTime\":\"2024-06-19 08:07:42\",\"name\":\"张*\",\"sex\":\"M\",\"userExtraInfo\":{\"isLightMarkting\":\"N\",\"operationScene\":\"creditT30\"}}");
                    queryUserRealMessage.setRiskMessage("{\"creditAmt\":180000}");
                    if(flg){
                        queryUserRealMessage.setTradeMessage("{\"isLoan\":\"N\",\"isSucc\":\"N\"}");
                        flg = false;
                    }else {
                        queryUserRealMessage.setTradeMessage("{\"isLoan\":\"Y\",\"isSucc\":\"Y\"}");
                        flg = true;
                    }
                    queryUserRealMessage.setCreateDate(LocalDate.now().toString());
                    queryUserRealMessage.setCreateTime(new Date());
                    queryUserRealMessageMapper.insertSelective(queryUserRealMessage);
                    log.warn(TITLE + "挡板数据, queryUserRealMessage{}", JSONObject.toJSONString(queryUserRealMessage));
                }
                Thread.sleep(400);
                result.setCode(ResultCode.SUCCESS.getValue());
                return result;
            }

            // 调用奇富查询用户信息接口
            Result<ResponseData<QrySleepUserRealMessageResp>> dataResult = qiFuClients.qrySleepUserRealMessage(qrySleepUserRealMessageReq);
            log.warn(TITLE + "返回结果, dataResult{}", JSONObject.toJSONString(dataResult));
            if (ResultCode.SUCCESS.getValue().equals(dataResult.getCode())) {
                ResponseData<QrySleepUserRealMessageResp> data = dataResult.getData();
                QrySleepUserRealMessageResp qrySleepUserRealMessageResp = data.getData().getT();
                List<QryUserRealMessage> realDetails = qrySleepUserRealMessageResp.getRealDetails();
                for (QryUserRealMessage qryUserRealMessage : realDetails) {
                    // 保存返回数据
                    QueryUserRealMessage queryUserRealMessage = new QueryUserRealMessage();
                    queryUserRealMessage.setApiCode(apiCode);
                    queryUserRealMessage.setBatchNo(tskId);
                    queryUserRealMessage.setUniqueReqNo(qryUserRealMessage.getUniqueReqNo());
                    queryUserRealMessage.setMobileMd5(qryUserRealMessage.getMobileMd5());
                    queryUserRealMessage.setStopMarketingSign(qryUserRealMessage.getStopMarketingSign());
                    if (qryUserRealMessage.getUserMessageRes() != null) {
                        queryUserRealMessage.setUserMessage(qryUserRealMessage.getUserMessageRes().toString());
                    }
                    if (qryUserRealMessage.getRiskMessageRes() != null) {
                        queryUserRealMessage.setRiskMessage(qryUserRealMessage.getRiskMessageRes().toString());
                    }
                    if (qryUserRealMessage.getTradeMessageRes() != null) {
                        queryUserRealMessage.setTradeMessage(qryUserRealMessage.getTradeMessageRes().toString());
                    }
                    queryUserRealMessage.setCreateDate(LocalDate.now().toString());
                    queryUserRealMessage.setCreateTime(new Date());
                    queryUserRealMessageMapper.insertSelective(queryUserRealMessage);
                }
                result.setCode(ResultCode.SUCCESS.getValue());
            }
        } catch (Exception e) {
            log.error("调用奇富查询用户方法执行异常", e);
        }
        return result;
    }

}
