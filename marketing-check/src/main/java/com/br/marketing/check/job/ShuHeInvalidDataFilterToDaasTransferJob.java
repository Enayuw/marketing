package com.br.marketing.check.job;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.check.service.ShuHeTransferService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 数禾失效数据过虑到Dass转化接口调度任务
 *
 * @author Guo Zeqiang
 * @dateTime 2022/7/13 10:018
 */
@Component
@Slf4j
public class ShuHeInvalidDataFilterToDaasTransferJob extends AbstractSimpleElasticJob {

    @Resource
    private ShuHeTransferService shuHeTransferService;

    /**
     * 其中“time”可不填写，默认“20:00:00”
     * JobParameter格式：{"J":{"apiCode":"code","orgname":"J","time":"20:00:00"},"Q":{"apiCode":"code","orgname":"Q","time":"20:00:00"},"K":{"apiCode":"code","orgname":"K","time":"20:00:00"}}
     * eg:
     * {"促申完":{"apiCode":"7410785","orgname":"shuheshenwan","time":"20:00:00"}}
     */
    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        long start = System.currentTimeMillis();
        Map<String, Map<String, String>> typeMap = new HashMap<>(8);
        Map<String, String> infoMap = new HashMap<>(4);
        infoMap.put("apiCode", "3710004");
        infoMap.put("orgname", "shuheshenwan");
        infoMap.put("time", "20:00:00");
        typeMap.put("促申完", infoMap);
        parseParam(context.getJobParameter(), typeMap);
        shuHeTransferService.invalidDataFilterToDaasTransfer(typeMap);
        long end = System.currentTimeMillis();
        log.warn("【数禾失效数据过虑到Dass转化接口调度任务】调度结束，耗时:{},分片:{},typeMap:{}", end - start
                , context.getShardingItemParameters(), typeMap);

    }

    /**
     * 2022/7/13 15:40
     * 解析参数
     */
    private void parseParam(String param, Map<String, Map<String, String>> typeMap) {
        if (StringUtils.isNotBlank(param)) {
            try {
                Map<String, Map<String, String>> paramMap = JSONObject.parseObject(param
                        , new TypeReference<Map<String, Map<String, String>>>() {
                        }.getType());
                typeMap.putAll(paramMap);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

}
