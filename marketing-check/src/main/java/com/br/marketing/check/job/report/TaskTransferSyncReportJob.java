package com.br.marketing.check.job.report;

import com.br.marketing.service.TransferSyncReportService;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 转化数据统计调度任务
 *
 * @author Guo Zeqiang
 * @dateTime 2022/6/29 10:07
 */
@Component
@Slf4j
public class TaskTransferSyncReportJob extends AbstractSimpleElasticJob {
    @Resource
    private TransferSyncReportService transferSyncReportService;

    /**
     * 2022/6/29 11:08
     * 工作配置(JobParameter)参数格式：yyyy-MM-dd,yyyy-MM-dd,yyyy-MM-dd,...
     * eg:2022-06-28,2022-06-29,2022-06-30
     * <p>
     * 参数配置规则：
     * 1. 配置一个日期或三个及以上时，直接使用配置中的日期。
     * 2. 配置二个日期时，处理为日期范围
     */
    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        Long start = System.currentTimeMillis();
        // 分片
        List<Integer> shardingItems = context.getShardingItems();
        // 总分片数
        int shardingTotalCount = context.getShardingTotalCount();
        String param = context.getJobParameter();
        SortedSet<String> dateStrSet = new TreeSet<>();
        // 处理job参数
        parameterHandle(param, dateStrSet);
        // 添加当前日期
        LocalDateTime now = LocalDateTime.now().atZone(ZoneId.systemDefault()).toLocalDateTime();
        dateStrSet.add(now.format(DateTimeFormatter.ISO_LOCAL_DATE));
        // 兼容跨天统计，当天零点需要对前一天的数据再次统计
        LocalDate minus = now.minusHours(1).atZone(ZoneId.systemDefault()).toLocalDate();
        if (now.toLocalDate().isAfter(minus)) {
            dateStrSet.add(minus.format(DateTimeFormatter.ISO_LOCAL_DATE));
        }
        transferSyncReportService.reportProcess(dateStrSet, shardingTotalCount, shardingItems,context.getJobName());
        Long end = System.currentTimeMillis();
        log.warn("【转化数据统计调度任务】调度结束，耗时：{},分片：{}", end - start, context.getShardingItemParameters());
    }

    /**
     * 2022/6/29 11:18
     * 解析参数
     */
    private void parameterHandle(String param, Set<String> dateStrSet) {
        // job中没有配置参数时默认使用当前日期
        if (StringUtils.isBlank(param)) {
            return;
        }
        StringTokenizer dateTokenizer = new StringTokenizer(param, ",");
        while (dateTokenizer.hasMoreTokens()) {
            String dateStr = dateTokenizer.nextToken();
            try {
                LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (Exception e) {
                log.warn(e.getMessage(), e);
                continue;
            }
            dateStrSet.add(dateStr);
        }
        int s = 2;
        if (s == dateStrSet.size()) {
            Iterator<String> iterator = dateStrSet.stream().iterator();
            String beginDateStr = iterator.next();
            LocalDate beginDate = LocalDate.parse(beginDateStr, DateTimeFormatter.ISO_LOCAL_DATE);
            String endDateStr = iterator.next();
            LocalDate endDate = LocalDate.parse(endDateStr, DateTimeFormatter.ISO_LOCAL_DATE);
            long day = endDate.toEpochDay() - beginDate.toEpochDay() - 1;
            for (; day > 0; day--) {
                dateStrSet.add(endDate.minusDays(day).format(DateTimeFormatter.ISO_LOCAL_DATE));
            }
        }
    }

}
