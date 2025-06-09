package com.br.marketing.service.carclue.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.FastDfsClient;
import com.br.marketing.client.HttpProxyClient;
import com.br.marketing.client.carclue.CarClueClient;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.*;
import com.br.marketing.service.SyncConfigService;
import com.br.marketing.service.carclue.ChannelRelationalService;
import com.br.marketing.service.carclue.clueenums.CarInformationTypeEnum;
import com.br.marketing.service.carclue.clueenums.ChannelRule;
import com.br.marketing.service.carclue.clueenums.ClueFileRecordingStatusEnum;
import com.br.marketing.service.carclue.clueenums.ProvinceTypeEnum;
import com.br.marketing.service.carclue.config.AbstractClueChannelConfig;
import com.br.marketing.service.carclue.strategy.ClueChannelConfigService;
import com.br.marketing.service.carclue.web.impl.CarClueReportServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @ClassName ChannelRelationalServiceImpl
 * @Description 外采渠道映射关系维护
 * @Author kongbx
 * @Date 2025/1/19 17:15
 */
@Service
@Slf4j
public class ChannelRelationalServiceImpl implements ChannelRelationalService {

    @Value(value = "${api.ycKA.carList:'https://car.s.zonrn.cn/api/yiPlanDown'}")
    private String ycKaUrl;

    @Value("${api.ycKA.isProxy:true}")
    private Boolean isProxy;

    @Resource
    CarClueClient carClueClient;
    @Resource
    CarClueProvincesInformationMapper carClueProvincesInformationMapper;
    @Resource
    CarClueSeriesInformationMapper carClueSeriesInformationMapper;
    @Resource
    CarClueInitMappingMapper carClueInitMappingMapper;
    @Resource
    CarClueRelationalMappingMapper carClueRelationalMappingMapper;
    @Resource
    CarClueSupplementMapper carClueSupplementMapper;
    @Resource
    CarChannelConfigMapper carChannelConfigMapper;
    @Resource
    CarClueManageConfigMapper carClueManageConfigMapper;
    @Resource
    ClueFileRecordingMapper clueFileRecordingMapper;
    @Resource
    private FastDfsClient fastDfsClient;
    @Resource
    CarClueReportServiceImpl carClueReportServiceImpl;
    @Autowired
    ClueChannelConfigService clueChannelConfigService;
    @Autowired
    SyncConfigService syncConfigService;
    @Autowired
    HttpProxyClient httpProxyClient;
    private static final String YCKATASK = "7-1";
    private static final String YCMEMBERTASK = "6+";
    public static final String ALL_SERVIES = "全系";
    private static final String TITL = "【车线索外采数据相关-】";

    /**
     * ============================== 获取省市/车辆 初始字典信息 ==============================
     */
    @Override
    public void getProvinceAndCity() {
        // 获取所有有效渠道配置
        CarChannelConfigExample example = new CarChannelConfigExample();
        example.createCriteria().andIsDelEqualTo(Constants.DATA_VALID);
        List<CarChannelConfig> channelConfigs = carChannelConfigMapper.selectByExample(example);

        // 处理每个渠道的省市信息和车辆信息
        channelConfigs.forEach(this::processChannelInfo);
    }

    /**
     * 处理单个渠道的省市和车辆信息
     */
    private void processChannelInfo(CarChannelConfig config) {
        String apiCode = config.getApiCode();

        // 处理省市信息
        if (!hasProvinceInfo(apiCode)) {
            buildProvinceInfo(config);
        }

        // 处理车辆信息
        if (!hasCarInfo(apiCode)) {
            buildCarInfo(config);
        }
    }

    /**
     * 检查今日是否已存在该渠道的省市信息
     */
    private boolean hasProvinceInfo(String apiCode) {
        CarClueProvincesInformationExample example = new CarClueProvincesInformationExample();
        example.createCriteria()
                .andApiCodeEqualTo(apiCode)
                .andAppletDateEqualTo(LocalDate.now().toString());
        return carClueProvincesInformationMapper.countByExample(example) > 0;
    }

    /**
     * 检查今日是否已存在该渠道的车辆信息
     */
    private boolean hasCarInfo(String apiCode) {
        CarClueSeriesInformationExample example = new CarClueSeriesInformationExample();
        example.createCriteria()
                .andApiCodeEqualTo(apiCode)
                .andAppletDateEqualTo(LocalDate.now().toString());
        return carClueSeriesInformationMapper.countByExample(example) > 0;
    }

    /**
     * 构建省市信息
     */
    private void buildProvinceInfo(CarChannelConfig config) {
        String strategyConfigInfo = config.getStrategyConfigInfo();
        if (ChannelRule.ConfigChannelRuleEnum.ZJ_CONFIG.getLabel().equals(strategyConfigInfo)) {
            buildZjCity();
        } else if (ChannelRule.ConfigChannelRuleEnum.YC_KA_CONFIG.getLabel().equals(strategyConfigInfo)) {
            buildYcCity(YCKATASK, ChannelRule.MatchChannelRuleEnum.YC_KA.getLabel());
        } else {
            buildYcCity(YCMEMBERTASK, ChannelRule.MatchChannelRuleEnum.YC_MEMBER.getLabel());
        }
    }

