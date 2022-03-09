package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.MarketingTransferSyncUserExample;
import com.br.marketing.entity.TransferFileTask;
import com.br.marketing.entity.TransferFileTaskExample;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.mapper.TransferFileTaskMapper;
import com.br.marketing.service.IMarketingSyncUserService;
import com.br.marketing.service.ITransferToFileService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * D20220302数禾转化数据提取分场景（http://c.100credit.cn/pages/viewpage.action?pageId=66163054）
 * 生成T-1日数据的文件， 有效期判断是用T-1日的日期
 * <p>
 * 一、需求背景
 * 数禾申完、首借接口转化的转化数据需要落到表格，便于推送人工及数据分析。
 * <p>
 * 二、需求目的
 * 月收入80w+
 * <p>
 * 三、具体实施方案
 * 1.提取路径：192.168.22.249:21/DATASHARE/data_shuhe/zhuanhua
 * 2.提取规则
 * <p>
 * a.命名规则：根据场景区分转化文件，不同场景落不同文件
 * 促申完：apicode_cushenwan_yyyymmdd.txt
 * 促首登：apicode_cushoudeng_yyyymmdd.txt
 * 促首借：apicode_cushoujie_yyyymmdd.txt
 * <p>
 * b.转化数据提取数据需在该数据有效期范围内，时间最好可配置
 * 场景      有效期判断
 * 促首登	单个自然月内
 * 促申完	T+15日
 * 促首借	T+31日
 * <p>
 * 备注：首借有效期：T+31，（暂时性，后续会更改为和首登一样的自然月）
 *
 * @author Guo Zeqiang
 * @dateTime 2022/3/4 15:16
 */
@Service
@Slf4j
public class TransferToFileByShuHeServiceImpl implements ITransferToFileService {
    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private TransferFileTaskMapper transferFileTaskMapper;

    @Resource
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;

    @Resource
    private IMarketingSyncUserService iMarketingSyncUserService;

    @Resource
    private TableCreateServiceImpl tableCreateService;

    @Resource
    private RedisChgService redisChgService;

    private final static Pattern PATTERN = Pattern.compile("[-+]?\\d+(\\.\\d+)?");

    private final static Map<String, String> FILE_NAME_PART;

    @Value("${otherConfig.warning.path:/tmp/data_shuhe}")
    private String path;

    private final static String EXTENSION = ".txt";
    private final static String HEADER = "apicode,taskid,usertype,custNum,cell,is_turn,is_black" +
            ",loginTime,clc_usr_fst_log_tim_all,clc_usr_iso_pho_tim,clc_usr_iso_idt_tim,clc_usr_iso_crd_tim" +
            ",clc_usr_iso_inf_tim,applyTime,auditTime,lentTime,insertime";

    static {
        FILE_NAME_PART = new HashMap<>(4);
        FILE_NAME_PART.put("促首登", "%s_cushoudeng_%s%s");
        FILE_NAME_PART.put("促申完", "%s_cushenwan_%s%s");
        FILE_NAME_PART.put("促首借", "%s_cushoujie_%s%s");
    }

    @Override
    public Result<List<TransferFileTask>> buildTransferTask(String apiCode) {
        List<TransferFileTask> transferFileTaskList = new ArrayList<>();
        String shuHeTransferJobStartTime = marketingCommonConfig.getShuHeTransferJobStartTime();
        LocalTime startTime = LocalTime.parse(StringUtils.isEmpty(shuHeTransferJobStartTime)
                ? "06:00:00" : shuHeTransferJobStartTime);
        long until = startTime.until(LocalTime.now(), ChronoUnit.HOURS);
        if (until == 0 || until == 1) {
            log.warn("xxxxxxxxxxxxxxxxxxxxxxxx:" + until);
        } else {
            log.warn("################:" + until);
        }
        String dateYyyyMmDdStr = LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        Map<String, String> shuHeTransferDataExtractMap = marketingCommonConfig.getShuHeTransferDataExtractMap();
        Set<String> userTypes = shuHeTransferDataExtractMap.keySet();
        List<String> stringList = userTypes.parallelStream().map(s -> String.format(FILE_NAME_PART.getOrDefault(s
                , "%s_".concat(s).concat("_%s%s")), apiCode, dateYyyyMmDdStr, EXTENSION)).collect(Collectors.toList());
        TransferFileTaskExample taskExample = new TransferFileTaskExample();
        taskExample.createCriteria().andApiCodeEqualTo(apiCode).andStartDateEqualTo(dateYyyyMmDdStr)
                .andFileNameIn(stringList);
        List<TransferFileTask> transferFileTasks = transferFileTaskMapper.selectByExample(taskExample);
        Result<List<TransferFileTask>> result = new Result<>();
        log.warn("数禾[{}]转化数据提取分#生成文件任务{}", apiCode, transferFileTasks.size());
        if (LocalTime.now().isAfter(startTime) && transferFileTasks.size() < 1) {
            log.warn("数禾[{}]转化数据提取分1#{}", apiCode, shuHeTransferDataExtractMap);
            // 将配置中的有效期处理成天
            Map<String, Integer> dataExtractMap = dataExtractDateHandle(shuHeTransferDataExtractMap, userTypes);
            log.warn("数禾[{}]转化数据提取分2#{}", apiCode, dataExtractMap);
            userTypes.forEach(userType -> {
                TransferFileTask transferFileTask = new TransferFileTask();
                long contextId = System.currentTimeMillis();
                transferFileTask.setApiCode(apiCode);
                transferFileTask.setFileType(dataExtractMap.getOrDefault(userType, 0));
                transferFileTask.setBatchNumber(userType);
                transferFileTask.setFileName("");
                transferFileTask.setStartDate(dateYyyyMmDdStr);
                transferFileTask.setContextId(contextId);
                transferFileTask.setTaskNumber(0);
                transferFileTask.setStatus(1);
                transferFileTask.setCreateTime(new Date());
                transferFileTask.setUpdateTime(new Date());
                transferFileTaskList.add(transferFileTask);
            });
        }
        result.setDate(transferFileTaskList);
        result.setCode(ResultCode.SUCCESS.getValue());
        return result;
    }

