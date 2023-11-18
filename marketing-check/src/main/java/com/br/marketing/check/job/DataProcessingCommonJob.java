package com.br.marketing.check.job;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.utils.StringUtils;
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
 * @Description 数据处理通用流程（客户数据清洗等）
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
        // 自定义参数 todo 用拓展字段 只处理某一天
        String jobParameter = shardingContext.getJobParameter();
        // 遍历通用配置表
        List<DataProcessingConfig> configs =
                dataProcessingConfigMapper.selectByShardOrderByPriorityLevel(shardingContext.getShardingTotalCount(),
                        shardingContext.getShardingItems());

        List<DataProcessingConfig> tasks = new ArrayList<>();
        for (DataProcessingConfig config : configs) {
            // 根据条件查询b_local_file
            // 查询b_local_file 状态为2（已完成）且文件推送状态为0（待推送）不判断complete
            LocalFileExample localFileExample = new LocalFileExample();
            LocalFileExample.Criteria criteria =
                    localFileExample.createCriteria().andStatusEqualTo("2").andPushStatusEqualTo("0").andApiCodeEqualTo(config.getApiCode());

            // 如果file_type中配置了fileName，则根据fileName和apiCode查询；否则，根据fileType和apiCode查询
            String fileTypeJson = config.getFileType();
            JSONObject jsonObject = JSON.parseObject(fileTypeJson);
            String fileName = jsonObject.getString("fileName");
            String fileType = jsonObject.getString("fileType");

            if (StringUtils.isNotEmpty(fileName)) {
                criteria.andFileNameLike(fileName+"%");
            } else {
                criteria.andFileTypeEqualTo(fileType);
            }

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

        for (DataProcessingConfig task : tasks) {
            process(task);
        }
    }

    /**
     * 根据前缀返回对应的排序顺序。对于其他前缀，放在列表末尾
     * @param task
     * @return
     */
    private static int getPrefixOrder(DataProcessingConfig task) {
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
