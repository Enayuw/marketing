package com.br.marketing.monkey.job.smy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.service.smy.ISmyPushBlackListService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;

/**
 * D20241219萨摩耶黑名单传输（sftp→api）-3710196
 * https://c.100credit.cn/pages/viewpage.action?pageId=190665900
 *
 * @Author bin.li1
 * @date 2024-12-23
 */
@Component
@Slf4j
public class SmyPushBlackListJob extends AbstractSimpleElasticJob {

    @Resource
    private ISmyPushBlackListService smyPushBlackListService;

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        String apiCode = "3710196";
        long start = System.currentTimeMillis();
        String parameter = context.getJobParameter();
        int pushStatus;
        if (JSON.isValidObject(parameter)) {
            JSONObject jsonObject = JSON.parseObject(parameter);
            if(jsonObject.containsKey("pushStatus") ){
                pushStatus = jsonObject.getInteger("pushStatus");
                jsonObject.remove("pushStatus");
            } else {
                pushStatus = 1;
            }
            jsonObject.forEach((String key, Object value) -> {
                LocalDate parse = LocalDate.parse(value.toString());
                log.warn("萨摩耶推送黑名单数据调度开始apiCodes:{},处理文件的日期：{}", apiCode, parse);
                smyPushBlackListService.pushBlackList(key, parse,pushStatus);
            });
        } else {
            pushStatus = 1;
            LocalDate now = LocalDate.now();
            log.warn("萨摩耶推送黑名单数据调度开始apiCodes:{},处理文件的日期：{}", apiCode, now);
            smyPushBlackListService.pushBlackList(apiCode, now,pushStatus);
        }
        long end = System.currentTimeMillis();
        log.warn("萨摩耶推送黑名单数据调度结束, 耗时:{}", end - start);
    }
}
