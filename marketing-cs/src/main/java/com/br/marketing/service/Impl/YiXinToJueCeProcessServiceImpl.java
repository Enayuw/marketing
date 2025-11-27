package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.client.intelligentcustomerservice.input.*;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.DistributeSourceTypeEnum;
import com.br.marketing.common.enums.DistributeTypeEnum;
import com.br.marketing.common.enums.SoleFieldEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.dto.DataJoinLogDTO;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUserCell;
import com.br.marketing.entity.TransferActionFront;
import com.br.marketing.mapper.MarketingSyncUserMapper;
import com.br.marketing.mapper.MarketingTransferInfoMapper;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.service.Impl.yixin.YiXinProcessExcludeRuleData;
import com.br.marketing.service.Impl.yixin.YiXinProcessGetBaseDataService;
import com.br.marketing.service.TransferDataValidityPeriodService;
import com.br.marketing.service.YiXinToJueCeProcessService;
import com.br.marketing.service.ZnkfPushService;
import com.br.marketing.service.customertagsprocess.CustomerTagsProcessServiceImpl;
import com.br.marketing.service.customertagsprocess.valobj.CustomerTagsValue;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.MethodRetryHandlerService;
import com.br.marketing.util.ThreadPoolAdjustmentUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 宜信推决策流程
 *
 * @author GuangChao.Zhang
 * @version 1.0
 * @date 2023/6/16 17:23
 */
@Service
@Slf4j
public class YiXinToJueCeProcessServiceImpl implements YiXinToJueCeProcessService {
    private static final TreeMap<String, Integer> ACTONTFROUNTYPETREE = new TreeMap<>();

    static {
        ACTONTFROUNTYPETREE.put("A", 3);
        ACTONTFROUNTYPETREE.put("B", 4);
        ACTONTFROUNTYPETREE.put("C", 5);
        ACTONTFROUNTYPETREE.put("D", 6);
        ACTONTFROUNTYPETREE.put("E", 7);
        ACTONTFROUNTYPETREE.put("F", 8);
        ACTONTFROUNTYPETREE.put("G", 9);
        ACTONTFROUNTYPETREE.put("H", 10);
        ACTONTFROUNTYPETREE.put("I", 11);
        ACTONTFROUNTYPETREE.put("J", 12);
        ACTONTFROUNTYPETREE.put("K", 13);
        ACTONTFROUNTYPETREE.put("L", 14);
    }
    @Resource
    private YiXinProcessGetBaseDataService yiXinProcessGetBaseDataService;

    @Resource
    private YiXinProcessExcludeRuleData yiXinProcessExcludeRuleData;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private MethodRetryHandlerService methodRetryHandlerService;


    @Resource
    private TransferDataValidityPeriodService transferDataValidityPeriodService;

    @Resource
    private MarketingTransferInfoMapper marketingTransferInfoMapper;

    @Resource
    private TableCreateServiceImpl tableCreateService;

    @Resource
    private ZnkfPushService znkfPushService;

    @Resource
    private JobManager jobManager;

    @Resource
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;

    @Resource
    private MarketingSyncUserMapper marketingSyncUserMapper;

    @Resource
    CustomerTagsProcessServiceImpl customerTagsProcessService;

    private static final Integer PARTITION = 2000;

