package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.client.intelligentcustomerservice.input.PolicyRetryByRuleDTO;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDTO;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserDetailDTO;
import com.br.marketing.client.intelligentcustomerservice.input.PushMarketingUserTaskInfoDTO;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUserCell;
import com.br.marketing.mapper.MarketingTransferInfoMapper;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.service.Impl.yixin.YiXinProcessExcludeRuleData;
import com.br.marketing.service.Impl.yixin.YiXinProcessGetBaseDataService;
import com.br.marketing.service.TransferDataValidityPeriodService;
import com.br.marketing.service.YiXinToJueCeMakeUpProcessService;
import com.br.marketing.service.ZnkfPushService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.MethodRetryHandlerService;
import com.br.marketing.util.ThreadPoolAdjustmentUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Description YiXinToJueCeMakeUpProcessServiceImpl
 * @Author hong.chen
 * @CreateTime 2023/08/09
 */
@Service
@Slf4j
public class YiXinToJueCeMakeUpProcessServiceImpl implements YiXinToJueCeMakeUpProcessService {
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

//    @Resource
//    private JobManager jobManager;

    @Resource
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;

    private static final Integer PARTITION = 2000;
    @Override
    public void doProcess(String param) {
        log.error("宜信推决策补推数据定时任务，启动");
        String apiCode = checkApiCode();
        Boolean pushBlackPhoneEnd = znkfPushService.isPushBlackPhoneEnd(apiCode, LocalDate.now().toString());
        if (!pushBlackPhoneEnd && LocalDateTime.now().getHour() < 11) {
            log.warn("未查询到黑名单结束标识！");
            return;
        }

        action(param,apiCode);
    }

    private void action(String param, String apiCode){
        // param样例：L,2023-08-08
        String tcId = tableCreateService.getTcId(apiCode);
        String[] split = param.split(",");
        String actionType = split[0];
        String makeupDateStr = split[1];
        LocalDate makeupDate = LocalDate.parse(makeupDateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        switch (actionType) {
            case "A":
//                Result<Long> resultA = actionFront(apiCodeTransfer, ACTONTFROUNTYPETREE.get(k));
//                if (!ResultCode.SUCCESS.getValue().equals(resultA.getCode())) {
//                    break;
//                }
//                pushMarketingTransferSyncUsersA(k, v, tcId);
//                updateActionFront(resultA);
//                break;
            case "B":
            case "D":
            case "E":
            case "F":
            case "G":
            case "H":
            case "I":
//                actionData(k, v, tcId, apiCodeTransfer);
                break;
            case "C":
//                actionDataCJK(k, v, tcId, apiCodeTransfer, "1");
                break;
            case "J":
//                actionDataCJK(k, v, tcId, apiCodeTransfer, "3");
                break;
            case "K":
//                actionDataCJK(k, v, tcId, apiCodeTransfer, "2");
                break;
            case "L":
                pushMarketingTransferSyncUsersL(actionType,tcId,makeupDate);
                break;
            default:
                log.warn("宜信转化数据推决策类型异常");
                break;
        }
    }

    private String checkApiCode() {
        String apiCodeTransfer = marketingCommonConfig.getYiXinGetTransferToJueCeApiCode();
        if (StringUtils.isBlank(apiCodeTransfer)) {
            log.error("宜信推送决策未配置apiCode");
        }
        return apiCodeTransfer;
    }

    private void pushMarketingTransferSyncUsersL(String actionType, String tcId, LocalDate makeupDate) {
        Long indexId = 3000l;
        // 创建线程池
        ThreadPoolExecutor yiXinToJueCeThread = getYiXinToJueCeThread();
        String requestDate = makeupDate.toString();
        String lastDate = makeupDate.minusDays(1).toString();
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
                yiXinToJueCeThread.submit(() -> threadDoProcess(tpList, actionType,makeupDate));
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
        log.error("宜信情况L补推送决策，补推完成！");
    }


    private void threadDoProcess(List<MarketingTransferSyncUser> marketingTransferSyncUserList, String actionType,LocalDate makeupDate) {
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
            pushToJueCe(actionType, marketingTransferSyncUserList,makeupDate);
        }catch (Exception e){
            log.error(e.getMessage(), e);
        }

    }


    private void initThreadPoolParam(ThreadPoolExecutor yiXinToJueCeThread) {
        ThreadPoolAdjustmentUtil.adjustThreadPoolSize(yiXinToJueCeThread, marketingCommonConfig.getYiXinToJueCeTpNum());
    }

    private ThreadPoolExecutor getYiXinToJueCeThread() {
        return BrExecutors.getThreadPool(marketingCommonConfig.getYiXinToJueCeTpNum(), marketingCommonConfig.getYiXinToJueCeTpNum());
    }

    private void pushToJueCe(String actionType, List<MarketingTransferSyncUser> e,LocalDate makeupDate) {
        if (CollectionUtils.isNotEmpty(e)) {
            if ("L".equals(actionType)) {
                pushJcOfL(actionType, getMarketingTransferSyncUserCells(e),makeupDate);
            } else {
                // A->K
//                pushJc(actionType, getMarketingTransferSyncUserCells(e));
            }
        }
    }

    private void pushJcOfL(String actionType, List<MarketingTransferSyncUserCell> marketingTransferSyncUserCellLists,LocalDate makeupDate) {
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
            // 时间转换
            String yyyyMMdd = makeupDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            taskInfoDTO.setBatchNumber(yyyyMMdd+ "_" + actionType.toLowerCase() + "_" + apiCodeJc);
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

    private JSONObject variablesInit(MarketingTransferSyncUserCell marketingTransferSyncUserCell, String cell, String actionType) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("custNum", marketingTransferSyncUserCell.getCustNum());
        jsonObject.put("cell", cell);
        jsonObject.put("userType", marketingTransferSyncUserCell.getUserType());
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

    private List<MarketingTransferSyncUserCell> getMarketingTransferSyncUserCells(List<MarketingTransferSyncUser> marketingTransferSyncUserList) {
        return marketingTransferSyncUserList.stream().map(jc -> transferDataValidityPeriodService.getNewValidityPeriodTransferData(jc, null))
                .collect(Collectors.toList()).stream().filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
