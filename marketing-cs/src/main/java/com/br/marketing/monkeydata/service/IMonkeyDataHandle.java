package com.br.marketing.monkeydata.service;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.monkeydata.entity.InputDataCondition;
import com.br.marketing.monkeydata.entity.IterationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public interface IMonkeyDataHandle<I, O, R extends InputDataCondition> {

    Logger log = LoggerFactory.getLogger(IMonkeyDataHandle.class);

    /**
     * 是否开启多线程
     *
     * @return
     */
    default Boolean isThread() {
        return false;
    }


    /**
     * 获取线程数
     *
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
    Result<IterationResult<I, R>> getInputData(R condition);

    /**
     * 数据过程处理
     *
     * @param inList
     * @return
     */
    Result<List<O>> processData(List<I> inList);

    /**
     * 数据标准输出
     *
     * @param outputDataList
     * @return
     */
    Result resultAction(List<O> outputDataList);


    /**
     * 调用入口
     *
     * @param condition
     * @return
     */
    default Result action(R condition) {
        Result res = new Result();
        ThreadPoolExecutor pool = null;
        if (isThread()) {
            pool = BrExecutors.getThreadPool(getThread(), getThread());
        }
        res.setCode(ResultCode.SUCCESS.getValue());
        for (; ; ) {
            Result<IterationResult<I, R>> inputRes = getInputData(condition);
            if (ResultCode.FAIL.getValue().equals(inputRes.getCode())) {
                break;
            }
            condition = inputRes.getData().getInDatacondition();
            List inputDataList = inputRes.getData().getInputDataList();
            if (isThread()&&pool!=null) {
                pool.submit(() -> {
                    Result<List<O>> outRes = processData(inputDataList);
                    if (ResultCode.SUCCESS.getValue().equals(outRes.getCode())) {
                        List data = outRes.getData();
                        Result result = resultAction(data);
                        if (!ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                            res.setCode(ResultCode.FAIL.getValue());
                            log.warn(res.getMessage());
                        }
                    }
                });
            } else {
                Result<List<O>> outRes = processData(inputDataList);
                if (ResultCode.SUCCESS.getValue().equals(outRes.getCode())) {
                    List data = outRes.getData();
                    Result result = resultAction(data);
                    if (!ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                        res.setCode(ResultCode.FAIL.getValue());
                        log.warn(res.getMessage());
                    }
                }
            }
            if (inputRes.getData().getIsSingle()!=null&&inputRes.getData().getIsSingle()) {
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
