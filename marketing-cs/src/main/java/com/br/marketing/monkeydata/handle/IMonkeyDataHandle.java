package com.br.marketing.monkeydata.handle;

import com.alibaba.fastjson.JSON;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.monkeydata.entity.InputDataCondition;
import com.br.marketing.monkeydata.entity.IterationResult;
import com.br.marketing.monkeydata.entity.commonobj.MonkeyContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public abstract class IMonkeyDataHandle<I, O, R extends InputDataCondition> {

    Logger log = LoggerFactory.getLogger(IMonkeyDataHandle.class);

    /**
     * 是否开启多线程
     *
     * @return
     */
    public Boolean isThread() {
        return false;
    }


    /**
     * 是否暂停
     *
     * @return
     */
    public Boolean isPause() {
        return false;
    }


    /**
     * 获取线程数
     *
     * @return
     */
    public Integer getThread() {
        return 5;
    }

    /**
     * 获取输入数据
     * 该方法返回Result<IterationResult<I, R>>对象，该对象会在(processData和resultAction)后续流程的前后判断是否进入下次循环
     * Result的code 为FAIL 在本次执行前就退出循环
     * IterationResult的isSingle 为False 在本次执行后就退出循环
     * 该方法抛出异常 会中断后续流程
     *
     * @return
     */
    public abstract Result<IterationResult<I, R>> getInputData(R condition);

    /**
     * 数据过程处理
     * 该方法会抛出异常
     *
     * @param inList
     * @return
     */
    public abstract Result<List<O>> processData(List<I> inList) throws Exception;

    /**
     * 数据标准输出
     * 该方法会抛出异常
     *
     * @param outputDataList
     * @return
     */
    public abstract Result resultAction(List<O> outputDataList);

    /**
     * 自定义执行方法
     * 实现该方法，并且该方法不存在返回null的情况,将代替模板流程。
     * 该方法抛出异常 会中断后续流程
     *
     * @param condition
     * @return
     */
    public Result customizedAction(R condition) {
        return null;
    }

    public Thread listenThreadPool(String taskId, ThreadPoolExecutor threadPoolExecutor) {
        Thread thread = new Thread(() -> {
            try {
                while (true) {
                    log.warn(String.format("任务：%s 的线程池运行状态 " +
                                    "活动线程数：%d" +
                                    "，核心线程数：%d" +
                                    "，最大线程数：%d" +
                                    "，队列量：%d"
                            , taskId
                            , threadPoolExecutor.getActiveCount()
                            , threadPoolExecutor.getCorePoolSize()
                            , threadPoolExecutor.getMaximumPoolSize()
                            , threadPoolExecutor.getQueue().size()));

                    Thread.sleep(60000L);

                }
            } catch (InterruptedException e) {
                log.warn(String.format("任务监听线程：%s 收到中断信号", taskId));
            }
        });
        thread.start();
        return thread;
    }

    public void removelistenThreadPool(Thread thread) {
        if (thread != null) {
            thread.interrupt();
        }
    }

    /**
     * 调用入口
     * 该方法返回Result对象
     * 先去执行自定义执行方法
     * 如自定义方法未实现 则执行该模板流程
     * 需注意 未开启多线成执行，getInputData，processData，resultAction 有异常，将退出执行，执行结果返回false
     * 开启多线成，线程内的异常只会记录日志 并不会阻断流程
     *
     * @param condition
     * @return
     */
    public final Result action(R condition) {
        try {
            long start = System.currentTimeMillis();
            String id = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + Math.random();
            log.warn("执行开始 执行id：{}，执行条件：{}", id, JSON.toJSONString(condition));
            Result resCustom = customizedAction(condition);
            if (resCustom != null) {
                log.warn("执行结束（自定义执行方法） 执行id：{}，执行耗时：{}", id, System.currentTimeMillis() - start);
                return resCustom;
            }
            Result res = new Result();
            ThreadPoolExecutor pool = null;
            Thread threadReport = null;
            if (isThread()) {
                pool = BrExecutors.getThreadPool(getThread(), getThread());
                threadReport = listenThreadPool(id, pool);
            }
            res.setCode(ResultCode.SUCCESS.getValue());
            for (; ; ) {
                Result<IterationResult<I, R>> inputRes = getInputData(condition);
                if (ResultCode.FAIL.getValue().equals(inputRes.getCode())) {
                    break;
                }
                if (isPause()) {
                    System.out.println("主线程暂停");
                    break;
                }
                condition = inputRes.getData().getInDatacondition();
                List inputDataList = inputRes.getData().getInputDataList();
                if (isThread() && pool != null) {
//                    Object context =  MonkeyContext.getProcessContext();
                    pool.submit(() -> {
//                        MonkeyContext.setProcessContext(context);
                        try {
                            if (!isPause()) {
                                Result<List<O>> outRes = processData(inputDataList);
                                if (ResultCode.SUCCESS.getValue().equals(outRes.getCode())) {
                                    List data = outRes.getData();
                                    Result result = resultAction(data);
                                    if (!ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                                        res.setCode(ResultCode.FAIL.getValue());
                                        log.warn(res.getMessage());
                                    }
                                }
                            } else {
                                System.out.println("线程暂停");
                            }
                        } catch (Exception ex) {
//                            MonkeyContext.clearProcessContext();
                            log.error(ex.getMessage(), ex);
                        }
//                        MonkeyContext.clearProcessContext();
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
                if (inputRes.getData().getIsSingle() != null && inputRes.getData().getIsSingle()) {
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
                removelistenThreadPool(threadReport);
            }
//            MonkeyContext.clearProcessContext();
            log.warn("执行结束 执行id：{}，执行耗时：{}", id, System.currentTimeMillis() - start);
            return res;
        } catch (Exception ex) {
            log.error("执行异常：" + ex.getMessage(), ex);
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage(ex.getMessage());
        }
    }

}
