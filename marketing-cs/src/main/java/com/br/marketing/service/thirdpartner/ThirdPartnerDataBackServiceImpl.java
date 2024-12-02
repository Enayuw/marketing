package com.br.marketing.service.thirdpartner;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.robotaiapi.RobotaiApiServiceClient;
import com.br.marketing.client.robotaiapi.input.CaseNumDTO;
import com.br.marketing.client.robotaiapi.input.RobotOutboundGeneralDTO;
import com.br.marketing.client.robotaiapi.input.ValidityChangeDTO;
import com.br.marketing.client.robotaiapi.output.RobotOutboundVo;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.entity.ThirdPartnerDataPassBackTask;
import com.br.marketing.entity.ThirdPartnerDataPassBackTaskExample;
import com.br.marketing.enums.ThirdPartnerDataPassBackTaskPushStatusEnum;
import com.br.marketing.mapper.MarketingSyncUserMapper;
import com.br.marketing.mapper.ThirdPartnerDataPassBackTaskMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.google.common.collect.Lists;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

public class ThirdPartnerDataBackServiceImpl implements ThirdPartnerDataBackService{
    @Resource
    ThirdPartnerDataPassBackTaskMapper thirdPartnerDataPassBackTaskMapper;

    @Resource
    MarketingSyncUserMapper marketingSyncUserMapper;
    
    @Resource
    MarketingCommonConfig marketingCommonConfig;

    @Autowired
    RobotaiApiServiceClient robotaiApiServiceClient;

    private final String METHOD_NAME = "updateBlackDateTime";

    @Override
    public void validChangeDataBack() {
        //1.查询【b_third_partner_data_pass_back_task】
        List<ThirdPartnerDataPassBackTask> taskList = queryTask();
        if(taskList.isEmpty()){
            return;
        }
        //遍历处理task
        taskList.forEach(task -> {
            processTask(task);
        });

    }

    /**
     * @description 处理任务主流程
     * @param task
     * @return void
     * @author hedongshuo
     * @date 2024/11/28 21:39
     **/
    private void processTask(ThirdPartnerDataPassBackTask task) {
        //1.获取参数
        String apiCode = task.getApiCode();
        String userType = task.getUserType();
        String appletDate = task.getAppletDate();
        String validStartDate = task.getValidStartDate();
        String validEndDate = task.getValidEndDate();
        //2.循环查询数据，分页2000，且一批2000调用接口
        HashMap<String, JSONObject> thirdPartnerApiMethodConfig = marketingCommonConfig.getThirdPartnerApiMethodConfig();
        JSONObject methodJson = thirdPartnerApiMethodConfig.get(METHOD_NAME);
        Integer pageSize = methodJson.getInteger("pageSize");
        Integer transferSize = methodJson.getInteger("transferSize");
        Integer threadSoleNum = methodJson.getInteger("threadSoleNum");
        RobotOutboundGeneralDTO<ValidityChangeDTO> dto = new RobotOutboundGeneralDTO<>();
        dto.setApiCode(apiCode);
        ValidityChangeDTO validityChangeDTO = new ValidityChangeDTO();
        validityChangeDTO.setMethod(METHOD_NAME);
        validityChangeDTO.setValidStartDate(validStartDate.concat(" 00:00:00"));
        validityChangeDTO.setValidEndDate(validEndDate.concat(" 23:59:59"));
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(threadSoleNum, threadSoleNum, 1);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        Long minId = null;
        for (; ; ) {
            List<MarketingSyncUser> list = marketingSyncUserMapper
                    .getCustNumByAppletDateAndUserType(apiCode, appletDate, userType, minId, pageSize);
            if (CollectionUtils.isEmpty(list)) {
                break;
            }
            minId = list.get(list.size() - 1).getId();
            List<CaseNumDTO> caseNumDTOS = list.stream().map(item -> {
                CaseNumDTO caseNumDTO = new CaseNumDTO();
                BeanUtils.copyProperties(item, caseNumDTO);
                return caseNumDTO;
            }).collect(Collectors.toList());
            pushData(dto, validityChangeDTO, caseNumDTOS, transferSize, threadPool, futures);
        }




    }

    /**
     * 推送数据
     *
     * @param dto
     * @param validityChangeDTO
     * @param caseNumDTOS
     * @param transferSize
     * @param threadPool
     * @param futures
     */
    private void pushData(RobotOutboundGeneralDTO<ValidityChangeDTO> dto,
                          ValidityChangeDTO validityChangeDTO, List<CaseNumDTO> caseNumDTOS,
                          Integer transferSize,
                          ThreadPoolExecutor threadPool, List<CompletableFuture<Void>> futures) {
        Lists.partition(caseNumDTOS, transferSize).forEach(caseNumDTOList -> {
            futures.add(CompletableFuture.runAsync(() -> {
                dto.setJsonData(validityChangeDTO);
                validityChangeDTO.setAccessNumber(UUID.randomUUID().toString());
                validityChangeDTO.setData(caseNumDTOList);
                RobotOutboundVo robotOutboundVo = robotaiApiServiceClient.pushRobotOutbound(dto, validityChangeDTO.getMethod());

            }, threadPool));
        });

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
        if (thirdPartnerDataPassBackTasks.isEmpty()) {
            return thirdPartnerDataPassBackTasks;
        }
        //有处理中的task，说明流程有异常，需要告警
        long executingCount = thirdPartnerDataPassBackTasks.stream()
                .filter(task -> task.getPushStatus() == ThirdPartnerDataPassBackTaskPushStatusEnum.EXECUTING.getPushStatus()).count();
        if (executingCount > 0) {
//            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.EXCEPTION_URGENT.getCode(), "三方数据有效期变更回传任务，有处理中的任务，请关注！"));
        }
        return thirdPartnerDataPassBackTasks.stream()
                .filter(task -> task.getPushStatus() == ThirdPartnerDataPassBackTaskPushStatusEnum.WAITED_EXECUTE.getPushStatus())
                .collect(Collectors.toList());
    }
}
