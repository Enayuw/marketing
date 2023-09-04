package com.br.marketing.check.job;

import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.service.MarketingSyncReportService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 上传数据统计报表任务
 *
 * @Author linquan.guo
 * @CreateDate 2021/11/18 13:42
 * @UpdateUser linquan.guo
 * @UpdateDate 2021/11/18 13:42
 * @UpdateRemark 修改内容
 * @Version 1.0
 */
@Component
@Slf4j
public class TaskUploadSyncReportJob extends AbstractSimpleElasticJob {
    /**
     * 日期
     */
    public static final String YYYYMMDD_FORMAT = "yyyy-MM-dd";
    @Resource
    private MarketingSyncReportService syncReportService;

    /**
     * 上传数据统计报表流程(job可以配置参数：时间字段格式yyyy-MM-dd 例：2021-11-19  2021-11-10,2021-11-19)
     *
     * @param context
     * @return
     */
    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        //选取时间范围
        List<String> dateList = uploadDateHandle(context);
        Long start = System.currentTimeMillis();
        log.warn("【上传数据统计报表任务】调度开始");
        if (dateList != null && !dateList.isEmpty()) {
            for (String uploadDate : dateList) {
                syncReportService.syncReportProcess(uploadDate, context.getJobName());
            }
        }
        Long end = System.currentTimeMillis();
        log.warn("【上传数据统计报表任务】调度结束，耗时：{},分片：{}", end - start, context.getShardingItemParameters());
    }

    /**
     * 选取时间范围
     *
     * @param
     * @return
     */
    public List<String> uploadDateHandle(JobExecutionMultipleShardingContext context) {
        String parameter = context.getJobParameter();
        List<String> indexList = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat(YYYYMMDD_FORMAT);
        if (StringUtils.isNotBlank(parameter)) {
            try {
                String[] split = parameter.split(",");
                if (split != null && split.length == 2) {
                    String beginDate = null;
                    String endDate = null;
                    for (int i = 0; i < split.length; i++) {
                        if (i == 0) {
                            beginDate = split[i];
                        } else {
                            endDate = split[i];
                        }
                    }
                    if (beginDate != null && endDate != null) {
                        Date bDate = DateHelper.parseDate(beginDate);
                        Date eDate = DateHelper.parseDate(endDate);
                        if (bDate.compareTo(eDate) == 0) {
                            indexList.add(dateFormat.format(bDate));
                        } else if (eDate.compareTo(bDate) == 1) {
                            indexList.add(dateFormat.format(bDate));
                            indexList.add(dateFormat.format(eDate));
                            while (true) {
                                Calendar next = Calendar.getInstance();
                                next.setTime(bDate);
                                next.add(Calendar.DATE, 1);
                                bDate = next.getTime();
                                String indexNext = dateFormat.format(bDate);
                                if (indexList.contains(indexNext)) {
                                    break;
                                } else {
                                    indexList.add(indexNext);
                                }
                            }
                        }
                    }
                } else {
                    Date date = DateHelper.parseDate(parameter);
                    indexList.add(dateFormat.format(date));
                }
            } catch (Exception e) {
                log.error("参数异常parameter:{}", parameter, e);
            }
        } else {
            //获取当前时间
            String currentDate = DateHelper.getDateAdd(0);
            indexList.add(currentDate);
            //获取前置一小时日期
            String dateByHour = DateHelper.getDateByHour(-1);
            if (!dateByHour.equals(currentDate)) {
                log.warn("【上传数据统计报表任务】调度更新前置一天统计：{}", dateByHour);
                indexList.add(dateByHour);
            }
        }
        return indexList;
    }
}
