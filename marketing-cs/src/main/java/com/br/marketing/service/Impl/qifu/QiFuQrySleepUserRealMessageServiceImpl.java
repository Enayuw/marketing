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

        int i = marketingCommonConfig.getQiFuQryUserMessageThreadNum() == null ? 5 : marketingCommonConfig.getQiFuQryUserMessageThreadNum();
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(i, i);
        Integer pageSize = marketingCommonConfig.getQiFuQryUserMessageSize();

        Set<String> taskIdSet = configList.stream().map(MarketingCustomizeDataValidConfig::getTaskId).collect(Collectors.toSet());
        BQifuClenTaskAction clenTaskAction = new BQifuClenTaskAction();
        clenTaskAction.setId(action.getId());
        clenTaskAction.setClenStatus(2);
        bqifuClenTaskActionMapper.updateByPrimaryKeySelective(clenTaskAction);
        for (String tskId : taskIdSet) {
            Long indexId = null;
            while (true) {
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
            log.error(AlertLog.buildErrorMessage(AlarmSendCodeEnum.QIFUCUDONGZHI_SERVICEERROR.getCode(),
                    TITLE + "，错误信息：" + e.getMessage()), e);
            Thread.currentThread().interrupt();
        }

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
                for (MarketingSyncUser marketingSyncUser : pageList) {
                    QueryUserRealMessage queryUserRealMessage = new QueryUserRealMessage();
                    queryUserRealMessage.setApiCode(apiCode);
                    queryUserRealMessage.setBatchNo(tskId);
                    queryUserRealMessage.setUniqueReqNo(marketingSyncUser.getCustNum());
                    queryUserRealMessage.setMobileMd5(marketingSyncUser.getCellMd5());
                    queryUserRealMessage.setStopMarketingSign("N");
                    queryUserRealMessage.setUserMessage("{\"age\":\"[28,35]\",\"lastLoginTime\":\"2024-06-19 08:07:42\"," +
                            "\"name\":\"张*\",\"sex\":\"M\",\"userExtraInfo\":{\"isLightMarkting\":\"N\",\"operationScene\":\"creditT30\"}}");
                    queryUserRealMessage.setRiskMessage("{\"creditAmt\":180000}");
                    queryUserRealMessage.setTradeMessage("{\"isLoan\":\"N\",\"isSucc\":\"N\"}");
                    queryUserRealMessage.setCreateDate(LocalDate.now().toString());
                    queryUserRealMessage.setCreateTime(new Date());
                    queryUserRealMessageMapper.insertSelective(queryUserRealMessage);
                    log.warn(TITLE + "挡板数据, queryUserRealMessage{}", JSONObject.toJSONString(queryUserRealMessage));
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
                    queryUserRealMessage.setAppletDate(listMap.get(qryUserRealMessage.getMobileMd5()).get(0).getAppletDate());
                    queryUserRealMessage.setUserType(listMap.get(qryUserRealMessage.getMobileMd5()).get(0).getUserType());
                    queryUserRealMessage.setCell(listMap.get(qryUserRealMessage.getMobileMd5()).get(0).getCell());
                    queryUserRealMessageMapper.insertSelective(queryUserRealMessage);
                }
                result.setCode(ResultCode.SUCCESS.getValue());
            }
        } catch (Exception e) {
            log.error(AlertLog.buildErrorMessage(AlarmSendCodeEnum.QIFUCUDONGZHI_SERVICEERROR.getCode(),
                    "调用奇富查询用户方法执行异常，错误信息：" + e.getMessage()), e);
        }
        return result;
    }

    private BQifuClenTaskAction getAction(String apiCode, String now) {
        try {
            BQifuClenTaskActionExample bQifuClenTaskActionExample = new BQifuClenTaskActionExample();
            bQifuClenTaskActionExample.createCriteria().andApiCodeEqualTo(apiCode).andActionDateEqualTo(now).andDeleteFlagEqualTo(0);
            bQifuClenTaskActionExample.setOrderByClause("create_time desc");
            List<BQifuClenTaskAction> bQifuClenTaskActions = bqifuClenTaskActionMapper.selectByExample(bQifuClenTaskActionExample);
            if (ObjectUtil.isNotEmpty(bQifuClenTaskActions)) {
                QueryUserRealMessageExample messageExample = new QueryUserRealMessageExample();
                messageExample.createCriteria().andApiCodeEqualTo(apiCode).andCreateDateEqualTo(now).andIsDeletedEqualTo(0).andStatusIn(Arrays.asList(0, 1));
                List<QueryUserRealMessage> queryUserRealMessages = queryUserRealMessageMapper.selectByExample(messageExample);
                if (CollectionUtil.isEmpty(queryUserRealMessages)) {
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

}
