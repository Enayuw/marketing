package com.br.marketing.bridge.job;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.LocalFileExample;
import com.br.marketing.entity.TransferActionFront;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.service.Impl.JobManager;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.service.Impl.zhongbang.ZhongBangService;
import com.br.marketing.service.SyncConfigService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.dangdang.ddframe.job.api.JobExecutionMultipleShardingContext;
import com.dangdang.ddframe.job.plugin.job.type.simple.AbstractSimpleElasticJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * 众邦财富文件拉取
 * D20231102众邦财富FileSDK文件查询-3710099（营销→客户）
 * http://c.100credit.cn/pages/viewpage.action?pageId=125078844
 * <p>
 * D20231102众邦财富FileSDK文件下载-3710099（营销→客户）
 * https://c.100credit.cn/pages/viewpage.action?pageId=136318794
 *
 * @author zeqiang.guo
 * @dateTime 2023/11/08 16:13
 */
@Component
@Slf4j
public class ZhongBangPullFileDataJob extends AbstractSimpleElasticJob {

    @Resource
    private ZhongBangService zhongBangService;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private TableCreateServiceImpl tableCreateService;

    @Resource
    private SyncConfigService syncConfigService;

    @Resource
    private JobManager jobManager;

    @Resource
    private LocalFileMapper localFileMapper;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final LocalTime repeatPullAsOfTime = LocalTime.parse("12:00");

    @Override
    public void process(JobExecutionMultipleShardingContext context) {
        String okFileExtension = ".ok";
        String txtFileExtension = ".txt";
        long start = System.currentTimeMillis();
        /*
         * param格式：{"apiCode":"yyyyMMdd,yyyy-MM-dd HH:mm:ss,yyyy-MM-dd HH:mm:ss"}
         */
        String parameter = context.getJobParameter();
        JSONObject paramJson = StringUtils.isBlank(parameter) ? null : JSONObject.parseObject(parameter);
        String localDate = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String local2Date = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        Integer status = 2;
        Map<String, List<Map<String, String>>> config2Map = marketingCommonConfig.getZhongBangPullFileDataConfigMap();
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
                String fileNameNew = fileName.endsWith(txtFileExtension) ? fileName.replace(txtFileExtension
                        , "") : fileName.endsWith("_") ? fileName.concat(dateStr) : fileName;
                TransferActionFront frontData = jobManager.getFrontData(apiCode, local2Date, 1
                        , fileNameNew.concat(txtFileExtension));
                Long id;
                boolean b;
                if (frontData == null) {
                    // 创建任务记录
                    frontData = newTransferActionFront(local2Date, apiCode, fileNameNew.concat(txtFileExtension));
                    id = jobManager.saveFrontData(frontData);
                    b = zhongBangService.fileQueryAndDownload(apiCode, cId, fileNameNew.concat(okFileExtension)
                            , tableHead, filePath.concat(fileNameNew).concat(File.separator), beginDateTime, endDateTime
                            , threadPool);
                } else if (frontData.getStatus().equals(status)) {
                    // 已成任务已经完成
                    return;
                } else {
                    id = frontData.getId();
                    boolean before = LocalTime.now().isBefore(repeatPullAsOfTime);
                    if (before || getLocalFileCount(apiCode, cId, fileNameNew.concat(txtFileExtension)) < 1) {
                        // 任务未完成且没有到截至时间
                        b = zhongBangService.fileQueryAndDownload(apiCode, cId
                                , fileNameNew.concat(okFileExtension), tableHead, filePath.concat(fileNameNew)
                                        .concat(File.separator), beginDateTime, endDateTime, threadPool);
                    } else {
                        // 任务未完成但已到截至时间并且已存在文件记录，标记任务已完成
                        b = true;
                        log.error("众邦财富FileSDK文件下载任务未完成但已到截至时间并且已存在文件记录，标记任务已完成，当前文件为{}"
                                , fileNameNew.concat(txtFileExtension));
                    }
                }
                if (b) {
                    jobManager.updateFrontDataStatus(id, status);
                }
            });
            threadPool.shutdown();
            try {
                long timeout = 5;
                while (!threadPool.awaitTermination(timeout, TimeUnit.MINUTES)) {
                    log.warn("众邦财富FileSDK文件下载任务等待入库任务结束，预估还有任务{}", threadPool.getTaskCount());
                }
            } catch (InterruptedException e) {
                List<Runnable> runnables = threadPool.shutdownNow();
                Thread.currentThread().interrupt();
                log.error(e.getMessage() + "还在运行的任务数:" + runnables.size(), e);
            }
        });
        long end = System.currentTimeMillis();
        log.warn("众邦财富FileSDK文件下载任务结束，job参数信息:{};speed参数信息:{}，运行耗时:{}s"
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
            linkedHashMap.put("original_caifu_"
                    , "custNum|@|id|@|cell|@|name|@|gender|@|age|@|region|@|registerTime|@|ifRegister|@|ifApply"
                            + "|@|ifLogin|@|loginTime|@|applyResult|@|productStartTime|@|productEndTime|@|ifApplyAmount"
                            + "|@|ifLent|@|userType");
            linkedHashMap.put("original_daikuan_"
                    , "custNum|@|id|@|cell|@|name|@|gender|@|age|@|region|@|registerTime|@|ifRegister|@|ifApply"
                            + "|@|ifLogin|@|loginTime|@|userType");
            linkedHashMap.put("transform_br_"
                    , "custNum|@|ifLogin1|@|ifApply1|@|applyTime|@|applyproductName|@|applyAmount|@|ifLent1|@|lentTime"
                            + "|@|lentAmount|@|pushTime|@|userType");
            map.put("3710099", linkedHashMap);
        } else {
            zhongBangPullFileDataConfigMap.forEach((k, l) -> {
                l.forEach(m -> m.forEach(linkedHashMap::put));
                map.put(k, linkedHashMap);
            });
        }
        return map;
    }


    /**
     * 2023-11-18 17:49
     * 查询文件下载情况
     */
    private int getLocalFileCount(String apiCode, String cId, String fileName) {
        LocalFileExample example = new LocalFileExample();
        example.createCriteria().andApiCodeEqualTo(apiCode).andCidEqualTo(cId)
                .andFileNameEqualTo(fileName)
                .andCreateTimeGreaterThanOrEqualTo(Date.from(
                        LocalDate.now().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()))
                .andFileTypeEqualTo("zhongbang_caifu");
        return localFileMapper.countByExample(example);
    }

    /**
     * 2023-11-18 17:49
     * 创建任务
     */
    private TransferActionFront newTransferActionFront(String loaclDate, String apiCode, String fileName) {
        TransferActionFront frontData = new TransferActionFront();
        frontData.setActionData(loaclDate);
        frontData.setRemark(fileName);
        frontData.setActionType(1);
        frontData.setApiCode(apiCode);
        return frontData;
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
        if (paramJson == null || (dateTimeStr = paramJson.getString(apiCode).split(regex)).length == 0) {
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
        list.add(dateStr);
        list.add(beginDateTime);
        list.add(endDateTime);
        return list;
    }
}

