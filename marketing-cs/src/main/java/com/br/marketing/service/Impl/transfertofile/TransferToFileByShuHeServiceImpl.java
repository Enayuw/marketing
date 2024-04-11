package com.br.marketing.service.Impl.transfertofile;

import com.alibaba.fastjson.JSONObject;
import com.br.common.encryption.Sha256Util;
import com.br.common.util.BrCipherMaker;
import com.br.common.util.DateUtils;
import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.entity.MarketingTransferSyncUser;
import com.br.marketing.entity.TransferFileTask;
import com.br.marketing.entity.TransferFileTaskExample;
import com.br.marketing.mapper.MarketingDataValidConfigDefaultMapper;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.mapper.TransferFileTaskMapper;
import com.br.marketing.service.IMarketingSyncUserService;
import com.br.marketing.service.ITransferToFileService;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.service.SyncConfigService;
import com.br.marketing.service.TransferDataValidityPeriodService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.BiFunction;
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

    @Autowired
    SyncConfigService syncConfigService;

    @Resource
    private TransferDataValidityPeriodService validityPeriodService;

    @Resource
    private MarketingDataValidConfigDefaultMapper validConfigDefaultMapper;

    private final static String EXTENSION = ".txt";
    private final static String TABLE_HEADER = "apicode,taskid,usertype,custNum,cell,is_turn,is_black" +
            ",loginTime,clc_usr_lst_app_sta_tim,clc_usr_iso_pho_tim,clc_usr_iso_idt_tim,clc_usr_iso_crd_tim" +
            ",clc_usr_iso_inf_tim,applyTime,auditTime,auditAmount,applyLoanTime,lentTime,clc_usr_adt_tim_rcn_lon_wo_asset_label,insertime";

    private final static String TABLE_HEADER_CUFUJIE = "apicode,taskid,groupType,cust_num,cell,is_turn,is_black" +
            ",clc_usr_lst_app_sta_tim,clc_usr_lst_non_dcp_trs_tim,off_usr_lst_ord_tim_all,clc_usr_avl_lmt_lv0" +
            ",clc_usr_adt_lmt_lv0,createtime,clc_usr_lst_ord_tim_all_wizard,clc_usr_adt_lmt_fst_all,clc_usr_lst_adt_apy_tim_hvy";

    private final static String TABLE_HEADER_CHONGSHEN = "apicode,taskid,usertype,custNum,cell,is_turn,is_black," +
            "clc_usr_max_dx_rrt_end,clc_usr_lst_app_sta_tim,clc_usr_iso_pho_tim,clc_usr_iso_idt_tim" +
            ",clc_usr_iso_crd_tim,clc_usr_iso_inf_tim,auditTime,clc_usr_lst_reaudit_apply_time,clc_usr_adt_tim_rcn_lon_wo_asset_label,createtime";

    static {
        FILE_NAME_PART = new HashMap<>(8);
        FILE_NAME_PART.put("促首登", "%s_cushoudeng_%s%s");
        FILE_NAME_PART.put("促申完", "%s_cushenwan_%s%s");
        FILE_NAME_PART.put("促首借", "%s_cushoujie_%s%s");
        FILE_NAME_PART.put("促复借", "%s_cufujie_%s%s");
        FILE_NAME_PART.put("重申", "%s_chongshen_%s%s");
    }

    private LocalDateTime appointTime;

    @Override
    public String isMyParam(String apiCode, String jobParameter) {
        return "";
    }

    @Override
    public Result<List<TransferFileTask>> buildTransferTask(String apiCode,String myParam) {
        List<TransferFileTask> transferFileTaskList = new ArrayList<>();
        Result<List<TransferFileTask>> result = new Result<>();
        result.setDate(transferFileTaskList);
        result.setCode(ResultCode.SUCCESS.getValue());
        String shuHeTransferJobStartTime = marketingCommonConfig.getShuHeTransferExtractJobStartTime();
        String dateYyyyMmDdStr = LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        Map<String, String> shuHeTransferDataExtractMap = marketingCommonConfig.getShuHeTransferExtractDayMap();
        Set<String> userTypes = new HashSet<>(marketingCommonConfig.getShuHeTransferExtractApiCodes().get(apiCode));
        if (CollectionUtils.isEmpty(userTypes)) {
            userTypes = shuHeTransferDataExtractMap.keySet();
        }
        LocalTime startTime;
        String finalDateYyyyMmDdStr;
        if (StringUtils.isEmpty(shuHeTransferJobStartTime)) {
            startTime = LocalTime.parse("06:00:00");
            appointTime = null;
            finalDateYyyyMmDdStr = dateYyyyMmDdStr;
        } else {
            String t = "T";
            // 处理指定日期提取数据, 添加T时为需要以指定日期获取数据
            // 格式：2022-04-02T06:00:00
            if (shuHeTransferJobStartTime.contains(t)) {
                final String[] ts = shuHeTransferJobStartTime.split(t);
                int length = ts.length;
                if (length == 2) {
                    startTime = LocalTime.parse(ts[1]);
                    appointTime = StringUtils.isEmpty(ts[0]) ? LocalDateTime.now()
                            : LocalDate.parse(ts[0]).atStartOfDay();
                } else if (length > 0) {
                    startTime = LocalTime.parse(ts[length - 1]);
                    appointTime = LocalDateTime.now();
                } else {
                    startTime = LocalTime.parse("06:00:00");
                    appointTime = LocalDateTime.now();
                }
                finalDateYyyyMmDdStr = appointTime.format(DateTimeFormatter
                        .BASIC_ISO_DATE).concat("_").concat(dateYyyyMmDdStr);
            } else {
                startTime = LocalTime.parse(shuHeTransferJobStartTime);
                appointTime = null;
                finalDateYyyyMmDdStr = dateYyyyMmDdStr;
            }
        }
        TransferFileTaskExample taskExample = new TransferFileTaskExample();
        Map<String, String> map = new HashMap<>();
        List<String> stringList = userTypes.stream().map(s -> {
            String fileName = getFileName(s, apiCode, finalDateYyyyMmDdStr, EXTENSION);
            map.put(fileName, s);
            return fileName;
        }).collect(Collectors.toList());
        taskExample.createCriteria().andApiCodeEqualTo(apiCode).andStartDateEqualTo(dateYyyyMmDdStr)
                .andFileNameIn(stringList);
        List<TransferFileTask> list = transferFileTaskMapper.selectByExample(taskExample);
        for (TransferFileTask task : list) {
            userTypes.remove(map.get(task.getFileName()));
        }
        if (LocalTime.now().isAfter(startTime) && userTypes.size() > 0) {
            // 将配置中的有效期处理成天
            Map<String, Integer> dataExtractMap = dataExtractDateHandle(shuHeTransferDataExtractMap, userTypes);
            //获取自动化配置的有效期配置存入内存


            userTypes.forEach(userType -> {
                TransferFileTask transferFileTask = new TransferFileTask();
                long contextId = System.currentTimeMillis();
                transferFileTask.setApiCode(apiCode);
                transferFileTask.setFileType(dataExtractMap.getOrDefault(userType, null));
                transferFileTask.setBatchNumber(userType);
                String fileName;
                if (appointTime != null) {
                    // 指定日期
                    fileName = getFileName(userType, apiCode, appointTime.format(DateTimeFormatter.BASIC_ISO_DATE).concat("_")
                        .concat(dateYyyyMmDdStr), EXTENSION);
                } else {
                    // 前一天
                    fileName = getFileName(userType, apiCode, dateYyyyMmDdStr, EXTENSION);
                }
                transferFileTask.setFileName(fileName);
                transferFileTask.setStartDate(dateYyyyMmDdStr);
                transferFileTask.setContextId(contextId);
                transferFileTask.setTaskNumber(0);
                transferFileTask.setStatus(1);
                transferFileTask.setCreateTime(new Date());
                transferFileTask.setUpdateTime(transferFileTask.getCreateTime());
                transferFileTaskMapper.insertSelective(transferFileTask);
                transferFileTaskList.add(transferFileTask);
            });
        }
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
        Map<String, Integer> dataExtractMap = new HashMap<>(
                (int) (shuHeTransferDataExtractMap.size() / 0.75 + 1));
        userTypes.forEach(userType -> {
            Matcher matcher = PATTERN.matcher(shuHeTransferDataExtractMap.get(userType));
            if (matcher.find()) {
                String day = matcher.group();
                dataExtractMap.put(userType, new BigDecimal(day).setScale(0
                        , BigDecimal.ROUND_HALF_UP).intValue() - 1);
            } else {
                dataExtractMap.put(userType, null);

            }
        });
        return dataExtractMap;
    }


    /**
     * 获取上传表中案件编号最新的创建时间
     */
    private Map<String, Map<String, Object>> getSyncUserLatestUploadTime(
            List<MarketingTransferSyncUser> mtsuList, String apiCode, String userType) {
        Set<String> custNums = mtsuList.parallelStream().map(
                MarketingTransferSyncUser::getCustNum).collect(Collectors.toSet());
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

    private String getFileName(String userType, String apiCode, String dateYyyyMmDdStr, String extension) {
        return String.format(FILE_NAME_PART.getOrDefault(userType, "%s_".concat(userType).concat("_%s%s")), apiCode
                , dateYyyyMmDdStr, extension);
    }

    @Override
    public Result<Object> actionTransferToFile(TransferFileTask transferFileTask,String jobParameter) {
        Result<Object> result = new Result<>();
        String dateYyyyMmDdStr = transferFileTask.getStartDate();
        String apiCode = transferFileTask.getApiCode();
        String userType = transferFileTask.getBatchNumber();
        LocalDateTime first;
        LocalDateTime last;
        String fileName;
        if (appointTime != null) {
            // 指定日期
            last = appointTime;
            fileName = getFileName(userType, apiCode, appointTime.format(DateTimeFormatter.BASIC_ISO_DATE).concat("_")
                    .concat(dateYyyyMmDdStr), EXTENSION);
        } else {
            // 前一天
            last = LocalDateTime.now().minusDays(1);
            fileName = getFileName(userType, apiCode, dateYyyyMmDdStr, EXTENSION);
        }
        Boolean bool = marketingCommonConfig.getShuHeTransferExtractIfUseQuasiTotalQuantity();
        if (bool != null && bool) {
            // 准全量时间开始时间
            if (transferFileTask.getFileType() == null) {
                first = last.withDayOfMonth(1);
            } else {
                first = last.minusDays(transferFileTask.getFileType());
            }
        } else {
            // 前一天
            first = last;
        }
        Date firstDateTime = Date.from(first.toLocalDate().atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        Date lastDateTime = Date.from(last.withHour(23).withMinute(59).withSecond(59).withNano(0)
                .atZone(ZoneId.systemDefault()).toInstant());
        // 生成检索条件
        final String tCid = setCid(apiCode);
        int pageSize = 2000;
        String pattern = "yyyy-MM-dd HH:mm:ss";
        String sqlPart = String.format("(api_code = '%s'\n" +
                        " AND user_type = '%s'\n" +
                        " AND create_time BETWEEN '%s'\n" +
                        " AND '%s')\n" +
                        " ORDER BY\n" +
                        " id ASC\n" +
                        " LIMIT %s, %s", apiCode, userType, DateUtils.format(firstDateTime, pattern)
                , DateUtils.format(lastDateTime, pattern), "%s", pageSize);
        List<MarketingTransferSyncUser> list = null;
        String fileDirectory = syncConfigService.getPath().concat("transferToFile").concat(File.separator).concat(apiCode)
                .concat(File.separator).concat(dateYyyyMmDdStr).concat(File.separator);
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
        String separator = ",";
        String defaultValue = "";
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(filePtah, false), StandardCharsets.UTF_8))) {
            //final boolean booLType = "促复借".equals(userType);
            BiFunction<MarketingTransferSyncUser, Map<String, Object>, String> f;
            if ("促复借".equals(userType)) {
                writer.write(TABLE_HEADER_CUFUJIE.concat("\r\n"));
                f = (transfer, creatTimeMap) -> tableCuFuJie(transfer, creatTimeMap, separator, defaultValue);
            } else if("重申".equals(userType)){
                writer.write(TABLE_HEADER_CHONGSHEN.concat("\r\n"));
                f = (transfer, creatTimeMap) -> tableChongShen(transfer, creatTimeMap, separator, defaultValue);
            }else {
                writer.write(TABLE_HEADER.concat("\r\n"));
                f = (transfer, creatTimeMap) -> table(transfer, creatTimeMap, separator, defaultValue);
            }
            writer.flush();
            while (list == null || list.size() == pageSize) {
                list = marketingTransferSyncUserMapper.findShuHeTransferList(tCid
                        , String.format(sqlPart, (page * pageSize)));
                page++;
                if (list.size() < 1) {
                    break;
                }
                writerFile(apiCode, userType, list, transferFileTask, writer, f);
            }
            transferFileTask.setBatchNumber(getFileName(userType, apiCode, dateYyyyMmDdStr
                    , "_" + transferFileTask.getContextId()));
            transferFileTask.setStatus(2);
            transferFileTaskMapper.updateByPrimaryKeySelective(transferFileTask);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            result.setCode(ResultCode.FAIL.getValue());
            result.setDate(e.getMessage());
            return result;
        }
        result.setCode(ResultCode.SUCCESS.getValue());
        return result;
    }

    /**
     * 写入文件
     */
    private void writerFile(String apiCode, String userType, List<MarketingTransferSyncUser> list,
                            TransferFileTask transferFileTask, BufferedWriter writer
            , BiFunction<MarketingTransferSyncUser, Map<String, Object>, String> f)
            throws IOException {
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
            // 加入判断，如果是促复借，使用新版有效期判断
            if ("促复借".equals(userType)){
                LocalDate localDate = LocalDate.now().minusDays(1L);
                String requestData = localDate.toString();
                Set<String> set = list.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toSet());
                //判断转化数据是否在有效期内
                Map<String, SyncUserValidityPeriodsBO> validityPeriodsByCustNum = validityPeriodService
                        .getValidityPeriodsByCustNumAndUserType(set, userType, apiCode, requestData);
                SyncUserValidityPeriodsBO boMap = validityPeriodsByCustNum.get(transferSyncUser.getCustNum());
                if (boMap == null) {
                    log.warn("{}不满足案件编号“有效期内”条件", transferSyncUser.getCustNum());
                    continue;
                }
                transferFileTask.setTaskNumber(transferFileTask.getTaskNumber() + 1);
                String sb = f.apply(transferSyncUser, creatTimeMap);
                writer.write(sb);
                writer.flush();
            } else{
                Boolean periodOfValidity = iMarketingSyncUserService.isPeriodOfValidity(
                        Date.from(appointTime == null
                                ? LocalDateTime.now().minusDays(1).atZone(ZoneId.systemDefault()).toInstant()
                                : appointTime.atZone(ZoneId.systemDefault()).toInstant())
                        , transferFileTask.getFileType(), (Date) creatTime);
                if (periodOfValidity) {
                    transferFileTask.setTaskNumber(transferFileTask.getTaskNumber() + 1);
                    String sb = f.apply(transferSyncUser, creatTimeMap);
                    writer.write(sb);
                    writer.flush();
                }
            }
        }
    }

    /**
     * 反序列化扩展字段
     */
    private JSONObject getReserveField(String reserveField) {
        return StringUtils.isEmpty(reserveField) ? new JSONObject() : JSONObject.parseObject(reserveField);
    }

    /**
     * 生成促复借数据
     */
    private String tableCuFuJie(MarketingTransferSyncUser transfer
            , Map<String, Object> creatTimeMap, String separator, String defaultValue) {
        JSONObject json = getReserveField(transfer.getReserveField1());
        return transfer.getApiCode()
                + separator +
                creatTimeMap.getOrDefault("taskId", defaultValue)
                + separator +
                transfer.getUserType()
                + separator +
                transfer.getCustNum()
                + separator +
                Sha256Util.getSHA256Encrypt(BrCipherMaker.getInstance().decode(
                        String.valueOf(getOrDefault(json, "cell"))))
                + separator +
                getOrDefault(json, "is_turn")
                + separator +
                getOrDefault(json, "is_black")
                + separator +
                getOrDefault(json, "clc_usr_lst_app_sta_tim")
                + separator +
                getOrDefault(json, "clc_usr_lst_non_dcp_trs_tim")
                + separator +
                getOrDefault(json, "off_usr_lst_ord_tim_all")
                + separator +
                getOrDefault(json, "clc_usr_avl_lmt_lv0")
                + separator +
                getOrDefault(json, "clc_usr_adt_lmt_lv0")
                + separator +
                (ObjectUtils.isEmpty(transfer.getCreateTime()) ? defaultValue
                        : DateUtils.format(transfer.getCreateTime(), "yyyy-MM-dd HH:mm:ss"))
                + separator +
                getOrDefault(json, "clc_usr_lst_ord_tim_all_wizard")
                + separator +
                getOrDefault(json, "clc_usr_adt_lmt_fst_all")
                + separator +
                getOrDefault(json, "clc_usr_lst_adt_apy_tim_hvy")
                + "\r\n";
    }

    /**
     * 生成重申数据
     */
    private String tableChongShen(MarketingTransferSyncUser transfer
            , Map<String, Object> creatTimeMap, String separator, String defaultValue) {
        JSONObject json = getReserveField(transfer.getReserveField1());
        return transfer.getApiCode()
                + separator +
                creatTimeMap.getOrDefault("taskId", defaultValue)
                + separator +
                transfer.getUserType()
                + separator +
                transfer.getCustNum()
                + separator +
                Sha256Util.getSHA256Encrypt(BrCipherMaker.getInstance().decode(
                        String.valueOf(getOrDefault(json, "cell"))))
                + separator +
                getOrDefault(json, "is_turn")
                + separator +
                getOrDefault(json, "is_black")
                + separator +
                getOrDefault(json, "clc_usr_max_dx_rrt_end")
                + separator +
                getOrDefault(json, "clc_usr_lst_app_sta_tim")
                + separator +
                getOrDefault(json, "clc_usr_iso_pho_tim")
                + separator +
                getOrDefault(json, "clc_usr_iso_idt_tim")
                + separator +
                getOrDefault(json, "clc_usr_iso_crd_tim")
                + separator +
                getOrDefault(json, "clc_usr_iso_inf_tim")
                + separator +
                ("".equals(getOrDefault(json, "clc_usr_lst_adt_apy_tim_hvy")) ? getOrDefault(json, "clc_usr_grp_zjy_csx_sjs_yzz_cqc_jxd_c2") : getOrDefault(json, "clc_usr_lst_adt_apy_tim_hvy"))
                + separator +
                getOrDefault(json, "clc_usr_lst_reaudit_apply_time")
                + separator +
                getOrDefault(json, "clc_usr_adt_tim_rcn_lon_wo_asset_label")
                + separator +
                (ObjectUtils.isEmpty(transfer.getCreateTime()) ? defaultValue
                        : DateUtils.format(transfer.getCreateTime(), "yyyy-MM-dd HH:mm:ss"))
                + "\r\n";
    }

    /**
     * 生成数据
     */
    private String table(MarketingTransferSyncUser transfer
            , Map<String, Object> creatTimeMap, String separator, String defaultValue) {
        JSONObject json = getReserveField(transfer.getReserveField1());
        return transfer.getApiCode()
                + separator +
                creatTimeMap.getOrDefault("taskId", defaultValue)
                + separator +
                transfer.getUserType()
                + separator +
                transfer.getCustNum()
                + separator +
                Sha256Util.getSHA256Encrypt(BrCipherMaker.getInstance().decode(
                        String.valueOf(getOrDefault(json, "cell"))))
                + separator +
                getOrDefault(json, "is_turn")
                + separator +
                getOrDefault(json, "is_black")
                + separator +
                (StringUtils.isEmpty(transfer.getLoginTime()) ? defaultValue : transfer.getLoginTime())
                + separator +
                getOrDefault(json, "clc_usr_lst_app_sta_tim")
                + separator +
                getOrDefault(json, "clc_usr_iso_pho_tim")
                + separator +
                getOrDefault(json, "clc_usr_iso_idt_tim")
                + separator +
                getOrDefault(json, "clc_usr_iso_crd_tim")
                + separator +
                getOrDefault(json, "clc_usr_iso_inf_tim")
                + separator +
                (StringUtils.isEmpty(transfer.getApplyTime()) ? defaultValue : transfer.getApplyTime())
                + separator +
                (StringUtils.isEmpty(transfer.getAuditTime()) ? defaultValue : transfer.getAuditTime())
                + separator +
                (StringUtils.isEmpty(transfer.getAuditAmount()) ? defaultValue : transfer.getAuditAmount())
                + separator +
                getOrDefault(json, "applyLoanTime") + separator +
                (StringUtils.isEmpty(transfer.getLentTime()) ? defaultValue : transfer.getLentTime())
                + separator +
                getOrDefault(json, "clc_usr_adt_tim_rcn_lon_wo_asset_label")
                + separator +
                (ObjectUtils.isEmpty(transfer.getCreateTime()) ? defaultValue
                        : DateUtils.format(transfer.getCreateTime(), "yyyy-MM-dd HH:mm:ss"))
                + "\r\n";
    }

    /**
     * 添加默认值
     */
    private String getOrDefault(JSONObject reserveField1Json, String key) {
        return reserveField1Json.getOrDefault(key, "").toString();
    }
}
