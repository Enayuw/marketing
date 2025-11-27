package com.br.marketing.service.Impl.qifu;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.qifu.*;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.*;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.ThreadPoolAdjustmentUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
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
    @Resource
    private BQifuClenTaskActionMapper bqifuClenTaskActionMapper;
    @Autowired
    MarketingCommonConfig marketingCommonConfig;
    @Resource
    private QiFuClients qiFuClients;

    private final static String TITLE = "【奇富批量接口用户查询】";


    @Override
    public void process(String apiCode) {
        // 根据apiCode和日期获取有效期配置不分页
        String now = LocalDate.now().toString();
        BQifuClenTaskAction action = getAction(apiCode, now);
        if (ObjectUtil.isEmpty(action)) {
            return;
        }
        MarketingCustomizeDataValidConfigExample example = new MarketingCustomizeDataValidConfigExample();
        example.createCriteria().andApiCodeEqualTo(apiCode).andValidStartDateLessThanOrEqualTo(now)
                .andValidEndDateGreaterThanOrEqualTo(now).andIsDelEqualTo(1);
        List<MarketingCustomizeDataValidConfig> configList = customizeDataValidConfigMapper.selectByExample(example);
        if (CollectionUtil.isEmpty(configList)) {
            log.warn("api_code:{}【该apiCode无有效期配置】", apiCode);
            return;
        }
        // 每次循环重新获取线程数配置
        int i = marketingCommonConfig.getQiFuQryUserMessageThreadNum() == null ? 5 : marketingCommonConfig.getQiFuQryUserMessageThreadNum();
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(i, i);
        Integer pageSize = marketingCommonConfig.getQiFuQryUserMessageSize();

        Set<String> taskIdSet = configList.stream().map(MarketingCustomizeDataValidConfig::getTaskId).collect(Collectors.toSet());
        for (String tskId : taskIdSet) {
            Long indexId = null;
            while (true) {
               if (ObjectUtil.isNotEmpty(marketingCommonConfig.getQiFuQryUserMessageThreadNum())
                       && threadPool.getCorePoolSize() != marketingCommonConfig.getQiFuQryUserMessageThreadNum()) {
                   ThreadPoolAdjustmentUtil.adjustThreadPoolSize(threadPool, marketingCommonConfig.getQiFuQryUserMessageThreadNum());
               }
                // 循环获取条件数据，每次2000条
                // 根据手机号 筛选 未推送过的数据
                final List<MarketingSyncUser> pageList = marketingSyncUserMapper.getSyncUserByCusBatch(
                        apiCode, tskId, indexId, now, pageSize);

                log.warn(TITLE+"筛选数据:{}", pageList);
                if (CollectionUtils.isEmpty(pageList)) {
                    break;
                }
                indexId = pageList.get(pageList.size() - 1).getId();

                List<List<MarketingSyncUser>> partition = ListUtils.partition(pageList, 50);

                partition.forEach((List<MarketingSyncUser> p) -> {
                    threadPool.submit(() -> action(p, apiCode, tskId));
                });
            }
        }
        // 在每次内循环结束时等待当前线程池完成
        long taskCount = -1;
        threadPool.shutdown();
        try {
            while (!threadPool.awaitTermination(30, TimeUnit.SECONDS)) {
                String str = String.format("线程状态(客户：%s,活动线程：%d,核心线程数：%d,未完成任务数：%d)"
                        , apiCode, threadPool.getActiveCount(), threadPool.getCorePoolSize()
                        , threadPool.getTaskCount() - threadPool.getCompletedTaskCount());
                log.warn(TITLE + str);
            }
        } catch (InterruptedException e) {
            log.error(AlertLog.buildErrorMessage(AlarmSendCodeEnum.QIFUCUDONGZHI_SERVICEERROR.getCode(),
                    TITLE + "，错误信息：" + e.getMessage()), e);
            Thread.currentThread().interrupt();
        }
        BQifuClenTaskAction clenTaskAction = new BQifuClenTaskAction();
        clenTaskAction.setId(action.getId());
        clenTaskAction.setClenStatus(2);
        bqifuClenTaskActionMapper.updateByPrimaryKeySelective(clenTaskAction);

    }

    private Result<String> action(List<MarketingSyncUser> pageList, String apiCode, String tskId) {
        Result<String> result = new Result<>();
        result.setCode(ResultCode.FAIL.getValue());
        Map<String, List<MarketingSyncUser>> listMap = pageList.stream().collect(Collectors.groupingBy(MarketingSyncUser::getCellMd5));
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
                try {
                    long time = 200;
                    if (mock.get("time") != null) {
                        time = Long.parseLong(mock.get("time").toString());
                    }
                    // 模拟真实接口调用耗时，记录开始时间
                    long startMockTime = System.currentTimeMillis();
                    
                    // 使用配置的固定延迟时间
                    Thread.sleep(time);
                    
                    // 记录结束时间并计算耗时
                    long endMockTime = System.currentTimeMillis();
                    log.warn(TITLE + "挡板模拟接口调用完成, 耗时:{}ms", (endMockTime - startMockTime));
                } catch (InterruptedException e) {
                    log.warn(TITLE + "挡板延迟模拟被中断");
                    Thread.currentThread().interrupt();
                }
                
                List<QueryUserRealMessage> batchInsertList = new ArrayList<>();
                for (MarketingSyncUser marketingSyncUser : pageList) {
                    QueryUserRealMessage queryUserRealMessage = createQueryUserRealMessage(apiCode, tskId, marketingSyncUser);
                    batchInsertList.add(queryUserRealMessage);
                }
                // 处理批量数据
                if (!CollectionUtils.isEmpty(batchInsertList)) {
                    batchInsertAndClear(batchInsertList, "挡板数据批量插入");
                }
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

                List<QueryUserRealMessage> batchInsertList = new ArrayList<>();
                for (QryUserRealMessage qryUserRealMessage : realDetails) {
                    // 保存返回数据
                    QueryUserRealMessage queryUserRealMessage = new QueryUserRealMessage();
                    queryUserRealMessage.setApiCode(emptyDefault(apiCode));
                    queryUserRealMessage.setBatchNo(emptyDefault(tskId));
                    queryUserRealMessage.setUniqueReqNo(emptyDefault(qryUserRealMessage.getUniqueReqNo()));
                    queryUserRealMessage.setMobileMd5(emptyDefault(qryUserRealMessage.getMobileMd5()));
                    queryUserRealMessage.setStopMarketingSign(emptyDefault(qryUserRealMessage.getStopMarketingSign()));
                    queryUserRealMessage.setUserMessage(emptyDefault(qryUserRealMessage.getUserMessageRes()));
                    queryUserRealMessage.setRiskMessage(emptyDefault(qryUserRealMessage.getRiskMessageRes()));
                    queryUserRealMessage.setTradeMessage(emptyDefault(qryUserRealMessage.getTradeMessageRes()));
                    queryUserRealMessage.setCreateDate(LocalDate.now().toString());
                    queryUserRealMessage.setCreateTime(new Date());

                    // 安全获取关联数据
                    List<MarketingSyncUser> users = listMap.get(qryUserRealMessage.getMobileMd5());
                    if (users != null && !users.isEmpty()) {
                        queryUserRealMessage.setAppletDate(emptyDefault(users.get(0).getAppletDate()));
                        queryUserRealMessage.setUserType(emptyDefault(users.get(0).getUserType()));
                        queryUserRealMessage.setCell(emptyDefault(users.get(0).getCell()));
                    }
                    batchInsertList.add(queryUserRealMessage);
                }
                // 批量插入
                if (!CollectionUtils.isEmpty(batchInsertList)) {
                    batchInsertAndClear(batchInsertList, "批量插入API返回数据");
                }
                result.setCode(ResultCode.SUCCESS.getValue());
            }
        } catch (Exception e) {
            log.error(AlertLog.buildErrorMessage(AlarmSendCodeEnum.QIFUCUDONGZHI_SERVICEERROR.getCode(),
                    "调用奇富查询用户方法执行异常，错误信息：" + e.getMessage()), e);
        }
        return result;
    }

    /**
     * 批量插入并清空列表
     *
     * @param batchList  待插入列表
     * @param logMessage 日志消息
     */
    private void batchInsertAndClear(List<QueryUserRealMessage> batchList, String logMessage) {
        try {
            if (!CollectionUtils.isEmpty(batchList)) {
                queryUserRealMessageMapper.batchInsert(batchList);
                log.warn(TITLE + "{}, 数量:{}", logMessage, batchList.size());
                batchList.clear();
            }
        } catch (Exception e) {
            log.error(AlertLog.buildErrorMessage(AlarmSendCodeEnum.QIFUCUDONGZHI_SERVICEERROR.getCode(),
                    "批量插入数据异常：" + e.getMessage()), e);
        }
    }

    /**
     * 创建用户消息对象
     */
    private QueryUserRealMessage createQueryUserRealMessage(String apiCode, String tskId, MarketingSyncUser marketingSyncUser) {
        QueryUserRealMessage queryUserRealMessage = new QueryUserRealMessage();
        queryUserRealMessage.setApiCode(apiCode);
        queryUserRealMessage.setBatchNo(tskId);
        queryUserRealMessage.setUniqueReqNo(marketingSyncUser.getCustNum());
        queryUserRealMessage.setMobileMd5(marketingSyncUser.getCellMd5());
        queryUserRealMessage.setStopMarketingSign("N");
        queryUserRealMessage.setUserMessage("{\"age\":\"[28,35]\",\"lastLoginTime\":\"2024-06-19 08:07:42\"," +
                "\"name\":\"张*\",\"sex\":\"M\",\"userExtraInfo\":{\"isLightMarkting\":\"N\",\"operationScene\":\"creditT30\"}}");
        queryUserRealMessage.setRiskMessage("{\"creditAmt\":180000}");
        queryUserRealMessage.setTradeMessage("{\"isSucc\":\"Y\",\"succAmtType\":\"1\",\"curAvailableQuota\":\"7\",\"hisSettleTime\":\"2025-03\",\"isLoan\":\"Y\"}");
        queryUserRealMessage.setCreateDate(LocalDate.now().toString());
        queryUserRealMessage.setCreateTime(new Date());
        queryUserRealMessage.setAppletDate(marketingSyncUser.getAppletDate());
        queryUserRealMessage.setUserType(marketingSyncUser.getUserType());
        queryUserRealMessage.setCell(marketingSyncUser.getCell());
        return queryUserRealMessage;
    }

    private BQifuClenTaskAction getAction(String apiCode, String now) {
        try {
            BQifuClenTaskActionExample bQifuClenTaskActionExample = new BQifuClenTaskActionExample();
            bQifuClenTaskActionExample.createCriteria().andApiCodeEqualTo(apiCode).andActionDateEqualTo(now).andDeleteFlagEqualTo(0);
            bQifuClenTaskActionExample.setOrderByClause("create_time desc");
            List<BQifuClenTaskAction> bQifuClenTaskActions = bqifuClenTaskActionMapper.selectByExample(bQifuClenTaskActionExample);
            if (ObjectUtil.isNotEmpty(bQifuClenTaskActions)) {
                if (bQifuClenTaskActions.get(0).getClenStatus() == 3) {
                    return null;
                }
                QueryUserRealMessageExample messageExample = new QueryUserRealMessageExample();
                messageExample.createCriteria().andApiCodeEqualTo(apiCode).andCreateDateEqualTo(now)
                        .andIsDeletedEqualTo(0).andEsUpdateStatusNotEqualTo(2);
                long count = queryUserRealMessageMapper.countByExample(messageExample);
                QueryUserRealMessageExample example = new QueryUserRealMessageExample();
                example.createCriteria().andApiCodeEqualTo(apiCode).andCreateDateEqualTo(now)
                        .andIsDeletedEqualTo(0).andUploadUpdateStatusNotEqualTo(2);
                long countByExample = queryUserRealMessageMapper.countByExample(example);

                if (count == 0 && countByExample == 0) {
                    BQifuClenTaskAction action = new BQifuClenTaskAction();
                    action.setId(bQifuClenTaskActions.get(0).getId());
                    action.setClenStatus(3);
                    bqifuClenTaskActionMapper.updateByPrimaryKeySelective(action);
                    return null;
                }
                return null;
            }
            BQifuClenTaskAction bQifuClenTaskAction = new BQifuClenTaskAction();
            bQifuClenTaskAction.setApiCode(apiCode);
            bQifuClenTaskAction.setActionDate(now);
            bQifuClenTaskAction.setCreateTime(new Date());
            bqifuClenTaskActionMapper.insertSelective(bQifuClenTaskAction);
            return bqifuClenTaskActionMapper.selectByExample(bQifuClenTaskActionExample).get(0);
        } catch (Exception e) {
            log.error(AlertLog.buildErrorMessage(AlarmSendCodeEnum.QIFUCUDONGZHI_SERVICEERROR.getCode(),
                    "促动支清洗记录插入异常，错误信息：" + e.getMessage()), e);
            return null;
        }
    }

    private String emptyDefault(Object value) {
        return ObjectUtil.isNotEmpty(value) ? value.toString() : "";
    }
}
