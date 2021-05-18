package com.br.marketing.api.aspect.strategy.batch.put;


import com.br.marketing.api.entities.api.Result;
import com.br.marketing.api.entities.api.StrategyApiContext;
import org.aspectj.lang.ProceedingJoinPoint;

/**
 * //				    _ooOoo_
 * //				   o8888888o
 * //				   88" . "88
 * //				   (| -_- |)
 * //				   O\  =  /O
 * //			    ____/`---'\____
 * //			  .'  \\|     |//  `.
 * //		     /  \\|||  :  |||//  \
 * //		    /  _|||||--:--|||||_  \
 * //		    | / | \\\  -  /// | \ |
 * //		    | \_|  ''\-:-/''  |_/ |
 * //		    \  .-\__  `-`  ___/-. /
 * //		  ___`...'  /--.--\  '...`___
 * //	   ."" '< `.___\_<|>_/___.'  >' "".
 * //	   | | : `- \`.;`\ _ /`;.`/ -` : | |
 * //	    \ \ `-.  \_ __\ /__ _/  .-` / /
 * // ======`-.____`-.____\____/.-`____.-`======
 * //				    `=---='
 * //^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
 * //			  Buddha Bless, No Bug !
 *
 * @Author xiaoxin.pang
 * @Date 2020/8/15 14:10
 * @Description:
 **/
public class Paramter {
    private ProceedingJoinPoint joinPoint;
    private StrategyApiContext context;
    private Result result;

    /**
     * 构造方法
     * @param joinPoint joinPoint
     */
    public Paramter(ProceedingJoinPoint joinPoint) {
        this.joinPoint = joinPoint;
    }

    /**
     * context get方法
     * @return StrategyApiContext StrategyApiContext
     */
    public StrategyApiContext getContext() {
        return context;
    }

    /**
     * Result get 方法
     * @return Result Result
     */
    public Result getResult() {
        return result;
    }

    /**
     * invoke方法
     * @return GetParamter GetParamter
     */
    public Paramter invoke() {
        Object[] args = joinPoint.getArgs();
        for (int i = 0; i < args.length; i++) {
            if(i==0){
                context=(StrategyApiContext) args[i];
            }else if(i==1){
                result = (Result) args[i];
            } else{
                break;
            }
        }
        return this;
    }
}
