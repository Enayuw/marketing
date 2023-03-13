package com.br.marketing.service.Impl;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.entity.TransferActionFront;
import com.br.marketing.entity.TransferActionFrontExample;
import com.br.marketing.mapper.TransferActionFrontMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class JobManager {

    @Autowired
    TransferActionFrontMapper transferActionFrontMapper;

    /**
     * 获取推送记录
     *
     * @param apiCode
     * @param date
     * @param actionType
     * @return
     */
    public Result<TransferActionFront> getFrontData(String apiCode, String date, Integer actionType) {
        TransferActionFrontExample frontExample = new TransferActionFrontExample();
        frontExample.createCriteria()
                .andApiCodeEqualTo(apiCode)
                .andActionDataEqualTo(date)
                .andActionTypeEqualTo(actionType)
                .andIsDelEqualTo(1);

        List<TransferActionFront> transferActionFronts = transferActionFrontMapper.selectByExample(frontExample);

        if (transferActionFronts.size() > 1) {
            log.error(String.format("该推送日志当前有条 请检查apiCode:%s,data:%s,type:%s", apiCode, date, actionType));
            return new Result<>().setCode(ResultCode.FAIL.getValue());
        }

        TransferActionFront transferActionFront = transferActionFronts.get(0);
        if (new Integer(2).equals(transferActionFront.getStatus())) {
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage("今日作业已经执行结束");
        }
        if (transferActionFronts.size() > 0) {
            return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(transferActionFront);
        }

        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(null);
    }

    public Long saveFrontData(String apiCode, String date, Integer actionType) {
        TransferActionFront front = new TransferActionFront();
        front.setApiCode(apiCode);
        front.setStatus(1);
        front.setActionType(actionType);
        front.setActionData(date);
        front.setCreateTime(new Date());
        transferActionFrontMapper.insertSelective(front);
        return front.getId();
    }

    public void updateFrontDataStatus(Long id, Integer status) {
        TransferActionFront front = new TransferActionFront();
        front.setId(id);
        front.setStatus(status);
        transferActionFrontMapper.updateByPrimaryKeySelective(front);
    }
}