    private String setCid(String apiCode) {
        String key = "marketing:api:shuhe:transfer:cid:".concat(apiCode);
        String cId;
        try {
            cId = redisChgService.get(key);
            if (StringUtils.isEmpty(cId)) {
                cId = tableCreateService.getCId(apiCode);
                // 缓存七天
                redisChgService.setex(key, cId, 7 * 86400);
            }
        } catch (Exception e) {
            cId = tableCreateService.getTcId(apiCode);
            log.error(e.getMessage(), e);
        }
        return cId.replaceFirst("-", "");
    }

    /**
     * 处理有效期范围
     */
    private Map<String, Integer> dataExtractDateHandle(Map<String, String> shuHeTransferDataExtractMap
            , Set<String> userTypes) {
        Map<String, Integer> dataExtractMap = new ConcurrentHashMap<>(
                (int) (shuHeTransferDataExtractMap.size() / 0.75 + 1));
        userTypes.forEach(userType -> {
            Matcher matcher = PATTERN.matcher(shuHeTransferDataExtractMap.get(userType));
            String day = "0";
            if (matcher.find()) {
                day = matcher.group();
            }
            dataExtractMap.put(userType, new BigDecimal(day).setScale(0, BigDecimal.ROUND_HALF_UP).intValue());
        });
        return dataExtractMap;
    }


    /**
     * 获取上传表中案件编号最新的创建时间
     */
    private Map<String, Map<String, Object>> getSyncUserLatestUploadTime(
            List<MarketingTransferSyncUser> mtsuList, String apiCode, String userType) {
        List<String> custNums = mtsuList.parallelStream().map(
                MarketingTransferSyncUser::getCustNum).collect(Collectors.toList());
        List<Map<String, Object>> creatTimeList = iMarketingSyncUserService
                .getCreatTimeByCustNumAndUserTypeList(apiCode, custNums, userType);
        return creatTimeList.parallelStream().collect(
                Collectors.toMap(m -> (String) m.get("custNum")
                        , m -> m, (v1, v2) -> {
                            Object v11 = v1.get("createTime");
                            Object v22 = v2.get("createTime");
                            return StringUtils.isEmpty(v11)
                                    ? v2 : StringUtils.isEmpty(v22)
                                    ? v1 : ((Date) v11).before((Date) v22) ? v2 : v1;
                        }));
    }

