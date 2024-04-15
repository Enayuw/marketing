package com.br.marketing.check.service;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.entity.Customer;
import com.br.marketing.entity.ScorePushCustomerConfig;
import com.br.marketing.entity.StraHisFile;
import com.br.marketing.enums.CallBackScoreResourceEnum;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;

import java.util.List;

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
 * @Date 2021/8/4 15:40
 * @Description:
 **/
public interface PushCustomerService {

    void push(ScorePushCustomerConfig pushCustomerConfig, StraHisFile file);

    /**
     * 获取回调的配置资源
     *
     * @param pushCustomerConfig
     * @param callBackScoreResourceEnum
     * @return
     */
    Integer getPushCustomerResource(ScorePushCustomerConfig pushCustomerConfig, CallBackScoreResourceEnum callBackScoreResourceEnum);

    /**
     * 获取跑分回调配置
     *
     * @return
     */
    List<ScorePushCustomerConfig> getScorePushConfigs();


    List<ScorePushCustomerConfig> getScorePushConfigs(Long fildId);

    StraHisFile getFile(Long fileId);

    String hasFileLock(Long fileId);

    void removeFileLock(Long fileId,String value);

    /**
     * 判断该跑分配置是否回调
     *
     * @param pushCustomerConfig
     * @return
     */
    Result<StraHisFile> isPush(ScorePushCustomerConfig pushCustomerConfig);

    void mockError(String type);

    void retry(Customer customer);
}
