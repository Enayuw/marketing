package com.br.marketing.api.aspect.log;

import com.br.marketing.api.entities.api.StrategyApiContext;
import org.aspectj.lang.ProceedingJoinPoint;

import javax.servlet.http.HttpServletRequest;

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
public class GetParamter {
    private ProceedingJoinPoint joinPoint;
    private StrategyApiContext context;
    private String jsonData;
    private HttpServletRequest request;

    /**
     * 构造方法
     * @param joinPoint joinPoint
     */
    public GetParamter(ProceedingJoinPoint joinPoint) {
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
     * jsonData get 方法
     * @return String jsonData
     */
    public String getJsonData() {
        return jsonData;
    }
    /**
     * request get 方法
     * @return HttpServletRequest request
     */
    public HttpServletRequest getRequest() {
        return request;
    }
    /**
     * invoke方法
     * @return GetParamter GetParamter
     */
    public GetParamter invoke() {
        Object[] args = joinPoint.getArgs();
        for (int i = 0; i < args.length; i++) {
            if(i==0){
                context=(StrategyApiContext) args[i];
            }else if(i==1){
                jsonData = (String) args[i];
            }else if(i==2){
                request = (HttpServletRequest) args[i];
            } else{
                break;
            }
        }
        return this;
    }
}
