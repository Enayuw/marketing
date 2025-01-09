package com.br.marketing.service.thirdpartner;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.robotaiapi.RobotaiApiServiceClient;
import com.br.marketing.client.robotaiapi.input.CaseNumDTO;
import com.br.marketing.client.robotaiapi.input.RobotOutboundGeneralDTO;
import com.br.marketing.client.robotaiapi.input.ValidityChangeDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.ThirdPartnerDataPassBackLog;
import com.br.marketing.entity.ThirdPartnerDataPassBackTask;
import com.br.marketing.entity.ThirdPartnerDataPassBackTaskExample;
import com.br.marketing.enums.ThirdPartnerDataPassBackLogStatusEnum;
import com.br.marketing.enums.ThirdPartnerDataPassBackTaskPushStatusEnum;
import com.br.marketing.mapper.MarketingSyncUserMapper;
import com.br.marketing.mapper.ThirdPartnerDataPassBackLogMapper;
import com.br.marketing.mapper.ThirdPartnerDataPassBackTaskMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.strategy.MethodRetryHandlerService;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ThirdPartnerDataBackServiceImpl implements ThirdPartnerDataBackService{
    @Resource
    ThirdPartnerDataPassBackTaskMapper thirdPartnerDataPassBackTaskMapper;

    @Resource
    MarketingSyncUserMapper marketingSyncUserMapper;
    
    @Resource
    MarketingCommonConfig marketingCommonConfig;

    @Autowired
    RobotaiApiServiceClient robotaiApiServiceClient;

    @Resource
    private MethodRetryHandlerService methodRetryHandlerService;

    @Resource
    ThirdPartnerDataPassBackLogMapper thirdPartnerDataPassBackLogMapper;

    private final String METHOD_NAME = "updateBlackDateTime";

    @Override
    public void validChangeDataBack() {
        //1.查询【b_third_partner_data_pass_back_task】
        List<ThirdPartnerDataPassBackTask> taskList = queryTask();
        //2.遍历处理task
        taskList.forEach(task -> processTask(task));
    }

    /**
     * @description 处理任务主流程
     * @param task
     * @return void
     * @author hedongshuo
     * @date 2024/11/28 21:39
     **/
    private void processTask(ThirdPartnerDataPassBackTask task) {
        //TODO考虑job续跑，先处理执行中的任务
        //1.更新task的状态 = 1-执行中
        ThirdPartnerDataPassBackTask taskForUpdate = new ThirdPartnerDataPassBackTask();
        taskForUpdate.setId(task.getId());
        taskForUpdate.setPushStatus(ThirdPartnerDataPassBackTaskPushStatusEnum.EXECUTING.getPushStatus());
        taskForUpdate.setUpdateTime(new Date());
        thirdPartnerDataPassBackTaskMapper.updateByPrimaryKeySelective(taskForUpdate);
        //2.循环查询数据，分页2000，且一批2000调用接口
        HashMap<String, JSONObject> thirdPartnerApiMethodConfig = marketingCommonConfig.getThirdPartnerApiMethodConfig();
        JSONObject methodJson = thirdPartnerApiMethodConfig.get(METHOD_NAME);
        RobotOutboundGeneralDTO<ValidityChangeDTO> dto = new RobotOutboundGeneralDTO<>();
        dto.setApiCode(task.getApiCode());
        ValidityChangeDTO validityChangeDTO = new ValidityChangeDTO();
        validityChangeDTO.setMethod(METHOD_NAME);
        validityChangeDTO.setValidStartDate(task.getValidStartDate().concat(" 00:00:00"));
        validityChangeDTO.setValidEndDate(task.getValidEndDate().concat(" 23:59:59"));
        ThreadPoolExecutor threadPool = BrExecutors
                .getThreadPool(methodJson.getInteger("threadSoleNum"), methodJson.getInteger("threadSoleNum"));
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        Boolean isFirstQuery = true;
        for (; ; ) {
            //2.1查询数据
            List<MarketingSyncUser> list = marketingSyncUserMapper
                    .getCustNumByAppletDateAndUserTypetikv_(
                            task.getApiCode(), task.getAppletDate(), task.getUserType(), task.getId(), methodJson.getInteger("pageSize"));
            if (CollectionUtils.isEmpty(list)) {
                if (isFirstQuery) {
                    //如果第一次查询就为空，则更新task的状态 = 2-执行成功
                    taskForUpdate.setPushStatus(ThirdPartnerDataPassBackTaskPushStatusEnum.FINISHED.getPushStatus());
                    thirdPartnerDataPassBackTaskMapper.updateByPrimaryKeySelective(taskForUpdate);
                }
                break;
            }
            isFirstQuery = false;
            Lists.partition(list, methodJson.getInteger("transferSize")).forEach(part -> {
                //2.2插入日志
                List<ThirdPartnerDataPassBackLog> passLogs = part.stream().map(syncUser -> {
                    String orgApiCode = JSON.parseObject(syncUser.getReserveField1()).getString("orgApiCode");
                    ThirdPartnerDataPassBackLog passLog = new ThirdPartnerDataPassBackLog();
                    passLog.setSyncUserId(syncUser.getId());
                    passLog.setOrgApiCode(orgApiCode);
                    passLog.setCustNum(syncUser.getCustNum());
                    passLog.setCell(syncUser.getCell());
                    passLog.setTaskId(task.getId());
                    passLog.setStatus(ThirdPartnerDataPassBackLogStatusEnum.PUSHING.getStatus());
                    passLog.setExtend("");
                    return passLog;
                }).collect(Collectors.toList());
                thirdPartnerDataPassBackLogMapper.saveBatch(passLogs);
                //2.3提取日志id
                List<Long> passLogIds = passLogs.stream()
                        .map(ThirdPartnerDataPassBackLog::getId).collect(Collectors.toList());
                //2.4构建推送数据
                List<CaseNumDTO> caseNumDTOS = passLogs.stream().map(passLog -> {
                    CaseNumDTO caseNumDTO = new CaseNumDTO();
                    caseNumDTO.setApiCode(passLog.getOrgApiCode());
                    caseNumDTO.setCaseNum(passLog.getCustNum());
                    caseNumDTO.setCell(passLog.getCell());
                    return caseNumDTO;
                }).collect(Collectors.toList());
                //2.5推送数据
                pushData(dto, validityChangeDTO, caseNumDTOS, threadPool, futures, passLogIds);
            });
        }
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        threadPool.shutdown();
        try {
            while (!threadPool.awaitTermination(10L, TimeUnit.SECONDS)) {
                log.warn("三方数据有效期变更回传：线程池关闭");
            }
        } catch (InterruptedException e) {
            threadPool.shutdownNow();
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.PUSHING_CUSTOMERERROR.getCode(), e.getMessage()
                    , "三方数据有效期变更回传任务：线程池关闭结束异常！"), e);
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 推送数据
     *
     * @param dto
     * @param validityChangeDTO
     * @param caseNumDTOS
     * @param threadPool
     * @param futures
     * @param passLogIds
     */
    private void pushData(RobotOutboundGeneralDTO<ValidityChangeDTO> dto,
                          ValidityChangeDTO validityChangeDTO,
                          List<CaseNumDTO> caseNumDTOS,
                          ThreadPoolExecutor threadPool,
                          List<CompletableFuture<Void>> futures,
                          List<Long> passLogIds) {
        futures.add(CompletableFuture.runAsync(() -> {
            try{
                dto.setJsonData(validityChangeDTO);
                String accessNumber = UUID.randomUUID().toString();
                validityChangeDTO.setAccessNumber(accessNumber);
                validityChangeDTO.setData(caseNumDTOS);
                Result<String> result = methodRetryHandlerService.callRobotOutbound(dto, 0, validityChangeDTO.getMethod());
                Integer status;
                if (result.isSuccess()) {
                    status = ThirdPartnerDataPassBackLogStatusEnum.PUSH_SUCCESS.getStatus();
                } else {
                    status = ThirdPartnerDataPassBackLogStatusEnum.PUSH_FAIL.getStatus();
                }
                thirdPartnerDataPassBackLogMapper.updateStatusByIds(passLogIds, status);
            } catch (Exception e) {
                log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_CUSTOMERERROR.getCode()
                        , "客服接口保存日志失败，method：" + validityChangeDTO.getMethod() + " -- " + JSON.toJSONString(caseNumDTOS)));
            }
        }, threadPool));
    }

    /**
     * @description 查询所有未执行的任务
     * @return java.util.List<com.br.marketing.entity.ThirdPartnerDataPassBackTask>
     * @author hedongshuo
     * @date 2024/11/28 21:00
     **/
    private List<ThirdPartnerDataPassBackTask> queryTask() {
        ThirdPartnerDataPassBackTaskExample example = new ThirdPartnerDataPassBackTaskExample();
        example.createCriteria()
                .andPushStatusNotEqualTo(ThirdPartnerDataPassBackTaskPushStatusEnum.FINISHED.getPushStatus())
                .andIsDeletedEqualTo(0);
        example.setOrderByClause("create_time asc");
        List<ThirdPartnerDataPassBackTask> thirdPartnerDataPassBackTasks = thirdPartnerDataPassBackTaskMapper.selectByExample(example);
        //有处理中的task，说明流程有异常，需要告警
        long executingCount = thirdPartnerDataPassBackTasks.stream()
                .filter(task -> task.getPushStatus() == ThirdPartnerDataPassBackTaskPushStatusEnum.EXECUTING.getPushStatus()).count();
        if (executingCount > 0) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_CUSTOMERERROR.getCode(),
                    "三方数据有效期变更回传任务，有处理中的任务，请关注！"));
        }
        return thirdPartnerDataPassBackTasks;
    }
}