    /**
     * 构建车辆信息
     */
    private void buildCarInfo(CarChannelConfig config) {
        String strategyConfigInfo = config.getStrategyConfigInfo();
        if (ChannelRule.ConfigChannelRuleEnum.ZJ_CONFIG.getLabel().equals(strategyConfigInfo)) {
            buildZjCar();
        } else if (ChannelRule.ConfigChannelRuleEnum.YC_KA_CONFIG.getLabel().equals(strategyConfigInfo)) {
            buildYcCar(YCKATASK, ChannelRule.MatchChannelRuleEnum.YC_KA.getLabel());
        } else {
            buildYcCar(YCMEMBERTASK, ChannelRule.MatchChannelRuleEnum.YC_MEMBER.getLabel());
        }
    }

    private void buildZjCity() {
        Result<JSONArray> zjCityResult = carClueClient.getZjCity();

        if (!ResultCode.SUCCESS.getValue().equals(zjCityResult.getCode())) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.CARCLUE_SERVICEERROR.getCode(),
                    TITL + "之家，省市调用异常, result：" + zjCityResult.getMessage()));
            return;
        }
        JSONArray jsonArray = zjCityResult.getData();
        if (jsonArray == null || jsonArray.isEmpty()) {
            log.warn(TITL + "之家，省市调用异常，返回数据为空");
            return;
        }
        List<String> apiCodes = carClueReportServiceImpl.getValueByKey
                (ChannelRule.MatchChannelRuleEnum.ZJ.getLabel());
        List<CarClueProvincesInformation> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject firstData = jsonArray.getJSONObject(i);
            Integer provinceId = firstData.getInteger("id");
            String provinceName = firstData.getString("name");
            JSONArray nodesArray = firstData.getJSONArray("nodes");
            for (int j = 0; j < nodesArray.size(); j++) {
                JSONObject node = nodesArray.getJSONObject(j);
                Integer cityId = node.getInteger("id");
                String cityName = node.getString("name");
                for (String apiCode : apiCodes) {
                    CarClueProvincesInformation info = new CarClueProvincesInformation();
                    info.setApiCode(apiCode);
                    info.setProvinceId(provinceId);
                    info.setProvinceName(provinceName);
                    info.setCityId(cityId);
                    info.setCityName(cityName);
                    list.add(info);
                }
                if (list.size() >= 500) {
                    carClueProvincesInformationMapper.batchInsert(list);
                    list.clear();
                }
            }
        }
        // 插入剩余的数据
        if (!list.isEmpty()) {
            carClueProvincesInformationMapper.batchInsert(list);
        }
    }

    private void buildYcCity(String task, String provincesType) {
        Result<JSONArray> ycCityResult = carClueClient.getYcCity(task);

        if (!ResultCode.SUCCESS.getValue().equals(ycCityResult.getCode())) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.CARCLUE_SERVICEERROR.getCode(),
                    TITL + "易车，省市调用异常, provincesType:" + provincesType + ", result:" + ycCityResult.getMessage()));
            return;
        }
        JSONArray jsonArray = ycCityResult.getData();
        if (jsonArray == null || jsonArray.isEmpty()) {
            log.warn(TITL + "易车，省市调用异常，返回数据为空  provincesType = {},", provincesType);
            return;
        }
        List<String> apiCodes = carClueReportServiceImpl.getValueByKey(provincesType);
        List<CarClueProvincesInformation> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject firstData = jsonArray.getJSONObject(i);
            for (String apiCode : apiCodes) {
                CarClueProvincesInformation info = new CarClueProvincesInformation();
                info.setApiCode(apiCode);
                info.setProvinceId(firstData.getInteger("provinceId"));
                info.setProvinceName(firstData.getString("provinceName"));
                info.setCityId(firstData.getInteger("cityId"));
                info.setCityName(firstData.getString("cityName"));
                list.add(info);
            }
            if (list.size() >= 500) {
                carClueProvincesInformationMapper.batchInsert(list);
                list.clear();
            }
        }
        // 插入剩余的数据
        if (!list.isEmpty()) {
            carClueProvincesInformationMapper.batchInsert(list);
        }
    }

    private void buildZjCar() {
        Result<JSONArray> zjCarResult = carClueClient.getZjCar();

        if (!ResultCode.SUCCESS.getValue().equals(zjCarResult.getCode())) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.CARCLUE_SERVICEERROR.getCode(),
                    TITL + "之家，车辆信息获取异常, result：" + zjCarResult.getMessage()));
            return;
        }
        JSONArray jsonArray = zjCarResult.getData();
        if (jsonArray == null || jsonArray.isEmpty()) {
            log.warn(TITL + "之家，车辆信息获取异常，返回数据为空");
            return;
        }
        List<String> apiCodes = carClueReportServiceImpl.getValueByKey
                (ChannelRule.MatchChannelRuleEnum.ZJ.getLabel());
        List<CarClueSeriesInformation> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject firstData = jsonArray.getJSONObject(i);
            for (String apiCode : apiCodes) {
                CarClueSeriesInformation info = new CarClueSeriesInformation();
                info.setApiCode(apiCode);
                info.setBrandId(firstData.getInteger("brand_id"));
                info.setBrandName(firstData.getString("brand_name"));
                info.setSubBrandId(firstData.getInteger("son_brand_id"));
                info.setSubBrandName(firstData.getString("son_brand_name"));
                info.setSeriesId(firstData.getInteger("series_id"));
                info.setSeriesName(firstData.getString("series_name"));
                list.add(info);
            }
            if (list.size() >= 500) {
                carClueSeriesInformationMapper.batchInsert(list);
                list.clear();
            }
        }
        // 插入剩余的数据
        if (!list.isEmpty()) {
            carClueSeriesInformationMapper.batchInsert(list);
        }
    }

    private void buildYcCar(String task, String provincesType) {
        // 1. 获取易车车辆数据
        Result<JSONArray> ycCarResult = carClueClient.getYcCar(task);

        if (!ResultCode.SUCCESS.getValue().equals(ycCarResult.getCode())) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.CARCLUE_SERVICEERROR.getCode(),
                    TITL + "易车，车辆信息获取异常, provincesType:" + provincesType + ", result:" + ycCarResult.getMessage()));
            return;
        }

        JSONArray jsonArray = ycCarResult.getData();
        if (jsonArray == null || jsonArray.isEmpty()) {
            log.warn(TITL + "易车，车辆信息获取异常，返回数据为空, provincesType = {}", provincesType);
            return;
        }

        List<String> apiCodes = carClueReportServiceImpl.getValueByKey(provincesType);
        List<CarClueSeriesInformation> list = new ArrayList<>();

        // 2. 用于去重的Set
        Set<Integer> processedSeriesIds = new HashSet<>();

        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject firstData = jsonArray.getJSONObject(i);
            Integer seriesId = firstData.getInteger("seriesId");

            // 3. 检查是否已处理过该seriesId
            if (processedSeriesIds.contains(seriesId)) {
                continue;
            }
            processedSeriesIds.add(seriesId);

            for (String apiCode : apiCodes) {
                CarClueSeriesInformation info = new CarClueSeriesInformation();
                info.setApiCode(apiCode);
                info.setBrandId(firstData.getInteger("brandId"));
                info.setBrandName(firstData.getString("brandName"));
                info.setSeriesId(seriesId);
                info.setSeriesName(firstData.getString("seriesName"));
                list.add(info);
            }

            // 批量插入
            if (list.size() >= 500) {
                carClueSeriesInformationMapper.batchInsert(list);
                list.clear();
            }
        }
        // 4. 插入剩余的数据
        if (!list.isEmpty()) {
            carClueSeriesInformationMapper.batchInsert(list);
        }
    }

    /**
     * ============================== 处理当天的 易车KA 外采初始配置 ==============================
     */
    @Override
    public void getInitMapping() {

        // 1. 判断今日是否已经生成易车KA初始配置
        CarChannelConfigExample example = new CarChannelConfigExample();
        example.createCriteria()
                .andStrategyConfigInfoEqualTo(ChannelRule.ConfigChannelRuleEnum.YC_KA_CONFIG.getLabel());
        List<CarChannelConfig> carChannelConfigs = carChannelConfigMapper.selectByExample(example);

        if (CollectionUtils.isEmpty(carChannelConfigs)) {
            log.warn(TITL + "缺少易车KA配置");
            return;
        }
        CarChannelConfig carChannelConfig = carChannelConfigs.get(0);
        String apiCode = carChannelConfig.getApiCode();
        CarClueInitMappingExample carClueInitMappingExample = new CarClueInitMappingExample();
        carClueInitMappingExample.createCriteria()
                .andApiCodeEqualTo(apiCode)
                .andAppletDateEqualTo(LocalDate.now().toString())
                .andIsDelEqualTo(Constants.DATA_VALID);

        int i = carClueInitMappingMapper.countByExample(carClueInitMappingExample);
        if (i > 0) {
            log.warn(TITL + "今日易车KA配置已更新");
            return;
        }
        // 2. 判断是否到了 易车KA每日文档的 拉取时间
        CarClueManageConfigExample carClueManageConfigExample = new CarClueManageConfigExample();
        carClueManageConfigExample.createCriteria().andIsDelEqualTo(Constants.DATA_VALID);

        List<CarClueManageConfig> configs = carClueManageConfigMapper.selectByExample(carClueManageConfigExample);
        if (CollectionUtils.isEmpty(configs)) {
            log.warn(TITL + "车线索配置管理为空！");
            return;
        }

        CarClueManageConfig config = configs.get(0);
        String pullDate = config.getPullDate();
        if (StringUtils.isEmpty(pullDate)) {
            log.warn(TITL + "未配置易车KA拉取时间！");
            return;
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
            LocalTime pullTime = LocalTime.parse(pullDate, formatter);
            // 获取当前时间
            LocalTime now = LocalTime.now();
            // 执行时间大于当前时间
            if (pullTime.isAfter(now)) {
                log.warn(TITL + "配置易车KA拉取每日文档时间还未到，拉取时间: {}，当前时间: {}", pullTime, now);
                return;
            }
        } catch (Exception e) {
            log.error(TITL + "时间格式解析错误，pullDate: {}", pullDate, e);
            return;
        }

        //拉取线上易车KA文档
        String syncDate = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String descPath = syncConfigService.getPath().concat("channel/").concat(syncDate).concat("/");
        String fileName = "易车KA" + "_" + syncDate + ".xls";
        String filePath = descPath.concat(fileName);
        try {
            //每日文档下载
            if (downloadFile(ycKaUrl, filePath)) {
                //解析文档
                parseKAFile(filePath, apiCode);

                //解析初始配置并清洗至映射表
                relationalMapping(apiCode);
            }
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.CARCLUE_SERVICEERROR.getCode(),
                    TITL + "文件下载失败：" + e.getMessage()));
        }
    }

    public boolean downloadFile(String fileUrl, String filePath) {

        HttpResponse response = httpProxyClient.downloadFile(fileUrl, isProxy);

        if (response == null) {
            return Boolean.FALSE;
        }
        // 检查响应码
        if (response.getStatusLine().getStatusCode() != 200) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.CARCLUE_SERVICEERROR.getCode(),
                    TITL + "线上文档拉取异常，返回响应：" + response.getStatusLine().getStatusCode()));
            return Boolean.FALSE;
        }

        // 获取文件大小
        HttpEntity entity = response.getEntity();
        // 创建目录（如果不存在）
        File file = new File(filePath);
        File parentDir = file.getParentFile();
        if (!parentDir.exists()) {
            parentDir.mkdirs();
        }
        // 下载文件
        log.warn(TITL + "下载文件目录：" + filePath);
        try (InputStream in = entity.getContent();
             FileOutputStream out = new FileOutputStream(filePath)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.CARCLUE_SERVICEERROR.getCode(),
                    TITL + "下载文件异常，返回响应：" + response.getStatusLine().getStatusCode()));
            return Boolean.FALSE;
        } finally {
            // 释放连接
            try {
                EntityUtils.consume(entity);
            } catch (IOException e) {
                log.warn(TITL + "释放连接异常");
                return Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
    }

    public void parseKAFile(String filePath, String apiCode) {
        String sql = "";
        try {
            FileInputStream file = new FileInputStream(filePath);
            Workbook workbook = new XSSFWorkbook(file);
            Sheet sheet = workbook.getSheetAt(0);
            List<String> valueStatements = new ArrayList<>();
            // 遍历每一行（跳过标题行）
            for (Row row : sheet) {
                // 跳过标题行
                if (row.getRowNum() == 0) continue;
                // 提取所需列的值（列索引从0开始）
                // A列：品牌
                String brand = getCellValue(row, 0);
                // B列：车型
                String series = getCellValue(row, 1);
                // C列：城市
                String cities = getCellValue(row, 2);
                // I列：日限量
                String dailyLimit = getCellValue(row, 8);
                // E列：需求ID
                String demandId = getCellValue(row, 4);

                if(StringUtils.isEmpty(brand) && StringUtils.isEmpty(series)){
                    continue;
                }
                // 构建VALUES部分
                String valueStatement = String.format(
                        "('%s', '%s', '%s', null, null, '%s', null, null, curdate(), now(), now(), 1, %s, '%s')",
                        apiCode,
                        escapeSql(brand),
                        escapeSql(series),
                        escapeSql(cities),
                        dailyLimit.isEmpty() ? "0" : dailyLimit,
                        escapeSql(demandId)
                );
                valueStatements.add(valueStatement);
            }
            workbook.close();
            file.close();
            // 构建完整的批量插入SQL
            sql = "INSERT INTO marketing.b_car_clue_init_mapping " +
                    "(api_code, brand_name, series_name, nation, satisfy_province_name, " +
                    "satisfy_city_name, exclude_province_name, exclude_city_name, applet_date, " +
                    "create_time, update_time, is_del, daily_limited, demand_id) " +
                    "VALUES " + String.join(", ", valueStatements) + ";";

            log.warn(TITL + "批量插入sql：" + sql);
            // 批量插入数据
            carClueRelationalMappingMapper.insertSql(sql);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.CARCLUE_SERVICEERROR.getCode(),
                    TITL + "批量插入外采数据异常，数据列表：" + sql, e.getMessage()));
        }
    }

    /**
     * 获取单元格值并处理空值
     *
     * @param row
     * @param cellIndex
     * @return
     */
    private static String getCellValue(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);

        if (cell.getCellType() == CellType.NUMERIC) {
            return String.valueOf((int) cell.getNumericCellValue());
        }

        // 获取单元格值并处理多行文本
        String value = cell.getStringCellValue().trim();

        // 替换换行符为逗号（可根据需要调整）
        value = value.replaceAll("\\r?\\n", ",");

        return value;
    }

    /**
     * 转义SQL中的特殊字符（如单引号）
     *
     * @param input
     * @return
     */
    private static String escapeSql(String input) {
        return input.replace("'", "''");
    }

    /**
     * ============================== 处理待清洗文档的 外采初始配置 ==============================
     */
    @Override
    public void getFileInitMapping() {
        // 1. 获取待清洗文件记录
        Optional<ClueFileRecording> fileRecordingOpt = getPendingCleanFile();
        if (!fileRecordingOpt.isPresent()) {
            return;
        }

        // 2. 处理文件
        ClueFileRecording recording = fileRecordingOpt.get();
        if(StringUtils.isEmpty(recording.getUpdateScope())){
            log.warn("{}待清洗文档配置缺少更新范围", TITL);
            return;
        }

        ClueFileRecording clueFileRecording = new ClueFileRecording();
        clueFileRecording.setId(recording.getId());
        clueFileRecording.setFileCleanStatus(ClueFileRecordingStatusEnum.CLEAN_ING.getValue());
        clueFileRecordingMapper.updateByPrimaryKeySelective(clueFileRecording);

        processFile(recording);
    }

    /**
     * 获取待清洗文件记录
     */
    private Optional<ClueFileRecording> getPendingCleanFile() {
        ClueFileRecordingExample example = new ClueFileRecordingExample();
        example.createCriteria()
                .andFileCleanStatusEqualTo(ClueFileRecordingStatusEnum.AWAIT_CLEAN.getValue())
                .andIsDelEqualTo(Constants.DATA_VALID);

        List<ClueFileRecording> recordings = clueFileRecordingMapper.selectByExample(example);

        if (CollectionUtils.isEmpty(recordings)) {
            log.warn("{}待清洗文档为空！", TITL);
            return Optional.empty();
        }

        if (recordings.size() > 1) {
            List<Long> ids = recordings.stream()
                    .map(ClueFileRecording::getId)
                    .collect(Collectors.toList());
            log.warn("{}待清洗文档有多个，id：{}", TITL, ids);
            return Optional.empty();
        }
        return Optional.of(recordings.get(0));
    }

    /**
     * 处理Excel文件
     * url: FastDFS文件路径
     * updateScope:清洗范围
     */
    private void processFile(ClueFileRecording recording) {
        String url = recording.getFileAdress();
        String updateScope = recording.getUpdateScope();

        try {
            // 从FastDFS获取文件流
            byte[] bytes = fastDfsClient.downloadFile(url);
            // 从字节数组输出流创建Workbook
            try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
                String[] channels = updateScope.split(",");
                for (String channel : channels) {
                    switch (channel.trim()) {
                        case "汽车之家":
                            processZjChannel(workbook);
                            break;
                        case "易车会员":
                            processYcMemberChannel(workbook);
                            break;
                        default:
                            log.warn("{}不支持的渠道类型：{}", TITL, channel);
                    }
                }
            }
            ClueFileRecording clueFileRecording = new ClueFileRecording();
            clueFileRecording.setId(recording.getId());
            clueFileRecording.setFileCleanStatus(ClueFileRecordingStatusEnum.CLEAN_FINISH.getValue());
            clueFileRecordingMapper.updateByPrimaryKeySelective(clueFileRecording);
        } catch (Exception e) {
            log.error("{}处理文件异常，文件路径：{}", TITL, url, e);
        }
    }

    /**
     * 处理汽车之家渠道数据
     */
    private void processZjChannel(Workbook workbook) {
        Sheet sheet = workbook.getSheet("汽车之家");
        if (sheet == null) {
            log.warn("{}汽车之家Sheet页不存在", TITL);
            return;
        }

        Optional<String> apiCodeOpt = getChannelApiCode(ChannelRule.ConfigChannelRuleEnum.ZJ_CONFIG.getLabel());
        if (!apiCodeOpt.isPresent()) {
            return;
        }
        // 解析文档数据 并 新增初始外采数据
        String apiCode = apiCodeOpt.get();
        parseSheetData(sheet, apiCode, false);
        // 解析初始外采数据 并 清洗至映射表
        relationalMapping(apiCode);
    }

    /**
     * 处理易车会员渠道数据
     */
    private void processYcMemberChannel(Workbook workbook) {
        Sheet sheet = workbook.getSheet("易车会员");
        if (sheet == null) {
            log.warn("{}易车会员Sheet页不存在", TITL);
            return;
        }

        Optional<String> apiCodeOpt = getChannelApiCode(ChannelRule.ConfigChannelRuleEnum.YC_MEMBER_CONFIG.getLabel());
        if (!apiCodeOpt.isPresent()) {
            return;
        }

        // 解析文档数据 并 新增初始外采数据
        String apiCode = apiCodeOpt.get();
        parseSheetData(sheet, apiCode, true);
        // 解析初始外采数据 并 清洗至映射表
        relationalMapping(apiCode);
    }

    /**
     * 获取渠道API代码
     */
    private Optional<String> getChannelApiCode(String strategyConfigInfo) {
        CarChannelConfigExample example = new CarChannelConfigExample();
        example.createCriteria()
                .andStrategyConfigInfoEqualTo(strategyConfigInfo);

        List<CarChannelConfig> configs = carChannelConfigMapper.selectByExample(example);

        if (CollectionUtils.isEmpty(configs)) {
            log.warn("{}缺少渠道配置，strategyConfigInfo：{}", TITL, strategyConfigInfo);
            return Optional.empty();
        }

        return Optional.of(configs.get(0).getApiCode());
    }

    /**
     * 解析Sheet数据
     */
    private void parseSheetData(Sheet sheet, String apiCode, boolean includeDemandId) {
        //删除今日已生成的数据
        deleteInitData(apiCode);

        // 验证表头
        if (!validateSheetHeaders(sheet, includeDemandId)) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.CARCLUE_SERVICEERROR.getCode(),
                    TITL + "Excel表头不符合要求，跳过处理"));
            return;
        }

        List<String> valueList = new ArrayList<>();

        for (Row row : sheet) {
            // 跳过标题行
            if (row.getRowNum() == 0) continue;

            String brand = getCellValue(row, 0);
            String series = getCellValue(row, 1);
            String nation = getCellValue(row, 2);
            String satisfyProvince = getCellValue(row, 3);
            String satisfyCity = getCellValue(row, 4);
            String excludeProvince = getCellValue(row, 5);
            String excludeCity = getCellValue(row, 6);
            if(StringUtils.isEmpty(brand) && StringUtils.isEmpty(series)){
                continue;
            }
            String valueStatement;
            if (includeDemandId) {
                String demandId = getCellValue(row, 7);
                valueStatement = String.format(
                        "('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', curdate(), now(), now(), 1, null, '%s')",
                        apiCode, escapeSql(brand), escapeSql(series), escapeSql(nation),
                        escapeSql(satisfyProvince), escapeSql(satisfyCity),
                        escapeSql(excludeProvince), escapeSql(excludeCity), escapeSql(demandId)
                );
            } else {
                valueStatement = String.format(
                        "('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s', curdate(), now(), now(), 1, null, null)",
                        apiCode, escapeSql(brand), escapeSql(series), escapeSql(nation),
                        escapeSql(satisfyProvince), escapeSql(satisfyCity),
                        escapeSql(excludeProvince), escapeSql(excludeCity)
                );
            }

            valueList.add(valueStatement);
        }
        executeBatchInsert(valueList);
    }

    /**
     * 验证表头是否符合要求
     * @param sheet Excel工作表
     * @param includeDemandId 是否包含需求ID列
     * @return 验证结果
     */
    private boolean validateSheetHeaders(Sheet sheet, boolean includeDemandId) {
        // 获取第一行作为表头
        Row headerRow = sheet.getRow(0);
        if (headerRow == null) {
            log.warn(TITL + "Excel文件缺少表头行");
            return false;
        }

        // 定义预期的表头列
        String[] expectedHeaders = {"品牌", "车系", "全国", "省份", "城市", "排除省份", "排除城市"};
        if (includeDemandId) {
            expectedHeaders = ArrayUtils.add(expectedHeaders, "会员ID");
        }

        // 验证每列的表头文本
        for (int i = 0; i < expectedHeaders.length; i++) {
            String cellValue = getCellValue(headerRow, i);
            if (!expectedHeaders[i].equals(cellValue)) {
                log.warn(TITL+"第{}列表头不符合要求，预期:'{}'，实际:'{}'",
                        i + 1, expectedHeaders[i], cellValue);
                return false;
            }
        }
        return true;
    }

    /**
     * 删除今日已生成的 外采初始数据
     */
    private void deleteInitData(String apiCode) {
        String date = LocalDate.now().toString();
        try {
            //删除当天已经生成的数据
            CarClueInitMappingExample carClueInitMappingExample = new CarClueInitMappingExample();
            carClueInitMappingExample.createCriteria()
                    .andAppletDateEqualTo(date)
                    .andApiCodeEqualTo(apiCode);
            carClueInitMappingMapper.deleteByExample(carClueInitMappingExample);
        } catch (Exception e) {
            log.error("{}删除历史外采初始数据有误，apiCode:{}，date:{}", TITL, apiCode, date);
        }
    }

    /**
     * 执行批量插入
     */
    private void executeBatchInsert(List<String> valueList) {
        if (CollectionUtils.isEmpty(valueList)) {
            log.warn("{}无有效数据可插入", TITL);
            return;
        }

        String sql = "INSERT INTO marketing.b_car_clue_init_mapping " +
                "(api_code, brand_name, series_name, nation, satisfy_province_name, " +
                "satisfy_city_name, exclude_province_name, exclude_city_name, applet_date, " +
                "create_time, update_time, is_del, daily_limited, demand_id) " +
                "VALUES " + String.join(", ", valueList) + ";";

        log.warn("{}批量插入SQL：{}", TITL, sql);
        carClueRelationalMappingMapper.insertSql(sql);
    }

    /**
     * ============================== 维护外采渠道商映射信息 ==============================
     */
    public void relationalMapping(String apiCode) {
        try {
            // 删除今日生成的外采映射数据
            deleteRelationalData(apiCode);

            // 获取最新日期
            String proviceCleanDate = carClueProvincesInformationMapper.getMaxCleanDate();
            String seriesCleanDate = carClueSeriesInformationMapper.getMaxCleanDate();
            String carClueInitDate = carClueInitMappingMapper.getMaxCleanDate();

            //获取省市集合
            CarClueProvincesInformationExample carClueProvincesInformationExample = new CarClueProvincesInformationExample();
            carClueProvincesInformationExample.createCriteria()
                    .andApiCodeEqualTo(apiCode)
                    .andAppletDateEqualTo(proviceCleanDate)
                    .andIsDelEqualTo(Constants.DATA_VALID);
            List<CarClueProvincesInformation> carClueProvincesInformations = carClueProvincesInformationMapper.selectByExample(carClueProvincesInformationExample);

            //获取品牌车系集合
            CarClueSeriesInformationExample carClueSeriesInformationExample = new CarClueSeriesInformationExample();
            carClueSeriesInformationExample.createCriteria()
                    .andApiCodeEqualTo(apiCode)
                    .andAppletDateEqualTo(seriesCleanDate)
                    .andIsDelEqualTo(Constants.DATA_VALID);
            List<CarClueSeriesInformation> carClueSeriesInformations = carClueSeriesInformationMapper.selectByExample(carClueSeriesInformationExample);

            //获取外采初始信息
            CarClueInitMappingExample carClueInitMappingExample = new CarClueInitMappingExample();
            carClueInitMappingExample.createCriteria()
                    .andAppletDateEqualTo(carClueInitDate)
                    .andApiCodeEqualTo(apiCode)
                    .andIsDelEqualTo(Constants.DATA_VALID);
            List<CarClueInitMapping> carClueInitMappingList = carClueInitMappingMapper.selectByExample(carClueInitMappingExample);


            Map<String, List<CarClueProvincesInformation>> provinceNameMap = carClueProvincesInformations.stream()
                    .filter(item -> item.getProvinceName() != null)
                    .collect(Collectors.groupingBy(CarClueProvincesInformation::getProvinceName));

            Map<String, List<CarClueProvincesInformation>> cityNameMap = carClueProvincesInformations.stream()
                    .filter(item -> item.getCityName() != null)
                    .collect(Collectors.groupingBy(CarClueProvincesInformation::getCityName));

            Map<String, List<CarClueSeriesInformation>> brandNameMap = carClueSeriesInformations.stream()
                    .filter(item -> item.getBrandName() != null)
                    .collect(Collectors.groupingBy(CarClueSeriesInformation::getBrandName));

            Map<String, List<CarClueSeriesInformation>> subBrandNameMap = carClueSeriesInformations.stream()
                    .filter(item -> item.getSubBrandName() != null)
                    .collect(Collectors.groupingBy(CarClueSeriesInformation::getSubBrandName));


            Map<String, List<CarClueSeriesInformation>> seriesNameMap = carClueSeriesInformations.stream()
                    .collect(Collectors.groupingBy(CarClueSeriesInformation::getSeriesName));

            AbstractClueChannelConfig channelConfig = clueChannelConfigService.getChannelConfigImpl(apiCode);
            //匹配初始信息
            List<CarClueRelationalMapping> carClueRelationalMappings = new ArrayList<>();
            for (CarClueInitMapping carClueInitMapping : carClueInitMappingList) {
                CarClueRelationalMapping carClueRelationalMapping = new CarClueRelationalMapping();
                carClueRelationalMapping.setMatchingType(0);
                carClueRelationalMapping.setApiCode(carClueInitMapping.getApiCode());
                carClueRelationalMapping.setBrandName(carClueInitMapping.getBrandName());
                carClueRelationalMapping.setDailyLimited(carClueInitMapping.getDailyLimited());
                carClueRelationalMapping.setMatchDailyLimited(0);
                carClueRelationalMapping.setDemandId(carClueInitMapping.getDemandId());

                // 处理初始外采品牌信息
                StringBuilder stringBuilder = new StringBuilder();
                channelConfig.verifyCarClueInit(stringBuilder, carClueRelationalMapping,
                        carClueInitMapping, provinceNameMap, cityNameMap, brandNameMap, subBrandNameMap);

                // 匹配省市类型
                matchProvincesType(carClueInitMapping, carClueRelationalMapping);

                // 处理车系信息
                String seriesName = carClueInitMapping.getSeriesName();
                if (seriesName == null) {
                    log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.CARCLUE_SERVICEERROR.getCode(),
                            "未找到车系名称! apiCode：" + carClueInitMapping.getApiCode() + "，品牌：" + carClueInitMapping.getBrandName()));
                }
                // 是否为全系
                if (ALL_SERVIES.equals(seriesName)) {
                    carClueRelationalMapping.setSeriesName(seriesName);
                    carClueRelationalMapping.setMatchingCause(stringBuilder.toString());
                    carClueRelationalMappings.add(carClueRelationalMapping);
                    continue;
                }
                // 多车系处理
                Arrays.stream(seriesName.split(","))
                        .forEach(singleSeries ->
                                processSingleSeriesMatch(apiCode, singleSeries,
                                        carClueRelationalMapping, seriesNameMap, carClueRelationalMappings,
                                        stringBuilder));

                if (!CollectionUtils.isEmpty(carClueRelationalMappings)) {
                    CarClueRelationalMapping mapping = carClueRelationalMappings.get(carClueRelationalMappings.size() - 1);

                    // 品牌未匹配到，但车系反找到品牌，则需要增加映射记录
                    if (carClueRelationalMapping.getMatchingType() == 1 && mapping.getMatchingType() == 0) {
                        updateBrandSupplementMapping(apiCode, carClueRelationalMapping.getBrandName(), mapping.getBrandName());
                    }
                }

                if (carClueRelationalMappings.size() >= 500) {
                    carClueRelationalMappingMapper.batchInsert(carClueRelationalMappings);
                    carClueRelationalMappings.clear();
                }
            }
            // 插入剩余的数据
            if (!carClueRelationalMappings.isEmpty()) {
                carClueRelationalMappingMapper.batchInsert(carClueRelationalMappings);
            }
            ;
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(
                    AlarmSendCodeEnum.CARCLUE_SERVICEERROR.getCode(),
                    "维护外采渠道商信息异常"), e);
        }
    }

    /**
     * 删除今日已生成的 外采映射数据
     */
    private void deleteRelationalData(String apiCode) {
        String date = LocalDate.now().toString();
        try {
            CarClueRelationalMappingExample example = new CarClueRelationalMappingExample();
            example.createCriteria()
                    .andAppletDateEqualTo(date)
                    .andApiCodeEqualTo(apiCode);
            carClueRelationalMappingMapper.deleteByExample(example);
        } catch (Exception e) {
            log.error("{}删除历史外采映射数据有误，apiCode:{}，date:{}", TITL, apiCode, date);
        }
    }

    /**
     * 处理单个车系匹配
     */
    private void processSingleSeriesMatch(String apiCode,
                                          String seriesName,
                                          CarClueRelationalMapping carClueRelationalMapping,
                                          Map<String, List<CarClueSeriesInformation>> seriesNameMap,
                                          List<CarClueRelationalMapping> carClueRelationalMappings,
                                          StringBuilder stringBuilder) {

        CarClueRelationalMapping mapping = createMappingCopy(carClueRelationalMapping);
        StringBuilder seriesErrorMsg = new StringBuilder();

        Optional<CarClueSeriesInformation> seriesInfo = matchSeries(
                apiCode, seriesName, seriesNameMap, seriesErrorMsg);

        if (!seriesInfo.isPresent()) {
            // 匹配失败的情况
            mapping.setSeriesName(seriesName);
            mapping.setMatchingType(1);
            mapping.setMatchingCause(stringBuilder.toString() + seriesErrorMsg);
            carClueRelationalMappings.add(mapping);
            return;
        }

        // 匹配成功的情况
        CarClueSeriesInformation matchedSeries = seriesInfo.get();
        int matchingType = mapping.getMatchingType();

        if (matchingType == 1) {
            // 品牌赋值失败但车系匹配成功，覆盖品牌信息
            mapping.setBrandId(matchedSeries.getBrandId());
            mapping.setBrandName(matchedSeries.getBrandName());
            mapping.setSeriesId(matchedSeries.getSeriesId());
            mapping.setSeriesName(matchedSeries.getSeriesName());
            mapping.setMatchingType(0);
        } else if (!mapping.getBrandName().equals(matchedSeries.getBrandName())) {
            // 品牌不一致的情况
            seriesErrorMsg
                    .append("【车系：").append(seriesName).append("】")
                    .append("原品牌和匹配品牌不一致，原品牌：").append(mapping.getBrandName())
                    .append("，匹配品牌：").append(matchedSeries.getBrandName());

            mapping.setSeriesName(seriesName);
            mapping.setMatchingType(1);

        } else {
            // 完全匹配成功的情况
            mapping.setSeriesId(matchedSeries.getSeriesId());
            mapping.setSeriesName(matchedSeries.getSeriesName());
            mapping.setMatchingType(0);
        }

        // 合并错误信息
        mapping.setMatchingCause(stringBuilder.toString() + seriesErrorMsg);
        carClueRelationalMappings.add(mapping);

        // 记录警告信息
        if (seriesErrorMsg.length() > 0) {
            log.warn(AlertLog.buildWarnMessage(
                    AlarmSendCodeEnum.CARCLUE_SERVICEERROR.getCode(),
                    seriesErrorMsg.toString()));
        }
    }

    /**
     * 创建映射对象副本
     */
    private CarClueRelationalMapping createMappingCopy(CarClueRelationalMapping source) {
        CarClueRelationalMapping mapping = new CarClueRelationalMapping();
        BeanUtils.copyProperties(source, mapping);
        return mapping;
    }

    /**
     * 车系匹配核心逻辑
     */
    private Optional<CarClueSeriesInformation> matchSeries(String apiCode,
                                                           String seriesName,
                                                           Map<String, List<CarClueSeriesInformation>> seriesNameMap,
                                                           StringBuilder errorMsg) {
        // 1. 尝试直接匹配
        List<CarClueSeriesInformation> seriesList = seriesNameMap.get(seriesName);
        if (!CollectionUtils.isEmpty(seriesList)) {
            return Optional.of(seriesList.get(0));
        }

        // 2. 尝试补充匹配
        List<CarClueSupplement> supplements = queryClueSupplement(apiCode, seriesName, CarInformationTypeEnum.SERIES.getValue());
        if (CollectionUtils.isEmpty(supplements)) {
            errorMsg.append("未匹配到该车系：").append(seriesName).append(" | ");
            return Optional.empty();
        }
        if (supplements.size() > 1) {
            errorMsg.append("补充表匹配出多个车系：").append(seriesName).append(" | ");
            log.warn(AlertLog.buildWarnMessage(
                    AlarmSendCodeEnum.CARCLUE_SERVICEERROR.getCode(),
                    "补充表匹配出多个车系：" + apiCode + seriesName));
            return Optional.empty();
        }
        // 3. 处理补充匹配结果
        String alternativeName = supplements.get(0).getNewName();
        List<CarClueSeriesInformation> alternativeSeries = seriesNameMap.get(alternativeName);

        if (CollectionUtils.isEmpty(alternativeSeries)) {
            errorMsg.append("未匹配到该车系(补充)：").append(alternativeName).append(" | ");
            log.warn(AlertLog.buildWarnMessage(
                    AlarmSendCodeEnum.CARCLUE_SERVICEERROR.getCode(),
                    "未匹配到该车系(补充)! apiCode：" + apiCode + "，车系：" + alternativeName));
            return Optional.empty();
        }

        // 4. 处理品牌映射更新
        CarClueSeriesInformation matchedSeries = alternativeSeries.get(0);
        return Optional.of(matchedSeries);
    }

    /**
     * 更新品牌补充映射表
     */
    private void updateBrandSupplementMapping(String apiCode, String oldName, String newName) {

        CarClueSupplement supplement = new CarClueSupplement();
        supplement.setApiCode(apiCode);
        supplement.setOldName(oldName);
        supplement.setNewName(newName);
        supplement.setType(CarInformationTypeEnum.BRAND.getValue());
        supplement.setCreateTime(new Date());
        supplement.setUpdateTime(new Date());

        try {
            carClueSupplementMapper.insertSelective(supplement);
        } catch (Exception e) {
            log.error("更新品牌补充映射表失败", e);
        }
    }

    /**
     * 匹配省市类型
     *
     * @param carClueInitMapping
     * @param carClueRelationalMapping
     */
    private void matchProvincesType(CarClueInitMapping carClueInitMapping, CarClueRelationalMapping carClueRelationalMapping) {
        if (StringUtils.isNotEmpty(carClueInitMapping.getNation())
                && StringUtils.isEmpty(carClueInitMapping.getExcludeProvinceName())
                && StringUtils.isEmpty(carClueInitMapping.getExcludeCityName()))
        {
            carClueRelationalMapping.setProvinceType(ProvinceTypeEnum.NATIONWIDE.getValue());
        }
        else if (StringUtils.isNotEmpty(carClueInitMapping.getSatisfyProvinceName()) ||
                StringUtils.isNotEmpty(carClueInitMapping.getSatisfyCityName()))
        {
            carClueRelationalMapping.setProvinceType(ProvinceTypeEnum.FIXED.getValue());
        } else
        {
            carClueRelationalMapping.setProvinceType(ProvinceTypeEnum.EXCLUDE.getValue());
        }
    }

    private List<CarClueSupplement> queryClueSupplement(String apiCode, String oldName, Integer type) {
        // 未直接匹配时，查询补充数据表
        CarClueSupplementExample supplementExample = new CarClueSupplementExample();
        supplementExample.createCriteria()
                .andApiCodeEqualTo(apiCode)
                .andOldNameEqualTo(oldName)
                .andTypeEqualTo(type)
                .andIsDelEqualTo(1);

        return carClueSupplementMapper.selectByExample(supplementExample);
    }

}
