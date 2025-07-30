package com.br.marketing.service.Impl.transfertofile;

import com.alibaba.fastjson.JSONObject;
import com.br.common.encryption.Sha256Util;
import com.br.common.util.BrCipherMaker;
import com.br.common.util.DateUtils;
import com.br.marketing.bo.SyncUserValidityPeriodsBO;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.mapper.TransferFileTaskMapper;
import com.br.marketing.service.ITransferToFileService;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.service.SyncConfigService;
import com.br.marketing.service.TransferDataValidityPeriodService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.BiFunction;
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
 *
 * 业务需求更新：2024年7月15日
 * D20240703数禾全场景取值逻辑&有效期变更-337
 * https://c.100credit.cn/pages/viewpage.action?pageId=166647068
 * @version v3
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
    private TableCreateServiceImpl tableCreateService;

    @Resource
    private RedisChgService redisChgService;

    @Resource
    private TransferDataValidityPeriodService transferDataValidityPeriodService;

    private final static Map<String, String> FILE_NAME_PART;

    @Resource
    private SyncConfigService syncConfigService;

    private final static String EXTENSION = ".txt";
    private final static String TABLE_HEADER = "apicode,taskid,usertype,custNum,cell,is_turn,is_black" +
            ",loginTime,clc_usr_lst_app_sta_tim,clc_usr_iso_pho_tim,clc_usr_iso_idt_tim,clc_usr_iso_crd_tim" +
            ",clc_usr_iso_inf_tim,applyTime,auditTime,auditAmount,applyLoanTime,lentTime,clc_usr_adt_tim_rcn_lon_wo_asset_label,insertime";

    private final static String TABLE_HEADER_CUFUJIE = "apicode,taskid,groupType,cust_num,cell,is_turn,is_black" +
            ",clc_usr_lst_app_sta_tim,clc_usr_lst_non_dcp_trs_tim,off_usr_lst_ord_tim_all,clc_usr_avl_lmt_lv0" +
            ",clc_usr_adt_lmt_lv0,clc_usr_adt_tim_rcn_lon_wo_asset_label,createtime";

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
    public Result<List<TransferFileTask>> buildTransferTask(String apiCode, String myParam) {
        List<TransferFileTask> transferFileTaskList = new ArrayList<>();
        Result<List<TransferFileTask>> result = new Result<>();
        result.setDate(transferFileTaskList);
        result.setCode(ResultCode.SUCCESS.getValue());
        String shuHeTransferJobStartTime = marketingCommonConfig.getShuHeTransferExtractJobStartTime();
        String dateYyyyMmDdStr = LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        Set<String> userTypes = new HashSet<>(marketingCommonConfig.getShuHeTransferExtractApiCodes().get(apiCode));
        if (CollectionUtils.isEmpty(userTypes)) {
            log.warn("数禾{}数据未提取成功，配置项中“shuHeTransferExtractApiCodes”未配置内容！myParam:{}", apiCode, myParam);
            return result;
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
            userTypes.forEach(userType -> {
                TransferFileTask transferFileTask = new TransferFileTask();
                long contextId = System.currentTimeMillis();
                transferFileTask.setApiCode(apiCode);
                transferFileTask.setFileType(1);
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

    private String getFileName(String userType, String apiCode, String dateYyyyMmDdStr, String extension) {
        return String.format(FILE_NAME_PART.getOrDefault(userType, "%s_".concat(userType).concat("_%s%s")), apiCode
                , dateYyyyMmDdStr, extension);
    }

    @Override
    public Result<Object> actionTransferToFile(TransferFileTask transferFileTask, String jobParameter) {
        Result<Object> result = new Result<>();
        String dateYyyyMmDdStr = transferFileTask.getStartDate();
        String apiCode = transferFileTask.getApiCode();
        String userType = transferFileTask.getBatchNumber();
        LocalDateTime localDateTime = LocalDateTime.now().minusDays(1);
        List<MarketingDataValidConfig> validConfigs;
        String fileName;
        if (appointTime != null) {
            validConfigs = new ArrayList<>();
            // 指定日期
            fileName = getFileName(userType, apiCode, appointTime.format(DateTimeFormatter.BASIC_ISO_DATE).concat("_")
                    .concat(dateYyyyMmDdStr), EXTENSION);
            MarketingDataValidConfig validConfig = new MarketingDataValidConfig();
            validConfig.setValidStartDate(appointTime.format(DateTimeFormatter.ISO_LOCAL_DATE));
            validConfig.setValidEndDate(validConfig.getValidStartDate());
            validConfigs.add(validConfig);
        } else {
            fileName = getFileName(userType, apiCode, dateYyyyMmDdStr, EXTENSION);
            validConfigs = transferDataValidityPeriodService.getDataMergeValidityPeriodList(
                    apiCode, userType, localDateTime);
        }
        String tCid = setCid(apiCode);
        int pageSize = 2000;
        List<MarketingTransferSyncUser> list = null;
        String fileDirectory = syncConfigService.getPath().concat("transferToFile").concat(File.separator).concat(apiCode)
                .concat(File.separator).concat(dateYyyyMmDdStr).concat(File.separator);
        final File filePath = new File(fileDirectory);
        if (!filePath.exists()) {
            if (!filePath.mkdirs()) {
                log.error("创建文件目录“{}”失败", fileDirectory);
            }
        }
        File filePtah = new File(fileDirectory, fileName);
        transferFileTask.setFilePath(fileDirectory);
        transferFileTask.setFileName(fileName);
        String separator = ",";
        String defaultValue = "";
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(filePtah, false), StandardCharsets.UTF_8))) {
            //final boolean booLType = "促复借".equals(userType);
            BiFunction<MarketingTransferSyncUser, MarketingSyncUser, String> f;
            if ("促复借".equals(userType)) {
                writer.write(TABLE_HEADER_CUFUJIE.concat("\r\n"));
                f = (transfer, marketingSyncUser) -> tableCuFuJie(transfer, marketingSyncUser, separator, defaultValue);
            } else if ("重申".equals(userType)) {
                String head = marketingCommonConfig.getShuHeChongShenTransferTableHead();
                writer.write(head.concat("\r\n"));
                f = (transfer, marketingSyncUser) -> tableChongShen(transfer, marketingSyncUser, separator, defaultValue, head);
            } else {
                writer.write(TABLE_HEADER.concat("\r\n"));
                f = (transfer, marketingSyncUser) -> table(transfer, marketingSyncUser, separator, defaultValue);
            }
            for (MarketingDataValidConfig validConfig : validConfigs) {
                writer.flush();
                // 生成检索条件
                MarketingTransferSyncUserExample example = new MarketingTransferSyncUserExample();
                example.settCid(tCid);
                example.createCriteria().andApiCodeEqualTo(apiCode).andUserTypeEqualTo(userType)
                        .andRequestDataGreaterThan(validConfig.getValidStartDate())
                        .andRequestDataLessThanOrEqualTo(validConfig.getValidEndDate() + " 23:59:59");
                Long maxId = 0L;
                int size = 0;
                while (list == null || size == pageSize) {
                    list = marketingTransferSyncUserMapper.selectByExampleAndMaxIdList(example, maxId, pageSize);
                    if (list.isEmpty()) {
                        break;
                    }
                    size = list.size();
                    maxId = list.get(size - 1).getId();
                    writerFile(apiCode, list, transferFileTask, writer, f);
                }
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
    private void writerFile(String apiCode, List<MarketingTransferSyncUser> list, TransferFileTask transferFileTask
            , BufferedWriter writer, BiFunction<MarketingTransferSyncUser, MarketingSyncUser, String> f) throws IOException {
        Set<String> custNumSet = list.stream().map(MarketingTransferSyncUser::getCustNum).collect(Collectors.toSet());
        Map<String, SyncUserValidityPeriodsBO> periods = transferDataValidityPeriodService.getValidityPeriodsByCustNum(
                custNumSet, apiCode, appointTime == null ? LocalDate.now().minusDays(1) : appointTime);
        for (MarketingTransferSyncUser transferSyncUser : list) {
            SyncUserValidityPeriodsBO userValidityPeriodsBO = periods.get(transferSyncUser.getCustNum());
            if (userValidityPeriodsBO == null) {
                continue;
            }
            transferFileTask.setTaskNumber(transferFileTask.getTaskNumber() + 1);
            MarketingSyncUser marketingSyncUser = userValidityPeriodsBO.getSyncUsers().get(0);
            String sb = f.apply(transferSyncUser, marketingSyncUser);
            writer.write(sb);
            writer.flush();
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
            , MarketingSyncUser marketingSyncUser, String separator, String defaultValue) {
        JSONObject json = getReserveField(transfer.getReserveField1());
        return transfer.getApiCode()
                + separator +
                marketingSyncUser.getCusBatch()
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
                getOrDefault(json, "clc_usr_adt_tim_rcn_lon_wo_asset_label")
                + separator +
                (ObjectUtils.isEmpty(transfer.getCreateTime()) ? defaultValue
                        : DateUtils.format(transfer.getCreateTime(), "yyyy-MM-dd HH:mm:ss"))
                + "\r\n";
    }

    /**
     * 生成重申数据
     */
    private String tableChongShen(MarketingTransferSyncUser transfer,
                                  MarketingSyncUser marketingSyncUser, String separator, String defaultValue, String head) {

        // 获取 JSON 数据
        JSONObject json = getReserveField(transfer.getReserveField1());

        // 前5个固定字段（按原逻辑）
        StringBuilder result = new StringBuilder();
        result.append(transfer.getApiCode()).append(separator);
        result.append(marketingSyncUser.getCusBatch()).append(separator);
        result.append(transfer.getUserType()).append(separator);
        result.append(transfer.getCustNum()).append(separator);
        result.append(Sha256Util.getSHA256Encrypt(BrCipherMaker.getInstance().decode(String.valueOf(getOrDefault(json, "cell"))))).append(separator);

        // 解析 head，获取后续字段的顺序
        String[] headers = head.split(",");

        // 从第6个字段开始，按 head 定义的顺序从 json 取值
        for (int i = 5; i < headers.length; i++) {
            String field = headers[i];

            // 特殊处理：auditTime 字段（原逻辑）
            if ("auditTime".equals(field)) {
                String auditTimeValue = ("".equals(getOrDefault(json, "clc_usr_lst_adt_apy_tim_hvy"))
                        ? getOrDefault(json, "clc_usr_grp_zjy_csx_sjs_yzz_cqc_jxd_c2")
                        : getOrDefault(json, "clc_usr_lst_adt_apy_tim_hvy"));
                result.append(auditTimeValue);
            }
            // 特殊处理：createtime 字段（原逻辑）
            else if ("createtime".equals(field)) {
                String createTimeValue = (ObjectUtils.isEmpty(transfer.getCreateTime())
                        ? defaultValue
                        : DateUtils.format(transfer.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
                result.append(createTimeValue);
            }
            // 其他字段：直接从 json 获取
            else {
                result.append(getOrDefault(json, field));
            }

            // 添加分隔符（最后一个字段不加）
            if (i < headers.length - 1) {
                result.append(separator);
            }
        }

        // 换行符
        result.append("\r\n");
        return result.toString();
    }

    /**
     * 生成数据
     */
    private String table(MarketingTransferSyncUser transfer
            , MarketingSyncUser marketingSyncUser, String separator, String defaultValue) {
        JSONObject json = getReserveField(transfer.getReserveField1());
        return transfer.getApiCode()
                + separator +
                marketingSyncUser.getCusBatch()
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
                getOrDefault(transfer.getLoginTime(), defaultValue)
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
                getOrDefault(transfer.getApplyTime(), defaultValue)
                + separator +
                getOrDefault(transfer.getAuditTime(), defaultValue)
                + separator +
                getOrDefault(transfer.getAuditAmount(), defaultValue)
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

    /**
     * 2024-07-16 11:02
     * null 值设置默认值
     */
    private String getOrDefault(String value, String defaultValue) {
        return (StringUtils.isEmpty(value) ? defaultValue : value);
    }
}
