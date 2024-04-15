package com.br.marketing.service.Impl.jobmanager;

import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.LocalFile;
import com.br.marketing.entity.StraHisFile;
import com.br.marketing.entity.TransferActionFront;
import com.br.marketing.entity.TransferActionFrontExample;
import com.br.marketing.enums.JobStatusEnum;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.mapper.TransferActionFrontMapper;
import com.br.marketing.service.IJobManagerService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;


/**
 * 滴滴作业判断
 */
@Service
@Slf4j
public class JobManagerByScorePushServiceImpl implements IJobManagerService {


    @Autowired
    TransferActionFrontMapper transferActionFrontMapper;


    @Autowired
    MarketingCommonConfig marketingCommonConfig;

    @Autowired
    AlarmApiClient alarmApiClient;
    /**
     * 滴滴作业准入判断
     *
     * @param apiCode    客户编号
     * @param actionType 任务类型
     * @param actionDate 任务日期
     * @param taskArgs   定制判断逻辑所需的参数 0-localId
     * @return
     */
    @Override
    public Result<TransferActionFront> isAllowExecute(String apiCode, Integer actionType, String actionDate, Object... taskArgs) {
        if (StringUtils.isBlank(apiCode)
                || actionType == null
                || StringUtils.isBlank(actionDate)
                || taskArgs.length <= 0) {
            return new Result<>().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setMessage("参数错误");
        }

        if (Integer.valueOf(11).equals(actionType)) {
            return isDataAllowExe(apiCode, actionType, actionDate, taskArgs);
        }

        return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage("未找到实现");
    }


    @Override
    public Result<TransferActionFront> updateJobStatus(TransferActionFront task, Object... taskArgs) {
        if (task == null || taskArgs.length <= 0) {
            return new Result<>().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue()).setMessage("参数错误");
        }
        if (Integer.valueOf(11).equals(task.getActionType())) {
            return updateAllowJobStatus(task, taskArgs);
        }
        return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage("未找到更新的实现");
    }


    private Result<TransferActionFront> isDataAllowExe(String apiCode, Integer actionType, String actionDate, Object... taskArgs) {
        StraHisFile file = (StraHisFile) taskArgs[0];

        TransferActionFrontExample frontExample = new TransferActionFrontExample();
        frontExample.createCriteria()
                .andApiCodeEqualTo(apiCode)
                .andActionTypeEqualTo(actionType)
                .andRemarkLike(file.getId().toString().concat("-%"))
                .andIsDelEqualTo(Constants.DATA_VALID);
        List<TransferActionFront> transferActionFronts = transferActionFrontMapper.selectByExample(frontExample);

        if (transferActionFronts.size() > 0) {
            TransferActionFront actionFront = transferActionFronts.get(0);

            // 任务结束或者重试失败
            if (JobStatusEnum.FINISH.getValue().equals(actionFront.getStatus())
                    || JobStatusEnum.RETRY_FAIL.getValue().equals(actionFront.getStatus())) {
                return new Result<>().setCode(ResultCode.FAIL.getValue());
            }

            //region 任务重试 更新重试次数
            String remark = actionFront.getRemark();
            String[] split = remark.split("-");
            if (split.length < 2) {
                return new Result<>().setCode(ResultCode.FAIL.getValue());
            }
            Integer num = Integer.valueOf(split[1]);
            num = num + 1;
            TransferActionFront updateEntity = new TransferActionFront();
            updateEntity.setId(actionFront.getId());
            updateEntity.setRemark(file.getId().toString().concat("-").concat(num.toString()));
            actionFront.setRemark(file.getId().toString().concat("-").concat(num.toString()));
            transferActionFrontMapper.updateByPrimaryKeySelective(updateEntity);
            //endregion

            return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(actionFront);
        } else {

            // 任务执行记录
            TransferActionFront actionFront = new TransferActionFront();
            actionFront.setApiCode(apiCode);
            actionFront.setStatus(1);
            actionFront.setActionType(actionType);
            actionFront.setActionData(actionDate);
            actionFront.setCreateTime(new Date());
            actionFront.setRemark(file.getId().toString().concat("-0"));
            transferActionFrontMapper.insertSelective(actionFront);
            return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(actionFront);
        }
    }

    private Result<TransferActionFront> updateAllowJobStatus(TransferActionFront task, Object... taskArgs) {
        Boolean isRetry = Boolean.TRUE;
        Boolean resBoolean = (Boolean) taskArgs[0];
        TransferActionFront updateEntity = new TransferActionFront();
        updateEntity.setId(task.getId());
        if (resBoolean) {
            updateEntity.setStatus(JobStatusEnum.FINISH.getValue());
        } else {
            String[] split = task.getRemark().split("-");
            Integer num = Integer.valueOf(split[1]);
            Integer scorePushRetryNum = marketingCommonConfig.getScorePushRetryNum() != null ? marketingCommonConfig.getScorePushRetryNum() : 3;
            if (num >= scorePushRetryNum) {
                updateEntity.setStatus(JobStatusEnum.RETRY_FAIL.getValue());
                isRetry = Boolean.FALSE;
                alarmApiClient.sendAlarm(String.format("作业类型：%d,作业任务记录id：%d"
                        ,task.getActionType(),task.getId())
                        ,"跑分推送客戶重试多次仍然失败", AlarmSendCodeEnum.EXCEPTION_URGENT.getCode());
            } else {
                updateEntity.setStatus(JobStatusEnum.RETRY.getValue());
            }
        }
        transferActionFrontMapper.updateByPrimaryKeySelective(updateEntity);
        return new Result<>().setCode(resBoolean ? ResultCode.SUCCESS.getValue() : ResultCode.FAIL.getValue()).setMessage(isRetry ? "1" : "0");
    }
}
