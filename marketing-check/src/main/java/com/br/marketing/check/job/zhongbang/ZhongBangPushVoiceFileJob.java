package com.br.marketing.check.job.zhongbang;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.entity.TransferActionFront;
import com.br.marketing.service.Impl.JobManager;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.service.Impl.zhongbang.ZhongBangService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * D20240506众邦财富filesdk录音文件上传-3710099
 * https://c.100credit.cn/pages/viewpage.action?pageId=160795358
 *
 * @author Guo Zeqiang
 * @dateTime 2024-05-08 17:36
 */
@Component
@Slf4j
public class ZhongBangPushVoiceFileJob extends AbstractSimpleElasticJob {

    @Resource
    private ZhongBangService zhongBangService;

    @Resource
    private JobManager jobManager;

    @Resource
    private TableCreateServiceImpl tableCreateService;

    /**
     * 2024-05-16 10:40
     * JobParameter格式：{"apiCode":yyyy-MM-dd}
     * ,根据apiCode和的日期列表获取数据，日期列表索引0为开始日期，索引1为结束日期,闭区间
     * eg:{"3710099":2024-05-16}
     */
    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        LocalDate localDate = LocalDate.now();
        String parameter = context.getJobParameter();
        JSONObject jsonObject;
        int okStatus = 2;
        if (JSON.isValid(parameter)) {
            jsonObject = JSONObject.parseObject(parameter);
        } else {
            jsonObject = new JSONObject();
            jsonObject.put(StringUtils.isEmpty(parameter) ? "3710099" : parameter, localDate);
        }
        String dateStr = localDate.toString();
        Set<Map.Entry<String, Object>> entries = jsonObject.entrySet();
        for (Map.Entry<String, Object> entry : entries) {
            if (entry.getKey().length() < 1) {
                // apiCode不存在时跳过
                continue;
            }
            String apiCode = entry.getKey();
            String cId = tableCreateService.getCId(apiCode);
            LocalDate value = null;
            Object entryValue = entry.getValue();
            if (entryValue instanceof String) {
                value = LocalDate.parse(entryValue.toString(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } else if (entryValue instanceof LocalDate) {
                value = (LocalDate) entryValue;
            }
            TransferActionFront actionFront;
            actionFront = jobManager.getFrontData(apiCode, dateStr
                    , JobManager.ActionTypeEnum.ZHONGBANG_PUSH_VOICE_FILE.getActionType(), null);
            if (actionFront != null) {
                if (okStatus == actionFront.getStatus()) {
                    continue;
                }
            } else {
                actionFront = new TransferActionFront();
                actionFront.setApiCode(apiCode);
                actionFront.setActionData(dateStr);
                actionFront.setCreateTime(new Date());
                actionFront.setUpdateTime(actionFront.getCreateTime());
                actionFront.setActionType(JobManager.ActionTypeEnum.ZHONGBANG_PUSH_VOICE_FILE.getActionType());
                actionFront.setStatus(1);
                jobManager.saveFrontData(actionFront);
                if (actionFront.getId() == null) {
                    log.warn("众邦上传录音文件任务执行记录添加失败~！{}，{}", apiCode, dateStr);
                    continue;
                }
            }
            boolean b = zhongBangService.voiceFileUpload(apiCode, cId, value);
            if (b) {
                jobManager.updateFrontDataStatus(actionFront.getId(), okStatus);
            }
        }
    }
}
