package com.br.marketing.monkeydata.service;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.monkeydata.entity.InputData;
import com.br.marketing.monkeydata.entity.InputDataCondition;
import com.br.marketing.monkeydata.entity.IterationResult;
import com.br.marketing.monkeydata.entity.OutputData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public interface IMonkeyDataHandle {

    Logger log = LoggerFactory.getLogger(IMonkeyDataHandle.class);

    /**
     * 是否开启多线程
     * @return
     */
    default Boolean isThread() {
        return false;
    }


    /**
     * 获取线程数
     * @return
     */
    default Integer getThread() {
        return 5;
    }

    /**
     * 获取输入数据
     *
     * @return
     */
    Result<IterationResult> getInputData(InputDataCondition condition);

    /**
     * 数据过程处理
     *
     * @param inList
     * @return
     */
    Result<List> processData(List inList);

    /**
     * 数据标准输出
     *
     * @param outputDataList
     * @return
     */
    Result resultAction(List outputDataList);


    /**
     * 调用入口
     *
     * @param condition
     * @return
     */
    default Result action(InputDataCondition condition) {
        Result res = new Result();
        ThreadPoolExecutor pool = null;
        if (isThread()) {
            pool = BrExecutors.getThreadPool(getThread(), getThread());
        }
        res.setCode(ResultCode.SUCCESS.getValue());
        for (; ; ) {
            Result<IterationResult> inputRes = getInputData(condition);
            if (ResultCode.FAIL.getValue().equals(inputRes.getCode())) {
                break;
            }
            condition = inputRes.getData().getInputDataCondition();
            List<InputData> inputDataList = inputRes.getData().getInputDataList();
            if (!isThread()) {
                pool.submit(() -> {
                    Result<List<OutputData>> outRes = processData(inputDataList);
                    if (ResultCode.SUCCESS.getValue().equals(outRes.getCode())) {
                        List<OutputData> data = outRes.getData();
                        Result result = resultAction(data);
                        if (!ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                            res.setCode(ResultCode.FAIL.getValue());
                            log.warn(res.getMessage());
                        }
                    }
                });
            } else {
                Result<List<OutputData>> outRes = processData(inputDataList);
                if (ResultCode.SUCCESS.getValue().equals(outRes.getCode())) {
                    List<OutputData> data = outRes.getData();
                    Result result = resultAction(data);
                    if (!ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                        res.setCode(ResultCode.FAIL.getValue());
                        log.warn(res.getMessage());
                    }
                }
            }
            if (inputRes.getData().getIsSingle()) {
                break;
            }
        }
        if (isThread()) {
            pool.shutdown();
            try {
                while (!pool.awaitTermination(10L, TimeUnit.SECONDS)) {

                }
            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
            }
        }
        return res;
    }

}
