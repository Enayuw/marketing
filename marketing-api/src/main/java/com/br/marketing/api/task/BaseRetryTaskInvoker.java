package com.br.marketing.api.task;

import com.br.marketing.common.exception.strategy.LoanTaskException;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Future;

/** 可重试任务执行器
 * 当任务执行时发生异常时，自动执行重试策略
 * 当执行三次皆出现异常时，执行报警和错误日志记录逻辑
 * @author Wang Weiwei
 * @since 2018/3/16
 * @param <T> the parameter of the class
 */
@Slf4j
public abstract class BaseRetryTaskInvoker<T> implements  LoanTaskInvoker<T>{
    private int retryNumber = 0;
    @Override
    public Future<T> invoker() {
        Future<T> tFuture = null;
        try {
            tFuture = retryInvoker();
        }catch (Exception e){
            retryNumber ++;
            try {
                Thread.sleep(10000);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
            if (retryNumber < 3){
                tFuture = invoker();
            }else {
                throw new LoanTaskException("策略子任务重试后异常", e);
            }
        }
        return tFuture;
    }


    /**
     * 可重试执行方法，该方法在发生异常时可被自动执行3次
     * @return  Future<T> 执行结果
     */
    abstract Future<T> retryInvoker();
}
