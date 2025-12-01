package com.br.marketing.bridge.job.zhongbang;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.bridge.service.ZhongBangAIService;
import com.br.marketing.common.enums.SftpFileTypeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.LocalFile;
import com.br.marketing.entity.LocalFileExample;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.service.Impl.JobManager;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.service.SyncConfigService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.File;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class ZhongBangAIPullFileDataJob extends AbstractSimpleElasticJob {

    @Resource
    private ZhongBangAIService zhongBangAIService;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private TableCreateServiceImpl tableCreateService;

    @Resource
    private SyncConfigService syncConfigService;


    @Resource
    private LocalFileMapper localFileMapper;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        String okFileExtension = ".txt.ok";
        long start = System.currentTimeMillis();
        /*
         * param格式：{"apiCode":"yyyyMMdd,yyyy-MM-dd HH:mm:ss,yyyy-MM-dd HH:mm:ss"}
         */
        String parameter = context.getJobParameter();
        JSONObject paramJson = StringUtils.isBlank(parameter) ? null : JSONObject.parseObject(parameter);
        String localDate = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        Map<String, List<Map<String, String>>> config2Map = marketingCommonConfig.getZhongBangAIPullFileDataConfigMap();
        Map<String, LinkedHashMap<String, String>> configMap = zhongBangPullFileDataConfigDefault(config2Map);
        int size = configMap.size();
        if (size < 1) {
            return;
        }
        configMap.forEach((apiCode, v) -> {
            List<String> dateStrList = getDateStrList(apiCode, paramJson);
            String dateStr = dateStrList.get(0);
            String beginDateTime = dateStrList.get(1);
            String endDateTime = dateStrList.get(2);
            String cId = tableCreateService.getCId(apiCode);
            String filePath = syncConfigService.getPullCustomerFilePath(apiCode).concat(localDate).concat(File.separator);
            ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(25, 50, new SynchronousQueue<>());
            v.forEach((fileName, tableHead) -> {
                String fileNumber = getFileNumber(apiCode, cId);
                String fileNameNew = fileName.concat(dateStr).concat("_").concat(fileNumber);
                zhongBangAIService.fileQueryAndDownload(apiCode, cId, fileNameNew.concat(okFileExtension)
                        , tableHead, filePath.concat(fileNameNew).concat(File.separator), beginDateTime, endDateTime
                        , threadPool);
            });
            threadPool.shutdown();
            try {
                long timeout = 5;
                while (!threadPool.awaitTermination(timeout, TimeUnit.MINUTES)) {
                    log.warn("众邦AI-FileSDK文件下载任务等待入库任务结束，预估还有任务{}", threadPool.getTaskCount());
                }
            } catch (InterruptedException e) {
                List<Runnable> runnables = threadPool.shutdownNow();
                Thread.currentThread().interrupt();
                log.error(e.getMessage() + "还在运行的任务数:" + runnables.size(), e);
            }
        });
        long end = System.currentTimeMillis();
        log.warn("众邦AI-FileSDK文件下载任务结束，job参数信息:{};speed参数信息:{}，运行耗时:{}s"
                , parameter, configMap, (end - start) / 1000);
    }

    /**
     * 2023-11-18 17:49
     * 任务执行的默认参数
     */
    private Map<String, LinkedHashMap<String, String>> zhongBangPullFileDataConfigDefault(
            Map<String, List<Map<String, String>>> zhongBangPullFileDataConfigMap) {
        Map<String, LinkedHashMap<String, String>> map = new HashMap<>(4);
        LinkedHashMap<String, String> linkedHashMap = new LinkedHashMap<>();
        if (zhongBangPullFileDataConfigMap == null) {
            linkedHashMap.put("original_AI_", "taskId|@|userType|@|cell|@|useTeble|@|custNum|@|cell|@|firstName|@|extend01");
            map.put("3740001", linkedHashMap);
        } else {
            zhongBangPullFileDataConfigMap.forEach((k, l) -> {
                l.forEach(m -> m.forEach(linkedHashMap::put));
                map.put(k, linkedHashMap);
            });
        }
        return map;
    }


    /**
     * 2025-11-14 14:49
     * 获取文件名后缀
     */
    private String getFileNumber(String apiCode, String cId) {
        LocalFileExample example = new LocalFileExample();
        example.createCriteria().andApiCodeEqualTo(apiCode).andCidEqualTo(cId)
                .andCreateTimeGreaterThanOrEqualTo(Date.from(LocalDate.now().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()))
                .andFileTypeEqualTo(SftpFileTypeEnum.ZHONGBANG_AI.getValue())
                .andStatusEqualTo("2");
        List<LocalFile> localFiles = localFileMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(localFiles)) {
            return "01";
        } else {
            //提取数字并找最大值
            int maxNumber = 0;
            for (LocalFile localFile : localFiles) {
                String fileName = localFile.getFileName();
                try {
                    String numberStr = fileName.substring(fileName.length() - 6, fileName.length() - 4);
                    int number = Integer.parseInt(numberStr);
                    maxNumber = Math.max(maxNumber, number);
                } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
                    log.error("众邦AI文件名格式解析失败: {}", fileName);
                }
            }
            // 自增并返回
            return String.format("%02d", maxNumber + 1);
        }
    }

    /**
     * 2023-11-18 17:47
     * 根据apiCode获取参数中的时间
     * 时间数组中index位置0为文件名称中拼接的日期(yyyyMMdd),1为查询文件的开始时间(yyyy-MM-dd HH:mm:ss)
     * ,2为查询文件的结束时间(yyyy-MM-dd HH:mm:ss)
     */
    private List<String> getDateStrList(String apiCode, JSONObject paramJson) {
        int day = marketingCommonConfig.getZhongBangPullFileDataDay();
        return getDateStrList(apiCode, paramJson, LocalDate.now().plusDays(day).format(DateTimeFormatter.BASIC_ISO_DATE));
    }

    /**
     * 2023-11-22 15:31
     * 获取文件名中的日期及查询范围
     */
    private List<String> getDateStrList(String apiCode, JSONObject paramJson, String localDate) {
        List<String> list = new ArrayList<>();
        String dateStr;
        String beginDateTime;
        String endDateTime;
        String regex = ",";
        int lengthIs3 = 3;
        int lengthIs2 = 2;
        String[] dateTimeStr;
        if (paramJson == null) {
            dateStr = localDate;
            beginDateTime = LocalDate.now().atStartOfDay().format(DATE_TIME_FORMATTER);
            endDateTime = LocalDate.now().atTime(23, 59, 59)
                    .atZone(ZoneId.systemDefault()).format(DATE_TIME_FORMATTER);
        } else {
            dateTimeStr = paramJson.getString(apiCode).split(regex);
            if (dateTimeStr.length == 0) {
                dateStr = localDate;
                beginDateTime = LocalDate.now().atStartOfDay().format(DATE_TIME_FORMATTER);
                endDateTime = LocalDate.now().atTime(23, 59, 59)
                        .atZone(ZoneId.systemDefault()).format(DATE_TIME_FORMATTER);
            } else if (lengthIs3 == dateTimeStr.length) {
                dateStr = dateTimeStr[0];
                beginDateTime = dateTimeStr[1];
                endDateTime = dateTimeStr[2];
            } else if (lengthIs2 == dateTimeStr.length) {
                dateStr = localDate;
                beginDateTime = dateTimeStr[0];
                endDateTime = dateTimeStr[1];
            } else {
                dateStr = dateTimeStr[0];
                beginDateTime = LocalDate.now().atStartOfDay().format(DATE_TIME_FORMATTER);
                endDateTime = LocalDate.now().atTime(23, 59, 59)
                        .atZone(ZoneId.systemDefault()).format(DATE_TIME_FORMATTER);
            }
        }
        list.add(dateStr);
        list.add(beginDateTime);
        list.add(endDateTime);
        return list;
    }
}
