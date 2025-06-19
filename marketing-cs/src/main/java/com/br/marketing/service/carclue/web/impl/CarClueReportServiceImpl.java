package com.br.marketing.service.carclue.web.impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.CarClueReportDTO;
import com.br.marketing.dto.DataExportTaskDTO;
import com.br.marketing.dto.ExecuteCarClueDTO;
import com.br.marketing.entity.*;
import com.br.marketing.entity.auth.MarketingUserDetail;
import com.br.marketing.enums.DataSourceEnum;
import com.br.marketing.mapper.CarClueExecuteRecordingMapper;
import com.br.marketing.mapper.CarClueInfoMapper;
import com.br.marketing.mapper.CarClueManageConfigMapper;
import com.br.marketing.mapper.DataExportTaskMapper;
import com.br.marketing.service.Impl.EntityOptServiceImpl;
import com.br.marketing.service.carclue.clueenums.CarClueCompleteStatusEnum;
import com.br.marketing.service.carclue.clueenums.CarClueDataStatusEnum;
import com.br.marketing.service.carclue.clueenums.ExecuteClueStatusEnum;
import com.br.marketing.service.carclue.web.CarClueReportService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.vo.CarClueInfoVo;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 车线索列表
 *
 * @author guangxiu.li
 * @date 2025/1/14
 * @description
 */
@Service
@Slf4j
public class CarClueReportServiceImpl implements CarClueReportService {

    @Resource
    CarClueInfoMapper carClueInfoMapper;
    @Resource
    CarClueExecuteRecordingMapper carClueExecuteRecordingMapper;
    @Resource
    CarClueManageConfigMapper carClueManageConfigMapper;
    @Resource
    EntityOptServiceImpl entityOptService;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    private DataExportTaskMapper dataExportTaskMapper;

    @Override
    public PageResultReturn getReportList(CarClueReportDTO request) {
        Integer current = request.getCurrent();
        Integer size = request.getSize();

        Map params = new HashMap();
        params.put("createTimeStart", request.getCreateTimeStart());
        params.put("createTimeEnd", request.getCreateTimeEnd());
        params.put("resourceType", request.getResourceType());
        params.put("intention", request.getIntention());
        params.put("clueDataStatusList", request.getClueDataStatus());
        params.put("clueCompleteStatusList", request.getClueCompleteStatus());
        params.put("updateTimeStart", request.getUpdateTimeStart());
        params.put("updateTimeEnd", request.getUpdateTimeEnd());
        if (ObjectUtil.isNotEmpty(request.getCluePushChannel())) {
            List<String> cluePushChannel = getValueByKey(request.getCluePushChannel());
            if (cluePushChannel.contains("fail")) {
                params.put("cluePushChannel", null);
                params.put("queryNullOrEmpty", true);
            } else {
                params.put("cluePushChannel", cluePushChannel);
                params.put("queryNullOrEmpty", false);
            }
        }
        params.put("cluePushStatus", request.getCluePushStatus());
        params.put("pushTimeStart", request.getPushTimeStart());
        params.put("pushTimeEnd", request.getPushTimeEnd());
        params.put("status", request.getClueCallbackFinalState());
        params.put("callBackTimeStart", request.getCallBackTimeStart());
        params.put("callBackTimeEnd", request.getCallBackTimeEnd());
        params.put("search", request.getSearch());

        List<String> allowedFields = Arrays.asList("create_time", "update_time", "push_time", "call_back_time",
                "clean_time");
        String orderByField = camelToSnake(request.getOrderByField());
        if (!allowedFields.contains(orderByField)) {
            orderByField = "create_time";
        }
        String orderByType = "ASC".equalsIgnoreCase(request.getOrderByType()) ? "ASC" : "DESC";

        params.put("orderByField", orderByField);
        params.put("orderByType", orderByType);

        PageHelper.startPage(current, size);
        List<CarClueInfoVo> list = carClueInfoMapper.selectList(params);
        list.forEach((CarClueInfoVo carClueInfoVo) -> {
            String encryptCell = encryptCell(carClueInfoVo.getCell());
            carClueInfoVo.setCell(encryptCell);
            String cluePushChannel = getChannelByApiCode(carClueInfoVo.getCluePushChannel());
            carClueInfoVo.setCluePushChannel(cluePushChannel);
        });

        return PageResultReturn.setPageResult(list, current, size);
    }