    @Override
    public void doProcess(LinkedHashMap<String, String> actionTypeLink) {
        String apiCodeTransfer = checkApiCode();
        Boolean pushBlackPhoneEnd = znkfPushService.isPushBlackPhoneEnd(apiCodeTransfer, LocalDate.now().toString());
        if (!pushBlackPhoneEnd && LocalDateTime.now().getHour() < 11) {
            log.warn("未查询到黑名单结束标识！");
            return;
        }

        yiXinToJueCeAction(actionTypeLink, apiCodeTransfer);

    }
    private  Result<Long> actionFront(String apiCodeTransfer,Integer actionType){
        Result<TransferActionFront> frontData = jobManager.getFrontData(apiCodeTransfer, LocalDate.now().toString(), actionType);
        if (!ResultCode.SUCCESS.getValue().equals(frontData.getCode())) {
            return new Result().setCode(ResultCode.FAIL.getValue());
        }
        Long jobId;
        TransferActionFront actionFront = frontData.getData();
        if (actionFront == null) {
            jobId = jobManager.saveFrontData(apiCodeTransfer, LocalDate.now().toString(), actionType);
        } else {
            jobId = actionFront.getId();
        }
        return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(jobId);
    }
    private void yiXinToJueCeAction(LinkedHashMap<String, String> actionTypeLink, String apiCodeTransfer) {
        String tcId = tableCreateService.getTcId(apiCodeTransfer);
        actionTypeLink.forEach((String k, String v) -> {
            switch (k) {
                case "A":
                    Result<Long> resultA = actionFront(apiCodeTransfer, ACTONTFROUNTYPETREE.get(k));
                    if (!ResultCode.SUCCESS.getValue().equals(resultA.getCode())) {
                        break;
                    }
                    pushMarketingTransferSyncUsersA(k, v, tcId);
                    updateActionFront(resultA);
                    break;
                case "B":
                case "D":
                case "E":
                case "F":
                case "H":
                case "I":
                    actionData(k, v, tcId, apiCodeTransfer);
                    break;
                case "C":
                    actionDataCJK(k, v, tcId, apiCodeTransfer, "1");
                    break;
                case "J":
                    actionDataCJK(k, v, tcId, apiCodeTransfer, "3");
                    break;
                case "K":
                    actionDataCJK(k, v, tcId, apiCodeTransfer, "2");
                    break;
                case "L":
                    actionDataL(apiCodeTransfer, tcId, k, v);
                    break;
                case "G":
                    actionDataG(k, v, tcId, apiCodeTransfer);
                    break;
                default:
                    log.warn("宜信转化数据推决策类型异常");
                    break;
            }
        });

    }

    private void actionDataG(String actionType, String v, String tcId, String apiCodeTransfer) {
        if (isTransferLast(tcId, apiCodeTransfer)) {
            Result<Long> resultK = actionFront(apiCodeTransfer, ACTONTFROUNTYPETREE.get(actionType));
            if (!ResultCode.SUCCESS.getValue().equals(resultK.getCode())) {
                return;
            }
            getMarketingTransferSyncUserListBtoCtoI(actionType, v, tcId);
            //推送liveType=9的数据
            pushTransferDataLiveTypeNine(actionType, v, tcId);
            updateActionFront(resultK);
        }
    }

