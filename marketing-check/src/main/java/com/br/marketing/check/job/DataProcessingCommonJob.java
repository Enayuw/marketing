package com.br.marketing.check.job;

import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.context.spring.DataProcessingContext;
import com.br.marketing.entity.LocalFile;
import com.br.marketing.entity.LocalFileExample;
import com.br.marketing.entity.dataProcess.DataProcessingConfig;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.mapper.dataProcess.DataProcessingConfigMapper;
import com.br.marketing.service.Impl.dataProcess.DataProcessAbstractProxy;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

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
 * @Description 数据处理通用流程（客户数据清洗等）
 * @Author hong.chen
 * @CreateTime 2023/11/11
 */
@Component
@Slf4j
public class DataProcessingCommonJob extends AbstractSimpleElasticJob{
    @Resource
    LocalFileMapper localFileMapper;

    @Resource
    DataProcessingConfigMapper dataProcessingConfigMapper;

    // 开启线程池
    // 判断apiCode和fileType是否配置了通用流程
    // 开启线程池，遍历b_pull_customer_file_data，查询要处理的数据，单个线程2000条，b_local_file表push_status记为1（任务开始）（子类可重写）
    // 组装参数：将客户字段映射到标准接口字段（子类必须实现）
    // 调用上传接口（子类必须实现）
    // 可以拓展结果处理（子类可重写）
    // b_local_file表push_status记为2（任务结束）
    // 线程池关闭（先关外层，再关内层.外层不关）
    // TODO 失败回滚
    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        // 自定义参数 todo
        String jobParameter = shardingContext.getJobParameter();
        // 遍历通用配置表
        List<DataProcessingConfig> configs =
                dataProcessingConfigMapper.selectByShardOrderByPriorityLevel(shardingContext.getShardingTotalCount(),
                        shardingContext.getShardingItems());

        List<DataProcessingConfig> tasks = new ArrayList<>();
        for (DataProcessingConfig config : configs) {
            // 根据apiCode和fileType查询b_local_file
            LocalFileExample localFileExample = new LocalFileExample();
            localFileExample.createCriteria().andStatusEqualTo("2").andCompleteEqualTo("1").andPushStatusEqualTo("0").andApiCodeEqualTo(config.getApiCode()).andFileTypeEqualTo(config.getFileType());
            List<LocalFile> localFiles = localFileMapper.selectByExample(localFileExample);

            if (CollectionUtils.isEmpty(localFiles)) {
                continue;
            }
            config.setLocalFileId(localFiles.get(0).getId());

            tasks.add(config);
        }


        ThreadPoolExecutor pool = BrExecutors.getThreadPool(5, 5);
        for (DataProcessingConfig task : tasks) {
            pool.execute(() -> process(task));
        }
//        pool.shutdown();
    }

    private void process(DataProcessingConfig task) {
        try {
            String proxyName = task.getProxyName();
            DataProcessAbstractProxy proxy = DataProcessingContext.getBean(proxyName);
            proxy.doProcess(task);
        } catch (Exception e) {
            log.error("");
        }
    }
}
