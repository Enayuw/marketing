package com.br.marketing.check.job;

import com.br.marketing.context.spring.DataProcessingContext;
import com.br.marketing.entity.LocalFile;
import com.br.marketing.entity.LocalFileExample;
import com.br.marketing.entity.dataProcess.DataProcessingConfig;
import com.br.marketing.enums.DataProcessByFileNameEnum;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.mapper.dataProcess.DataProcessingConfigMapper;
import com.br.marketing.service.Impl.dataProcess.DataProcessAbstractProxy;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

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

//    private static final LinkedHashMap<Integer, String> PROCESSlINK = new LinkedHashMap<>();
//
//    static {
//        PROCESSlINK.put(1,"original_caifu_");
//        PROCESSlINK.put(2,"original_daikuan_");
//        PROCESSlINK.put(3,"transform_");
//    }

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
            // todo 不判断complete
            localFileExample.createCriteria().andStatusEqualTo("2").andPushStatusEqualTo("0").andApiCodeEqualTo(config.getApiCode()).andFileTypeEqualTo(config.getFileType());
            List<LocalFile> localFiles = localFileMapper.selectByExample(localFileExample);

            if (CollectionUtils.isEmpty(localFiles)) {
                continue;
            }

            for (LocalFile localFile : localFiles) {
                DataProcessingConfig task = new DataProcessingConfig();
                BeanUtils.copyProperties(config, task);
                task.setLocalFile(localFile);
                tasks.add(task);
            }
        }

        // 根据文件名称特殊处理
        List<DataProcessingConfig> resultTasks = tasks.stream().map(t -> {
            String fileName = t.getLocalFile().getFileName();
            for (DataProcessByFileNameEnum anEnum : DataProcessByFileNameEnum.values()) {
                if (fileName.startsWith(anEnum.getFileNamePrefix())) {
                    t.setUrl(anEnum.getUrl());
                }
            }
            return t;
        }).collect(Collectors.toList());
        resultTasks.sort(Comparator.comparing(DataProcessingCommonJob::getPrefixOrder));

        for (DataProcessingConfig task : resultTasks) {
            process(task);
        }
//        pool.shutdown();
    }

    /**
     * 根据前缀返回对应的排序顺序。对于其他前缀，放在列表末尾
     * @param task
     * @return
     */
    private static int getPrefixOrder(DataProcessingConfig task){
        String fileName = task.getLocalFile().getFileName();
        for (DataProcessByFileNameEnum anEnum : DataProcessByFileNameEnum.values()) {
            if (fileName.startsWith(anEnum.getFileNamePrefix())) {
                // todo
//                task.setUrl(anEnum.getUrl());
                return anEnum.getOrder();
            }
        }

        return Integer.MAX_VALUE;
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
