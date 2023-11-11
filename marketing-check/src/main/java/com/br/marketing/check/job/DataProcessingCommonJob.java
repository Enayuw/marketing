package com.br.marketing.check.job;

import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;

/**
 *  				    _ooOoo_
 *  				   o8888888o
 *  				   88" . "88
 *  				   (| -_- |)
 *  				   O\  =  /O
 *  			    ____/`---'\____
 *  			  .'  \\|     |//  `.
 *  		     /  \\|||  :  |||//  \
 *  		    /  _|||||--:--|||||_  \
 *  		    | / | \\\  -  /// | \ |
 *  		    | \_|  ''\-:-/''  |_/ |
 *  		    \  .-\__  `-`  ___/-. /
 *  		  ___`...'  /--.--\  '...`___
 *  	   ."" '< `.___\_<|>_/___.'  >' "".
 *  	   | | : `- \`.;`\ _ /`;.`/ -` : | |
 *  	    \ \ `-.  \_ __\ /__ _/  .-` / /
 *   ======`-.____`-.____\____/.-`____.-`======
 *  				    `=---='
 *  ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
 *  			  Buddha Bless, No Bug !
 *
 * @Description 数据清洗通用流程
 * @Author hong.chen
 * @CreateTime 2023/11/11
 */
public class DataProcessingCommonJob extends AbstractSimpleElasticJob{
    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        // 遍历b_local_file
        // 开启线程池
          // 判断apiCode和fileType是否配置了通用流程
          // 开启线程池，遍历b_pull_customer_file_data，查询要处理的数据，单个线程2000条，b_local_file表push_status记为1（任务开始）（子类可重写）
            // 组装参数：将客户字段映射到标准接口字段（子类必须实现）
            // 调用上传接口（子类必须实现）
            // 可以拓展结果处理（子类可重写）
          // b_local_file表push_status记为2（任务结束）
        // 线程池关闭（先关外层，再关内层）
    }
}