    @Override
    public Result<Object> actionTransferToFile(TransferFileTask transferFileTask) {
        Result<Object> result = new Result<>();
        String dateYyyyMmDdStr = transferFileTask.getStartDate();
        String apiCode = transferFileTask.getApiCode();
        String userType = transferFileTask.getBatchNumber();
        // 前一天时间范围
        LocalDateTime localDateTime = LocalDateTime.now().minusDays(1);
        LocalDateTime first = localDateTime.withHour(0).withMinute(0).withSecond(0);
        LocalDateTime last = localDateTime.withHour(23).withMinute(59).withSecond(59);
        // 生成检索条件
        MarketingTransferSyncUserExample example = new MarketingTransferSyncUserExample();
        example.createCriteria().andCreateTimeBetween(Date.from(first.atZone(ZoneId.systemDefault()).toInstant())
                , Date.from(last.atZone(ZoneId.systemDefault()).toInstant())).andApiCodeEqualTo(apiCode)
                .andUserTypeEqualTo(userType);
        example.settCid(setCid(apiCode));
        List<MarketingTransferSyncUser> list = null;
        String fileNameDefault = FILE_NAME_PART.getOrDefault(userType
                , "%s_".concat(userType).concat("_%s%s"));
        String fileName = String.format(fileNameDefault, apiCode, dateYyyyMmDdStr, EXTENSION);
        String fileDirectory = path.concat(apiCode).concat(File.separator)
                .concat(dateYyyyMmDdStr).concat(File.separator);
        final File filePath = new File(fileDirectory);
        if (!filePath.exists()) {
            if (!filePath.mkdirs()) {
                log.error("创建文件目录“{}”失败", fileDirectory);
            }
        }
        final File filePtah = new File(fileDirectory, fileName);
        transferFileTask.setFilePath(fileDirectory);
        transferFileTask.setFileName(fileName);
        int page = 0;
        int pageSize = 2000;
        String separator = ",";
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(filePtah, false), StandardCharsets.UTF_8))) {
            writer.write(HEADER.concat("\r\n"));
            writer.flush();
            while (list == null || list.size() == pageSize) {
                example.setOrderByClause(String.format("id ASC LIMIT %s,%s", (page * pageSize), pageSize));
                page++;
                list = marketingTransferSyncUserMapper.selectByExample(example);
                if (list.size() < 1) {
                    break;
                }
                writerFile(apiCode, userType, list, transferFileTask, separator, writer);
            }
            transferFileTask.setBatchNumber(String.format(fileNameDefault, apiCode, "", dateYyyyMmDdStr)
                    .concat("_") + System.currentTimeMillis());
            transferFileTask.setStatus(2);
            transferFileTaskMapper.insertSelective(transferFileTask);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            result.setCode(ResultCode.FAIL.getValue());
            result.setDate(e.getMessage());
            transferFileTaskMapper.insertSelective(transferFileTask);
            return result;
        }
        result.setCode(ResultCode.SUCCESS.getValue());
        return result;
    }

    /**
     * 写入文件
     */
    private void writerFile(String apiCode, String userType, List<MarketingTransferSyncUser> list,
                            TransferFileTask transferFileTask, String separator, BufferedWriter writer) throws IOException {
        // 获取custNum最新创建时间
        Map<String, Map<String, Object>> custNumMap = getSyncUserLatestUploadTime(list
                , apiCode, userType);
        for (MarketingTransferSyncUser transferSyncUser : list) {
            Map<String, Object> creatTimeMap = custNumMap.getOrDefault(transferSyncUser.getCustNum(), null);
            if (creatTimeMap == null) {
                continue;
            }
            Object creatTime = creatTimeMap.getOrDefault("createTime", null);
            if (creatTime == null) {
                continue;
            }
            Boolean periodOfValidity = iMarketingSyncUserService.isPeriodOfValidity(transferSyncUser.getCreateTime()
                    , transferFileTask.getFileType(), (Date) creatTime);
            if (periodOfValidity) {
                transferFileTask.setTaskNumber(transferFileTask.getTaskNumber() + 1);
                String reserveField1 = transferSyncUser.getReserveField1();
                JSONObject reserveField1Json = JSONObject.parseObject(reserveField1);
                String sb = transferSyncUser.getApiCode() + separator +
                        creatTimeMap.getOrDefault("taskId", "") + separator +
                        transferSyncUser.getUserType() + separator +
                        transferSyncUser.getCustNum() + separator +
                        reserveField1Json.getOrDefault("cell", "") + separator +
                        reserveField1Json.getOrDefault("is_turn", "") + separator +
                        reserveField1Json.getOrDefault("is_black", "") + separator +
                        (StringUtils.isEmpty(transferSyncUser.getLoginTime()) ? ""
                                : transferSyncUser.getLoginTime()) + separator +
                        reserveField1Json.getOrDefault("clc_usr_fst_log_tim_all", "") + separator +
                        reserveField1Json.getOrDefault("clc_usr_iso_pho_tim", "") + separator +
                        reserveField1Json.getOrDefault("clc_usr_iso_idt_tim", "") + separator +
                        reserveField1Json.getOrDefault("clc_usr_iso_crd_tim", "") + separator +
                        reserveField1Json.getOrDefault("clc_usr_iso_inf_tim", "") + separator +
                        (StringUtils.isEmpty(transferSyncUser.getApplyTime()) ? ""
                                : transferSyncUser.getApplyTime()) + separator +
                        (StringUtils.isEmpty(transferSyncUser.getAuditTime()) ? ""
                                : transferSyncUser.getAuditTime()) + separator +
                        (StringUtils.isEmpty(transferSyncUser.getAuditAmount()) ? ""
                                : transferSyncUser.getAuditAmount()) + separator +
                        reserveField1Json.getOrDefault("applyLoanTime", "") + separator +
                        (StringUtils.isEmpty(transferSyncUser.getLentTime()) ? ""
                                : transferSyncUser.getLentTime()) + separator +
                        (ObjectUtils.isEmpty(transferSyncUser.getInsertTime()) ? ""
                                : transferSyncUser.getInsertTime()) + "\r\n";
                writer.write(sb);
                writer.flush();
            }
        }
    }
}