    private void pushTransferDataLiveTypeNine(String actionType, String v, String tcId) {

        Long indexId = null;
        // 创建线程池
        ThreadPoolExecutor yiXinToJueCeThread = getYiXinToJueCeThread();
        String requestDate = LocalDate.now().toString();
        String apiCode = marketingCommonConfig.getYiXinGetTransferToJueCeApiCode();
        while (true) {
            initThreadPoolParam(yiXinToJueCeThread);
            List<MarketingTransferSyncUser> marketingTransferSyncUserList =
                    yiXinProcessGetBaseDataService.getYXMarketingTransferByLiveType(tcId,apiCode,
                            requestDate,indexId,"9");
            if(marketingTransferSyncUserList.isEmpty()){
                break;
            }
            indexId = marketingTransferSyncUserList.get(marketingTransferSyncUserList.size() - 1).getId();
            List<List<MarketingTransferSyncUser>> partition = ListUtils.partition(marketingTransferSyncUserList, PARTITION);
            partition.forEach(users->{
                List<MarketingTransferSyncUser> tpList = new ArrayList<>();
                tpList.addAll(users);
                yiXinToJueCeThread.submit(() -> threadDoProcess(tpList, actionType));
            });
        }
        yiXinToJueCeThread.shutdown();
        try {
            while (!yiXinToJueCeThread.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.info("等待线程池结束");
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    private void actionDataL(String apiCodeTransfer, String tcId, String k, String v) {
        if (!isTransferLast(tcId, apiCodeTransfer)) {
            return;
        }
        Result<Long> resultL = actionFront(apiCodeTransfer, ACTONTFROUNTYPETREE.get(k));
        if (!ResultCode.SUCCESS.getValue().equals(resultL.getCode())) {
            return;
        }
        pushMarketingTransferSyncUsersL(k, v, tcId);
        updateActionFront(resultL);
    }

    private void actionData(String k
            , String v
            , String tcId
            , String apiCodeTransfer) {
        if (isTransferLast(tcId, apiCodeTransfer)) {
            Result<Long> resultK = actionFront(apiCodeTransfer, ACTONTFROUNTYPETREE.get(k));
            if (!ResultCode.SUCCESS.getValue().equals(resultK.getCode())) {
                return;
            }
            getMarketingTransferSyncUserListBtoCtoI(k, v, tcId);
            updateActionFront(resultK);
        }
    }

    private void actionDataCJK(String k
            , String v
            , String tcId
            , String apiCodeTransfer, String registerChannel) {
        if (isTransferLast(tcId, apiCodeTransfer)) {
            Result<Long> resultK = actionFront(apiCodeTransfer, ACTONTFROUNTYPETREE.get(k));
            if (!ResultCode.SUCCESS.getValue().equals(resultK.getCode())) {
                return;
            }
            getMarketingTransferSyncUserListCJK(k, v, tcId, registerChannel);
            updateActionFront(resultK);
        }
    }

    /**
     * 2023-07-14 14:19
     * 检查registerChannel
     */
    private Predicate<MarketingTransferSyncUser> checkRegisterChannel(final String registerChannel) {
        return user -> {
            String reserveField1 = user.getReserveField1();
            if (StringUtils.isBlank(reserveField1)) {
                return false;
            }
            JSONObject jsonObject = JSONObject.parseObject(reserveField1);
            return registerChannel.equals(jsonObject.getString("registerChannel"));
        };
    }

    private void updateActionFront(Result<Long> result) {
        jobManager.updateFrontDataStatus(result.getData(), 2);
    }

    private boolean isTransferLast(String tcId, String apiCodeTransfer) {
        // 查询转化数据last =1 的数据是否传输到详情表。2000个
        String requestId = marketingTransferInfoMapper.countByApiCodAndLastOne(apiCodeTransfer, LocalDate.now().toString(), "1");
        if (StringUtils.isEmpty(requestId)) {
            log.warn("宜信转化数据为传输last1");
            return false;
        }
        if(marketingTransferSyncUserMapper.getCountByRequestId(tcId,requestId) == 0){
            log.warn("宜信转化数据requestId：{},未找到对应的详情数据",requestId);
            return false;
        }
        return true;
    }

    private String checkApiCode() {
        String apiCodeTransfer = marketingCommonConfig.getYiXinGetTransferToJueCeApiCode();
        if (StringUtils.isBlank(apiCodeTransfer)) {
            log.error("宜信推送决策未配置apiCode");
        }
        return apiCodeTransfer;
    }

    /**
     * 情况 a
     *
     * @param tcId cid
     */
    private void pushMarketingTransferSyncUsersA(String actionType, String type, String tcId) {
        Long indexId = 3000L;
        // 创建线程池
        ThreadPoolExecutor yiXinToJueCeThread = getYiXinToJueCeThread();
        String requestDate = LocalDate.now().minusDays(1).toString();
        String apiCode = marketingCommonConfig.getYiXinGetTransferToJueCeApiCode();
        while (true) {
            initThreadPoolParam(yiXinToJueCeThread);
            log.warn("开始时间:{}",LocalDateTime.now());
            List<MarketingTransferSyncUser> marketingTransferSyncUserList =
                    yiXinProcessGetBaseDataService.getMarketingTransferSyncUserListA(tcId,apiCode,
                    requestDate,indexId);
            log.warn("结束时间:{}",LocalDateTime.now());
            if(marketingTransferSyncUserList.isEmpty()){
                break;
            }
            indexId = marketingTransferSyncUserList.get(marketingTransferSyncUserList.size() - 1).getId();
            List<List<MarketingTransferSyncUser>> partition = ListUtils.partition(marketingTransferSyncUserList, PARTITION);
            partition.forEach(users->{
                List<MarketingTransferSyncUser> tpList = new ArrayList<>();
                tpList.addAll(users);
                yiXinToJueCeThread.submit(() -> threadDoProcess(tpList, actionType));
            });
        }
        yiXinToJueCeThread.shutdown();
        try {
            while (!yiXinToJueCeThread.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.info("等待线程池结束");
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    /**
     * 情况 b
     *
     * @param tcId cid
     */
    private void getMarketingTransferSyncUserListBtoCtoI(String actionType, String type, String tcId) {
        Long indexId = 3000L;
        // 创建线程池
        ThreadPoolExecutor yiXinToJueCeThread = getYiXinToJueCeThread();
        String requestDate = LocalDate.now().toString();
        String apiCode = marketingCommonConfig.getYiXinGetTransferToJueCeApiCode();
        if ("B".equals(actionType)) {
            requestDate = LocalDate.now().minusDays(30).toString();
        }
        while (true) {
            initThreadPoolParam(yiXinToJueCeThread);

            List<MarketingTransferSyncUser> marketingTransferSyncUserList =
                    yiXinProcessGetBaseDataService.getMarketingTransferSyncUserListBtoCtoI(tcId, apiCode,
                            type, requestDate, indexId);
            if (marketingTransferSyncUserList.isEmpty()) {
                break;
            }
            indexId = marketingTransferSyncUserList.get(marketingTransferSyncUserList.size() - 1).getId();
            List<List<MarketingTransferSyncUser>> partition = ListUtils.partition(marketingTransferSyncUserList, PARTITION);
            partition.forEach(users -> {
                List<MarketingTransferSyncUser> tpList = new ArrayList<>();
                tpList.addAll(users);
                yiXinToJueCeThread.submit(() -> threadDoProcess(tpList, actionType));
            });
        }
        yiXinToJueCeThread.shutdown();
        try {
            while (!yiXinToJueCeThread.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.info("等待线程池结束");
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    private void getMarketingTransferSyncUserListCJK(String actionType, String type, String tcId, String registerChannel) {
        Long indexId = 3000L;
        // 创建线程池
        ThreadPoolExecutor yiXinToJueCeThread = getYiXinToJueCeThread();
        String requestDate = LocalDate.now().toString();
        String apiCode = marketingCommonConfig.getYiXinGetTransferToJueCeApiCode();
        while (true) {
            initThreadPoolParam(yiXinToJueCeThread);
            List<MarketingTransferSyncUser> marketingTransferSyncUserList =
                    yiXinProcessGetBaseDataService.getMarketingTransferSyncUserListCJK(tcId, apiCode,
                            type, requestDate, indexId, registerChannel);
            if (marketingTransferSyncUserList.isEmpty()) {
                break;
            }
            indexId = marketingTransferSyncUserList.get(marketingTransferSyncUserList.size() - 1).getId();
            List<List<MarketingTransferSyncUser>> partition = ListUtils.partition(marketingTransferSyncUserList, PARTITION);
            partition.forEach(users -> {
                List<MarketingTransferSyncUser> tpList = new ArrayList<>();
                tpList.addAll(users);
                yiXinToJueCeThread.submit(() -> threadDoProcess(tpList, actionType));
            });
        }
        yiXinToJueCeThread.shutdown();
        try {
            while (!yiXinToJueCeThread.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.info("等待线程池结束");
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    /**
     * 情况 L
     *
     * @param tcId cid
     */
    private void pushMarketingTransferSyncUsersL(String actionType, String type, String tcId) {
        Long indexId = 3000L;
        // 创建线程池
        ThreadPoolExecutor yiXinToJueCeThread = getYiXinToJueCeThread();
        String requestDate = LocalDate.now().toString();
        String lastDate = LocalDate.now().minusDays(1).toString();
        String applyDtStart = lastDate + " 00:00:00";
        String applyDtEnd = lastDate + " 23:59:59";
        String apiCode = marketingCommonConfig.getYiXinGetTransferToJueCeApiCode();
        while (true) {
            initThreadPoolParam(yiXinToJueCeThread);
            log.warn("开始时间:{}", LocalDateTime.now());
            List<MarketingTransferSyncUser> marketingTransferSyncUserList =
                    yiXinProcessGetBaseDataService.getMarketingTransferSyncUserListL(tcId, apiCode,
                            requestDate, applyDtStart, applyDtEnd, indexId);
            log.warn("结束时间:{}", LocalDateTime.now());
            if(marketingTransferSyncUserList.isEmpty()){
                break;
            }
            log.warn("宜信推送决策,情况L剔除前数据量级:{}", marketingTransferSyncUserList.size());
            indexId = marketingTransferSyncUserList.get(marketingTransferSyncUserList.size() - 1).getId();
            List<List<MarketingTransferSyncUser>> partition = ListUtils.partition(marketingTransferSyncUserList, PARTITION);
            partition.forEach(users->{
                List<MarketingTransferSyncUser> tpList = new ArrayList<>();
                tpList.addAll(users);
                yiXinToJueCeThread.submit(() -> threadDoProcess(tpList, actionType));
            });
        }
        yiXinToJueCeThread.shutdown();
        try {
            while (!yiXinToJueCeThread.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.info("等待线程池结束");
            }
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }


    private void threadDoProcess(List<MarketingTransferSyncUser> marketingTransferSyncUserList, String actionType) {
        try {
            switch (actionType) {
                case "A":
                    yiXinProcessExcludeRuleData.excludeActionA(marketingTransferSyncUserList);
                    break;
                case "B":
                    yiXinProcessExcludeRuleData.excludeActionB(marketingTransferSyncUserList);
                    break;
                case "C":
                case "D":
                case "E":
                case "F":
                case "G":
                case "H":
                case "I":
                case "J":
                case "K":
                    yiXinProcessExcludeRuleData.excludeActionCtoI(marketingTransferSyncUserList);
                    break;
                case "L":
                    yiXinProcessExcludeRuleData.excludeActionL(marketingTransferSyncUserList);
                default:
                    break;
            }
            pushToJueCe(actionType, marketingTransferSyncUserList);
        }catch (Exception e){
            log.error(e.getMessage(), e);
        }

    }


    private void initThreadPoolParam(ThreadPoolExecutor yiXinToJueCeThread) {
        ThreadPoolAdjustmentUtil.adjustThreadPoolSize(yiXinToJueCeThread, marketingCommonConfig.getYiXinToJueCeTpNum());
    }

    private ThreadPoolExecutor getYiXinToJueCeThread() {
        return BrExecutors.getThreadPool(marketingCommonConfig.getYiXinToJueCeTpNum(), marketingCommonConfig.getYiXinToJueCeTpNum(),30);
    }


    private void pushToJueCe(String actionType, List<MarketingTransferSyncUser> e) {
        if (CollectionUtils.isNotEmpty(e)) {
            if ("L".equals(actionType)) {
                pushJcOfL(actionType, getMarketingTransferSyncUserCellsByValidityPeriods(e));
            } else {
                // A->K
                pushJc(actionType, getMarketingTransferSyncUserCellsByValidityPeriods(e));
            }
        }
    }

    /**
     * 过滤该集合有效期内数据并分别取最新一条
     * @param marketingTransferSyncUserList
     * @return 有效数据集合
     */
    private List<MarketingTransferSyncUserCell> getMarketingTransferSyncUserCellsByValidityPeriods
    (List<MarketingTransferSyncUser> marketingTransferSyncUserList) {
        String apiCode = marketingCommonConfig.getYiXinGetTransferToJueCeApiCode();
        Set<String> custNums = marketingTransferSyncUserList.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toSet());
        Map<String, SyncUserValidityPeriodsBO> periodsByCustNum = transferDataValidityPeriodService.getValidityPeriodsByCustNum(custNums, apiCode,
                new Date());
        return marketingTransferSyncUserList.stream().map((MarketingTransferSyncUser t) -> {
            SyncUserValidityPeriodsBO bo = periodsByCustNum.get(t.getCustNum());
            if (bo == null) {
                log.warn("宜信推决策，该custNum{}对应上传数据，不在有效期", t.getCustNum());
                return null;
            }

            List<MarketingSyncUser> syncUsers = bo.getSyncUsers();
            if (CollectionUtils.isEmpty(syncUsers)) {
                log.warn("宜信推决策，根据该custNum{}未找到上传数据！", t.getCustNum());
                return null;
            }

            MarketingSyncUser marketingSyncUser = syncUsers.get(0);
            MarketingTransferSyncUserCell marketingTransferSyncUserCell = new MarketingTransferSyncUserCell();
            MarketingTransferSyncUser transferSyncUser = new MarketingTransferSyncUser();
            BeanUtils.copyProperties(t, transferSyncUser);
            BeanUtils.copyProperties(t, marketingTransferSyncUserCell);
            marketingTransferSyncUserCell.setCell(marketingSyncUser.getCell());
            marketingTransferSyncUserCell.setTaskId(marketingSyncUser.getCusBatch());
            marketingTransferSyncUserCell.setUserType(marketingSyncUser.getUserType());
            marketingTransferSyncUserCell.setMarketingSyncUser(marketingSyncUser);
            marketingTransferSyncUserCell.setMarketingTransferSyncUser(transferSyncUser);
            return marketingTransferSyncUserCell;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * 情况A-K推送决策逻辑
     *
     * @param actionType                         情况说明
     * @param marketingTransferSyncUserCellLists 带电话的转化数据
     */
    private void pushJc(String actionType, List<MarketingTransferSyncUserCell> marketingTransferSyncUserCellLists) {
        log.warn("情况:{},去重前推送量级：{}", actionType, marketingTransferSyncUserCellLists.size());
        String apiCodeJc = marketingCommonConfig.getYiXinTransferToJueCeApiCode();
        // 2000 拆分一组
        List<List<MarketingTransferSyncUserCell>> partition = ListUtils.partition(marketingTransferSyncUserCellLists, PARTITION);
        partition.forEach((List<MarketingTransferSyncUserCell> m) -> {
            ArrayList<DataJoinLogDTO> logList = new ArrayList<>();
            ArrayList<PushMarketingUserDetailDTO> pushs = new ArrayList<>();
            // 决策数据初始化 pushs
            pushDataInit(actionType, apiCodeJc, m, logList, pushs);
            // 封装重试参数
            PolicyRetryByRuleSoleDTO retryByRuleDTO = getPolicyRetryByRuleSoleDTO(actionType, apiCodeJc, logList, pushs);
            // 推送决策方法
            methodRetryHandlerService.callPolicySoleData(retryByRuleDTO, 0);
        });

    }

    /**
     * 情况L推送决策逻辑
     * @param actionType 情况说明
     * @param marketingTransferSyncUserCellLists 带电话的转化数据
     */
    private void pushJcOfL(String actionType, List<MarketingTransferSyncUserCell> marketingTransferSyncUserCellLists) {
        log.warn("情况:{},不去重推送量级：{}", actionType, marketingTransferSyncUserCellLists.size());
        String apiCodeJc = marketingCommonConfig.getYiXinGetTransferToJueCeApiCode();
        // 2000 拆分一组
        List<List<MarketingTransferSyncUserCell>> partition = ListUtils.partition(marketingTransferSyncUserCellLists, PARTITION);
        partition.forEach((List<MarketingTransferSyncUserCell> list) -> {
            // 组装List<PushMarketingUserDetailDTO>
            List<PushMarketingUserDetailDTO> pushList = list.parallelStream().map(t -> {
                PushMarketingUserDetailDTO pushData = new PushMarketingUserDetailDTO();
                pushData.setCaseNumber(t.getCustNum());
                // log解密  md5加密
                String cell = DigestUtils.md5DigestAsHex(
                        BrCipherMaker.getInstance().decode(t.getCell()).getBytes(StandardCharsets.UTF_8));
                pushData.setPhone(cell);
                pushData.setVariables(variablesInit(t, cell, actionType));
                return pushData;
            }).collect(Collectors.toList());

            PushMarketingUserTaskInfoDTO taskInfoDTO = new PushMarketingUserTaskInfoDTO();
            taskInfoDTO.setMethod("caseAdd");
            taskInfoDTO.setBatchNumber(DateFormatUtils.format(new Date(), "yyyyMMdd") + "_" + actionType.toLowerCase() + "_" + apiCodeJc);
            taskInfoDTO.setStrategyCode(marketingCommonConfig.getYiXinToJueCeStrategyMapOfL().get(apiCodeJc));
            taskInfoDTO.setAccessNumber(UUID.randomUUID().toString());
            taskInfoDTO.setData(pushList);

            PushMarketingUserDTO pushMarketingUserDTO = new PushMarketingUserDTO();
            pushMarketingUserDTO.setApiCode(apiCodeJc);
            pushMarketingUserDTO.setJsonData(taskInfoDTO);

            PolicyRetryByRuleDTO retryByRuleDTO = new PolicyRetryByRuleDTO();
            // 转化表id
            retryByRuleDTO.setIds(list.stream().map(MarketingTransferSyncUserCell::getId).collect(Collectors.toList()));
            retryByRuleDTO.setInfoId(null);
            retryByRuleDTO.setPushMarketingUserDTO(pushMarketingUserDTO);
            // 推送决策方法
            methodRetryHandlerService.callPolicyDataYiXinToJueCe(retryByRuleDTO, 0);
        });

    }

    private PolicyRetryByRuleSoleDTO getPolicyRetryByRuleSoleDTO(String actionType,
                                                                 String apiCodeJc,
                                                                 ArrayList<DataJoinLogDTO> logList,
                                                                 ArrayList<PushMarketingUserDetailDTO> pushs) {
        PolicyRetryByRuleSoleDTO retryByRuleDTO = new PolicyRetryByRuleSoleDTO();
        retryByRuleDTO.setApiCode(apiCodeJc);
        retryByRuleDTO.setBatchNumber(DateFormatUtils.format(new Date(), "yyyyMMdd") + "_" + actionType.toLowerCase() + "_" + apiCodeJc);
        retryByRuleDTO.setData(pushs);
        retryByRuleDTO.setDetailLogList(logList);
        //传参去重
        retryByRuleDTO.setIsSole(Boolean.TRUE);
        if ("A".equals(actionType)) {
            retryByRuleDTO.setStrategyCode(marketingCommonConfig.getYiXinToJueCeStrategyMap().get(apiCodeJc));
            // apiCode,cust_num,status
            retryByRuleDTO.setSoleField(SoleFieldEnum.CUST_NUM_STATUS_SOLE.getValue());
            // 周一的数据 下周一推送判断的范围是周一到周日。
            retryByRuleDTO.setSoleDay(7);
        } else {
            // apiCode,custNum
            retryByRuleDTO.setSoleField(SoleFieldEnum.CUST_NUM_SOLE.getValue());
        }

        return retryByRuleDTO;
    }

    private void pushDataInit(String actionType,
                              String apiCodeJc,
                              List<MarketingTransferSyncUserCell> marketingTransferSyncUserCells,
                              ArrayList<DataJoinLogDTO> logList,
                              ArrayList<PushMarketingUserDetailDTO> pushs) {
        marketingTransferSyncUserCells.forEach((MarketingTransferSyncUserCell m) -> {
                    PushMarketingUserDetailDTO marketingUserDetailDTO = new PushMarketingUserDetailDTO();
                    marketingUserDetailDTO.setCaseNumber(m.getCustNum());
                    // log解密  md5加密
                    String cell = DigestUtils.md5DigestAsHex(
                            BrCipherMaker.getInstance().decode(m.getCell()).getBytes(StandardCharsets.UTF_8));
                    marketingUserDetailDTO.setPhone(cell);
                    marketingUserDetailDTO.setVariables(variablesInit(m, cell, actionType));
                    pushs.add(marketingUserDetailDTO);
                    // 把封装的日志插入到数组中
                    logList.add(methodRetryHandlerService.dataJoinLogFix(marketingUserDetailDTO, DistributeTypeEnum.POLICYDATA
                            , apiCodeJc, m.getCustNum(), m.getCell()
                            , null, DistributeSourceTypeEnum.TRANSFER, actionType, null));
                }
        );
    }

    private JSONObject variablesInit(MarketingTransferSyncUserCell marketingTransferSyncUserCell, String cell, String actionType) {

        MarketingSyncUser marketingSyncUser = marketingTransferSyncUserCell.getMarketingSyncUser();
        MarketingTransferSyncUser transferSyncUser = marketingTransferSyncUserCell.getMarketingTransferSyncUser();
        JSONObject jsonObject = JSONObject.parseObject(marketingSyncUser.getReserveField1());
        buildJson(jsonObject, marketingSyncUser);

        jsonObject.put("custNum", marketingTransferSyncUserCell.getCustNum());
        jsonObject.put("cell", cell);
        jsonObject.put("userType", marketingTransferSyncUserCell.getUserType());

        Map<String, Object> stringObjectMap = entityToMapWithBeanMap(transferSyncUser);
        mergeJSONObjects(jsonObject, stringObjectMap);

        switch (actionType) {
            case "D":
                jsonObject.put("unlentAmount", marketingTransferSyncUserCell.getUnlentAmount());
                JSONObject parsed = JSONObject.parseObject(marketingTransferSyncUserCell.getReserveField1());
                jsonObject.put("availableAmount", parsed.get("availableAmount"));
                break;
            case "E":
            case "F":
            case "G":
                JSONObject parse = JSONObject.parseObject(marketingTransferSyncUserCell.getReserveField1());
                jsonObject.put("raiseLimiType", parse.get("raiseLimiType"));
                jsonObject.put("raiseLimiSuccess", parse.get("raiseLimiSuccess"));
                jsonObject.put("recommendType", parse.get("recommendType"));
                break;
            case "H":
            case "I":
                JSONObject parseh = JSONObject.parseObject(marketingTransferSyncUserCell.getReserveField1());
                jsonObject.put("availableAmount", parseh.get("availableAmount"));
                break;
            case "L":
            default:
                break;
        }
        return jsonObject;
    }

    private JSONObject buildJson(JSONObject jsonObject, MarketingSyncUser syncUser) {
        jsonObject.put("cusBatch", emptyDefault(syncUser.getCusBatch()));
        jsonObject.put("requestBatch", emptyDefault(syncUser.getRequestBatch()));
        jsonObject.put("idCard", emptyDefault(get3keyValue(syncUser.getIdCard(), "idCard", CustomerTagsValue.PushJc3keyTypeEnum.MD5_ALL.getValue())));
        jsonObject.put("name", emptyDefault(get3keyValue(syncUser.getName(), "name", CustomerTagsValue.PushJc3keyTypeEnum.MD5_ALL.getValue())));
        jsonObject.put("groupType", emptyDefault(syncUser.getGroupType()));
        jsonObject.put("registerDate", emptyDefault(syncUser.getRegisterDate()));
        jsonObject.put("appletDate", emptyDefault(syncUser.getAppletDate()));
        jsonObject.put("taskId", emptyDefault(syncUser.getCusBatch()));
        return jsonObject;
    }

    public static Map<String, Object> entityToMapWithBeanMap(Object entity) {
        Map<String, Object> resultMap = new HashMap<>();
        ObjectMapper objectMapper = new ObjectMapper();

        BeanMap beanMap = BeanMap.create(entity);

        for (Object keyObj : beanMap.keySet()) {
            String key = keyObj.toString();
            Object value = beanMap.get(keyObj);

            if ("id".equals(key) || "createTime".equals(key) || "updateTime".equals(key)
                    || "tCid".equals(key) || "cid".equals(key) || "status".equals(key)
                    || "isTask".equals(key) || "taskTime".equals(key) || "isRepeat".equals(key)) {
                continue;
            }

            if (value == null || "".equals(value.toString().trim())) {
                continue;
            }

            // 处理 reserveField1 和 reserveField2
            if ("reserveField1".equals(key) || "reserveField2".equals(key)) {
                if (value instanceof String) {
                    try {
                        // 将 JSON 字符串解析为 Map 并合并到结果中
                        Map<String, Object> nestedMap = objectMapper.readValue(value.toString(), new TypeReference<Map<String, Object>>() {});
                        resultMap.putAll(nestedMap);
                    } catch (Exception e) {
                        log.warn("reserveField1或reserveField2不是 JSON 格式，跳过！, key :" + key);
                    }
                }
            } else {
                resultMap.put(key, value);
            }
        }
        return resultMap;
    }

    public JSONObject mergeJSONObjects(JSONObject jsonObject, Map<String, Object> map) {
        if (jsonObject == null) {
            return map == null ? new JSONObject() : new JSONObject(map);
        }
        if (map == null) {
            return jsonObject;
        }

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value == null || "".equals(value.toString())) {
                continue;
            }
            if (!jsonObject.containsKey(key)) {
                jsonObject.put(key, value);
            }
        }
        return jsonObject;
    }

    private String emptyDefault(String value) {
        return com.br.common.util.StringUtils.isNotEmpty(value) ? value : "";
    }

    private String get3keyValue(String content, String contentType, Integer encryptionType) {

        if (org.apache.commons.lang3.StringUtils.isBlank(content)) {
            return content;
        }

        if (CustomerTagsValue.PushJc3keyTypeEnum.MD5_ALL.getValue().equals(encryptionType)) {
            String decode = BrCipherMaker.getInstance().decode(content);
            return org.apache.commons.lang3.StringUtils.isNotBlank(decode) ? DigestUtils.md5DigestAsHex(decode.getBytes()) : content;
        }

        return null;
    }
}