    public String encryptCell(String cell) {
        try {
            cell = BrCipherMaker.getInstance().decode(cell);
            if (cell == null || cell.isEmpty()) {
                return "";
            }
            if (cell.length() < 7) {
                return cell;
            }
            return cell.substring(0, 3) + "****" + cell.substring(7);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(
                    AlarmSendCodeEnum.CARCLUE_SERVICEERROR.getCode(),
                    "线索数据收集号解密失败！cell: " + cell), e);
        }
        return "";
    }



    @Override
    public ApiResult<Boolean> editCarClues(List<CarClueInfo> voList) {
        if (voList == null || voList.isEmpty()) {
            return new ApiResult<Boolean>().fail(false, "更新列表不能为空");
        }
        List<Integer> carClueDataStatusList = Arrays.asList(CarClueDataStatusEnum.ABNORMAL_CLUE.getValue(),
                CarClueDataStatusEnum.LACK_CLUE.getValue());
        List<Integer> carClueCompleteStatusList =
                Arrays.asList(CarClueCompleteStatusEnum.AETIFICAL_ABNORMAL_COMPLETE.getValue(), CarClueCompleteStatusEnum.AETIFICAL_LACK_COMPLETE.getValue());
        for (CarClueInfo vo : voList) {
            CarClueInfo clueInfo = new CarClueInfo();
            try {
                CarClueInfo carClueInfo = carClueInfoMapper.selectByPrimaryKey(vo.getId());
                clueInfo.setId(vo.getId());
                clueInfo.setBrand(vo.getBrand());
                clueInfo.setSeries(vo.getSeries());
                clueInfo.setCity(vo.getCity());
                clueInfo.setClueDataStatus(CarClueDataStatusEnum.READY.getValue());
                Integer clueCompleteStatus = carClueInfo.getClueCompleteStatus();
                if (carClueDataStatusList.contains(vo.getClueDataStatus()) && !carClueCompleteStatusList.contains(clueCompleteStatus)) {
                    if (CarClueDataStatusEnum.ABNORMAL_CLUE.getValue().equals(vo.getClueDataStatus())) {
                        clueInfo.setClueCompleteStatus(CarClueCompleteStatusEnum.AETIFICAL_ABNORMAL_COMPLETE.getValue());
                    } else {
                        clueInfo.setClueCompleteStatus(CarClueCompleteStatusEnum.AETIFICAL_LACK_COMPLETE.getValue());
                    }
                }
                clueInfo.setUpdateTime(new Date());
                entityOptService.writeOptLog(vo.getId(), clueInfo, carClueInfo);
                carClueInfoMapper.updateByPrimaryKeySelective(clueInfo);
            } catch (Exception e) {
                log.warn(AlertLog.buildWarnMessage(
                        AlarmSendCodeEnum.CARCLUE_SERVICEERROR.getCode(),
                        "编辑车线索信息失败！voId: " + vo.getId()), e);
            }
        }
        return new ApiResult<Boolean>().success(true);
    }

    @Override
    public ApiResult<Boolean> executeClueData(ExecuteCarClueDTO dto, MarketingUserDetail user) {

        if(dto == null){
            return new ApiResult<Boolean>().fail("入参为空！");
        }

        int executeType = dto.getExecuteType();
        CarClueManageConfigExample example = new CarClueManageConfigExample();
        if(executeType == 0){
            example.createCriteria().andIsDelEqualTo(Constants.DATA_VALID).andCleanTypeEqualTo(1);
        }else {
            example.createCriteria().andIsDelEqualTo(Constants.DATA_VALID).andPullTypeEqualTo(1);
        }
        int i = carClueManageConfigMapper.countByExample(example);
        if(i > 0){
            return new ApiResult<Boolean>().fail("渠道商配置为自动执行，不能增加手动执行记录！");
        }

        Long userId = Long.valueOf(user.getId());
        String userName = user.getUserName();
        CarClueExecuteRecording carClueExecuteRecording = new CarClueExecuteRecording();
        carClueExecuteRecording.setExecuteType(dto.getExecuteType());
        carClueExecuteRecording.setExecuteStatus(ExecuteClueStatusEnum.AWAIT_EXECUTE.getValue());
        carClueExecuteRecording.setOptUserId(userId);
        carClueExecuteRecording.setOptUserName(userName);
        carClueExecuteRecording.setCreateTime(new Date());
        carClueExecuteRecording.setUpdateTime(new Date());
        carClueExecuteRecording.setIsDel(Constants.DATA_VALID);

        List<Long> ids = dto.getClueIds();
        if(!CollectionUtils.isEmpty(ids)){
            carClueExecuteRecording.setClueIds(dto.getClueIds().toString());
        }else {
            carClueExecuteRecording.setClueRange(JSONObject.toJSONString(dto.getClueRange()));
        }
        carClueExecuteRecordingMapper.insertSelective(carClueExecuteRecording);
        //增加日志
        Long id = carClueExecuteRecording.getId();
        entityOptService.writeOptLog(id, carClueExecuteRecording, null);
        return new ApiResult<Boolean>().success(true);
    }

    public List<String>  getValueByKey(String key) {
        try {
            Map<String, Object> carClueApiCodeMapping = marketingCommonConfig.getCarClueApiCodeMapping();
            Map<String, List> channel = (Map<String, List>) carClueApiCodeMapping.get("channel");
            if (ObjectUtil.isEmpty(channel)) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.CARCLUE_SERVICEERROR.getCode(),
                        "渠道不存在！"));
            }
            List<String> carClueApiCodes = channel.get(key);
            return ObjectUtil.isNotEmpty(carClueApiCodes) ? carClueApiCodes : new ArrayList<>();
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.CARCLUE_SERVICEERROR.getCode(),
                    "获取推送渠道映射失败！错误信息：" + e.getMessage()), e);
            return new ArrayList<>();
        }
    }

    public String getChannelByApiCode(String apiCode) {
        Map<String, Object> configMap = marketingCommonConfig.getCarClueApiCodeMapping();

        if (configMap == null || !configMap.containsKey("channel")) {
            return null;
        }

        Map<String, List<String>> channelMap = (Map<String, List<String>>) configMap.get("channel");

        for (Map.Entry<String, List<String>> entry : channelMap.entrySet()) {
            if (entry.getValue().contains(apiCode)) {
                return entry.getKey();
            }
        }

        return null;
    }

    private String camelToSnake(String str) {
        if (str == null) {
            return null;
        }
        return str.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

    @Override
    public ApiResult<Boolean> createTask(DataExportTaskDTO dto, MarketingUserDetail user) {
        log.info("开始创建数据导出任务，参数：{}, 用户：{}", dto, user.getUserName());

        // 1. 参数验证
        if (dto == null) {
            return new ApiResult<Boolean>().fail(false, "DTO参数不能为空");
        }
        Integer dataSourceCode = dto.getDataSource();
        if (ObjectUtil.isEmpty(dataSourceCode)) {
            return new ApiResult<Boolean>().fail(false, "数据源不能为空");
        }
        // 2. 验证数据源是否有效
        DataSourceEnum dataSourceEnum = getDataSourceEnum(dto.getDataSource());
        if (dataSourceEnum == null) {
            return new ApiResult<Boolean>().fail(false, "无效的数据源code");
        }
        // 3. 处理任务名称 - 如果为空则生成默认名称
        String taskName = dto.getTaskName();
        taskName = generateDefaultTaskName(taskName, dataSourceCode);
        // 4. 检查任务名称是否重复
        if (isTaskNameExists(taskName)) {
            return new ApiResult<Boolean>().fail(false, "任务名称已存在");
        }

        dto.setTaskName(taskName);
        // 5. DTO转换为Entity
        DataExportTask task = convertToEntity(dto, user, dataSourceEnum);

        // 6. 保存到数据库
        dataExportTaskMapper.insertSelective(task);
        return new ApiResult<Boolean>().success(true);
    }

    /**
     * 根据前端传入的code获取数据源枚举
     * @param dataSourceCode 前端传入的数据源code（数字字符串）
     * @return 数据源枚举，如果不存在返回null
     */
    private DataSourceEnum getDataSourceEnum(Integer dataSourceCode) {
        try {
            return DataSourceEnum.getByCode(dataSourceCode);
        } catch (NumberFormatException e) {
            log.error("数据源code格式错误：{}", dataSourceCode);
            return null;
        }
    }

    /**
     * 生成默认任务名称
     * @param taskName 原始任务名称
     * @param dataSourceCode 数据源代码
     * @return 生成的任务名称
     */
    private String generateDefaultTaskName(String taskName, Integer dataSourceCode) {
        try {
            if (dataSourceCode == 1) {
                // 其他数据源，如果taskName为空，返回默认名称
                if (StringUtils.isBlank(taskName)) {
                    // dataSource=1: 返回 {时间}_序列号 的任务名称
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                    String timeStr = sdf.format(new Date());
                    int sequence = getNextSequenceForTimeBasedName(timeStr);
                    return timeStr + "_" + String.format("%02d", sequence);
                }
                return taskName;

            } else {
                // 其他数据源，如果taskName为空，返回默认名称
                if (StringUtils.isBlank(taskName)) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                    return "导出任务_" + sdf.format(new Date());
                }
                return taskName;
            }
        } catch (Exception ex) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.BAOXIAN_SERVICEERROR.getCode(),
                    "创建默认任务名错误！错误信息：" + ex.getMessage()), ex);
            return "导出任务_" + System.currentTimeMillis();
        }
    }

    /**
     * 检查任务名称是否已存在
     */
    private boolean isTaskNameExists(String taskName) {
        try {
            DataExportTaskExample example = new DataExportTaskExample();
            example.createCriteria()
                    .andTaskNameEqualTo(taskName)
                    .andStatusEqualTo((byte) 1);

            List<DataExportTask> existingTasks = dataExportTaskMapper.selectByExample(example);
            return !CollectionUtils.isEmpty(existingTasks);
        } catch (Exception e) {
            log.error("检查任务名称是否存在时异常", e);
            return true;
        }
    }

    /**
     * 获取任务名称的下一个序列号
     * @return 下一个序列号
     */
    private int getNextSequenceForTimeBasedName(String timeStr) {
        try {
            DataExportTaskExample example = new DataExportTaskExample();
            example.createCriteria()
                    .andTaskNameLike(timeStr + "_%")
                    .andStatusEqualTo((byte) 1);

            List<DataExportTask> existingTasks = dataExportTaskMapper.selectByExample(example);

            if (CollectionUtils.isEmpty(existingTasks)) {
                return 1;
            }

            // 找出最大的序列号
            int maxSequence = 0;
            for (DataExportTask task : existingTasks) {
                String name = task.getTaskName();
                if (name != null && name.startsWith(timeStr + "_")) {
                    String sequencePart = name.substring((timeStr + "_").length());
                    try {
                        int sequence = Integer.parseInt(sequencePart);
                        maxSequence = Math.max(maxSequence, sequence);
                    } catch (NumberFormatException e) {
                        // 忽略无法解析的序列号
                    }
                }
            }

            return maxSequence + 1;

        } catch (Exception ex) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.BAOXIAN_SERVICEERROR.getCode(),
                    "获取时间基础任务名称序列号时异常！错误信息：" + ex.getMessage()), ex);
            return 1;
        }
    }

    /**
     * DTO转换为Entity
     */
    private DataExportTask convertToEntity(DataExportTaskDTO dto, MarketingUserDetail user, DataSourceEnum dataSourceEnum) {
        DataExportTask task = new DataExportTask();

        // 基本信息
        task.setTaskName(dto.getTaskName());
        task.setDataSource(dataSourceEnum.getSourceCode());
        task.setExportHeaders(dto.getExportHeaders());
        task.setEstimatedRows(dto.getEstimatedRows());

        // 文件名模板：如果为空则生成默认模板
        String fileNameTemplate = dto.getTaskName() + ".txt";
        task.setFileNameTemplate(fileNameTemplate);

        // JSON字段序列化
        if (!ObjectUtil.isEmpty(dto.getFieldMapping())) {
            JSONObject queryCondition = JSON.parseObject(dto.getFieldMapping());
            task.setFieldMapping(queryCondition.toJSONString());
        }

        if (!ObjectUtil.isEmpty(dto.getQueryCondition())) {
            JSONObject queryCondition = JSON.parseObject(dto.getQueryCondition());
            task.setQueryCondition(queryCondition.toJSONString());
        }

        // 默认状态：启用
        task.setStatus((byte) 1);

        // 创建人信息
        task.setCreateBy(user.getUserName());
        task.setUpdateBy(user.getUserName());

        // 时间信息
        Date now = new Date();
        task.setCreateTime(now);
        task.setUpdateTime(now);
        task.setTaskRule("{\"extraScene\":\"文件提取_车线索数据提取\"}");

        return task;
    }

}
