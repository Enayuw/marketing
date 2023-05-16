package com.br.marketing.service.Impl.jobmanager;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.entity.TransferActionFront;
import com.br.marketing.service.IJobManagerService;
import org.springframework.stereotype.Service;


/**
 * 默认实现
 */
@Service
public class JobManagerServiceImpl implements IJobManagerService {

    @Override
    public Result<TransferActionFront> isAllowExecute(String apiCode, Integer actionType, String actionDate, Object... taskArgs) {
        return new Result<>().setCode(ResultCode.SUCCESS.getValue());
    }

    @Override
    public Result<TransferActionFront> updateJobStatus(TransferActionFront task, Object... taskArgs) {
        return null;
    }
}
