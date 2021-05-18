package com.br.marketing.api.task;

import java.util.concurrent.Future;

/** 贷中任务执行器接口
 * @author Wang Weiwei
 * @since 2018/3/16
 * @param <T> the parameter of the class
 */
public interface LoanTaskInvoker<T> {

    /**
     * 执行运算任务
     * @return 执行结果
     */
    Future<T> invoker();
}
