package com.br.marketing.check.utils;

import com.br.marketing.common.utils.EncodeUtil;
import com.br.marketing.entity.RequestLog;
import com.br.marketing.rpcclient.RpcClientProxy;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

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
 * @Date 2020/6/1 13:13
 * @Description:
 **/
@Slf4j
public class MomUtil {

    public static void sendMom(String apiCode,String requestStr,String responseStr,Long costTime,String swiftNumber,String resultCode){
        RequestLog requestLog = new RequestLog();
        requestLog.setRequestTime(new Date());
        requestLog.setApiCode(apiCode);
        requestLog.setRequestStr(EncodeUtil.encodeDefault(requestStr));
        requestLog.setResponseStr(responseStr);
        requestLog.setResponseTime(new Date());
        requestLog.setCostTime(costTime);
        requestLog.setSwiftNumber(swiftNumber);
        requestLog.setUrl("push");
        requestLog.setCode(resultCode);
        RpcClientProxy.sendRequestLog(requestLog);
    }
}
