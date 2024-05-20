package com.br.marketing.check.job;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.LocalFile;
import com.br.marketing.entity.LocalFileExample;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.service.PushDataService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import com.google.common.base.Splitter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * @author guangchao.zhang
 * @Classname CallingToSendJob
 * @Description 回调第三方接口发送不打信息
 * @Date 2022/2/16 10:02 AM
 */
//@Component
@Slf4j
public class XieChengSmsDataCollidingToSendJob extends AbstractSimpleElasticJob {

    private final static String XIECHENGSMSCOLLIDING = "xiechengsmscolliding";


    @Resource
    private LocalFileMapper localFileMapper;

    @Resource
    private PushDataService pushDataService;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;


    @Override
    public void process(JobExecutionMultipleShardingContext jobExecutionMultipleShardingContext) {
        // 参数 0 是新文件（未推过的文件）  1 是 旧文件（推过的文件）
        // 注意：需要删除掉数据库里的mq 消息配置
        // sup_callback_yyyymmdd_01 callback_yyyymmdd_01
        // 1.判断是否为补偿定时任务，入参是否为空，如果为空则是正常定时任务 不为空则为补偿定时任务
        // 2.若为正常定时任务，根据文件名判断是否为立刻需要推送的数据（spu+当前日期）;
        // 3.若不是立刻推送的的数据  则判断是
        // 4.含有"sup" 则为立刻推送，不含有 "sup" 则需要 8点-24点之间推送。
        //1. 立即推：日期为当天+“SUP”标识 (新文件)
        //2. 标准推：日期为当天（不含“sup”标识）(新文件)
        //3. 周期推：日期小于当天（旧文件）
        //4. 未来推：日期大于当天
        //4. 补偿推：根据localId 直接推
        //    local_id 来源 定时任务方法输入、查询log表异常的数据所对应的local_id
        String jobParameter = jobExecutionMultipleShardingContext.getJobParameter();
        if (StringUtils.isNotBlank(jobParameter)) {
            List<String> params = Splitter.on(",").splitToList(jobParameter);
            push(Long.valueOf(params.get(0)), ("0").equals(params.get(1)));
        } else {
            LocalFileExample localFileExample = new LocalFileExample();
            localFileExample.createCriteria()
                    .andFileTypeEqualTo(XIECHENGSMSCOLLIDING)
                    .andStatusEqualTo("2");
            List<LocalFile> localFileList = localFileMapper.selectByExample(localFileExample);

            localFileList.forEach((lf) -> {
                LocalDate fileDate = isFileDate(lf);
                // 新文件
                if (LocalDate.now().isEqual(fileDate) && !lf.getFileName().contains("sup")) {
                    // 整点推 // 补偿推
                    // 当期那时间是否符合推送时间 当前时间 >= 推送时间
                    if (!LocalTime.now().isBefore(getSendTime())) {
                        push(lf.getId(), true);
                    }
                }
                if (LocalDate.now().isAfter(fileDate)) {
                    //周期推
                    // 当前时间大约文件推送时间 旧文件
                    if (lf.getFileName().contains("sup")) {
                        push(lf.getId(), false);
                    } else if (!LocalTime.now().isBefore(getSendTime())) {
                        push(lf.getId(), false);
                    }
                }
            });
        }

    }

    private void push(Long localId, boolean value) {
        JSONObject msg = new JSONObject();
        msg.put("localId", localId);
        msg.put("isNewFile", value);
        pushDataService.pushXieChengSmsCollidingToDbData(msg.toJSONString());
    }

    /**
     * 判断当前时间是否大于20点
     *
     */
    private  LocalTime getSendTime() {
        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss");
        return LocalTime.parse(marketingCommonConfig.getXieChengSmsCollidingStartTime(), timeFormat);
    }

    private LocalDate isFileDate(LocalFile lf) {
        List<String> strings = Splitter.on("_").splitToList(lf.getFileName());
        if (strings.contains("sup")) {
            return getLocalDate(strings.get(2));
        } else {
            return getLocalDate(strings.get(1));
        }
    }
    private static LocalDate getLocalDate(String dateStr) {
        return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyyMMdd"));
    }
}
