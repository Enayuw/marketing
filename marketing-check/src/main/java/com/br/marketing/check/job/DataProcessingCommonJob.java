package com.br.marketing.check.job;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.context.spring.DataProcessingContext;
import com.br.marketing.entity.LocalFile;
import com.br.marketing.entity.LocalFileExample;
import com.br.marketing.entity.dataProcess.DataProcessingConfig;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.mapper.dataProcess.DataProcessingConfigMapper;
import com.br.marketing.service.Impl.dataProcess.DataProcessAbstractProxy;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import com.marketingkit.tracking.model.indicator.DataFlowDirection;
import com.marketingkit.tracking.service.TrackingService;
import com.marketingkit.tracking.util.TrackingContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

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
 * @Description 文件数据处理通用流程（客户数据清洗等）
 * @Author hong.chen
 * @CreateTime 2023/11/11
 */
@Component
@Slf4j
public class DataProcessingCommonJob extends AbstractSimpleElasticJob {
    @Resource
    LocalFileMapper localFileMapper;

    @Resource
    DataProcessingConfigMapper dataProcessingConfigMapper;

    @Override
    public void process(JobExecutionMultipleShardingContext shardingContext) {
        // 遍历通用配置表，相同fileType分在一个分片上，并将遍历结果按照优先级排序
        List<DataProcessingConfig> configs =
                dataProcessingConfigMapper.selectByShardOrderByPriorityLevel(shardingContext.getShardingTotalCount(),
                        shardingContext.getShardingItems());

        // 构建满足处理条件的任务列表
        List<DataProcessingConfig> tasks = getTasks(configs);

        // 按序执行
        start(tasks);
    }

    private List<DataProcessingConfig> getTasks(List<DataProcessingConfig> configs) {
        List<DataProcessingConfig> tasks = new ArrayList<>();
        for (DataProcessingConfig config : configs) {
            // 根据条件查询b_local_file
            // 查询b_local_file 状态为2（已完成）且文件推送状态为0（待推送）不判断complete
            LocalFileExample localFileExample = new LocalFileExample();
            LocalFileExample.Criteria criteria =
                    localFileExample.createCriteria().andStatusEqualTo("2").andPushStatusEqualTo("0").andApiCodeEqualTo(config.getApiCode());

            // 如果file_type中配置了fileName，则根据fileName和apiCode查询；否则，根据fileType和apiCode查询
            queryByFileNameOrFileType(config, criteria);
            List<LocalFile> localFiles = localFileMapper.selectByExample(localFileExample);

            if (CollectionUtils.isEmpty(localFiles)) {
                continue;
            }

            // 同一文件名前缀或同一类型可能查到多个文件，也按照priority_level排序
            for (LocalFile localFile : localFiles) {
                DataProcessingConfig task = new DataProcessingConfig();
                BeanUtils.copyProperties(config, task);
                task.setLocalFile(localFile);
                tasks.add(task);
            }
        }
        return tasks;
    }

    private void queryByFileNameOrFileType(DataProcessingConfig config, LocalFileExample.Criteria criteria) {
        String fileTypeJson = config.getFileType();
        JSONObject jsonObject = JSON.parseObject(fileTypeJson);
        String fileName = jsonObject.getString("fileName");
        String fileType = jsonObject.getString("fileType");

        criteria.andFileTypeEqualTo(fileType);
        // 根据文件名前缀模糊匹配
        if (StringUtils.isNotEmpty(fileName)) {
            criteria.andFileNameLike(fileName + "%");
        }
    }

    private void start(List<DataProcessingConfig> tasks) {
        for (DataProcessingConfig task : tasks) {
            log.warn("数据处理任务开始，apiCode:{}，fileName:{}", task.getApiCode(), task.getLocalFile().getFileName());
            process(task);
            log.warn("数据处理任务结束，apiCode:{}，fileName:{}", task.getApiCode(), task.getLocalFile().getFileName());
        }
    }

    private void process(DataProcessingConfig task) {
        String proxyName = task.getProxyName();
        DataProcessAbstractProxy proxy = DataProcessingContext.getBean(proxyName);

        try {
            proxy.doProcess(task);
        } catch (Exception e) {
            log.error("数据处理任务异常,配置表id:{},apiCode:{}", task.getId(), task.getApiCode(), e.getMessage(), e);
        }
    }

}
