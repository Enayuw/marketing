package com.br.marketing.service.carclue.web.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import cn.hutool.core.util.ObjectUtil;
import com.br.common.log.AlertLog;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.CarClueChannelConfigDTO;
import com.br.marketing.dto.CarClueChannelDTO;
import com.br.marketing.entity.*;
import com.br.marketing.mapper.CarClueManageConfigMapper;
import com.br.marketing.mapper.CarClueRelationalMappingMapper;
import com.br.marketing.mapper.ClueFileRecordingMapper;
import com.br.marketing.service.SyncConfigService;
import com.br.marketing.service.carclue.clueenums.ClueFileRecordingStatusEnum;
import com.br.marketing.service.carclue.web.CarClueChannelService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.vo.CarClueChannelVo;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

/**
 * @ClassName CarClueChannelServiceImpl
 * @Description 车线索外采渠道管理
 * @Author kongbx
 * @Date 2025/5/6 11:00
 */
@Service
@Slf4j
public class CarClueChannelServiceImpl implements CarClueChannelService {

    @Resource
    private ClueFileRecordingMapper clueFileRecordingMapper;
    @Resource
    private CarClueManageConfigMapper carClueManageConfigMapper;
    @Resource
    CarClueRelationalMappingMapper carClueRelationalMappingMapper;
    @Resource
    RedisChgService redisChgService;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Autowired
    SyncConfigService syncConfigService;

    @Override
    public PageResultReturn getCarClueChannelList(CarClueChannelDTO request) {
        Integer current = request.getCurrent();
        Integer size = request.getSize();
        String search = request.getSearch();
        String cluePushChannel = "";

        if (ObjectUtil.isNotEmpty(request.getCluePushChannel())) {
            List<String> cluePushChannels = getValueByKey(request.getCluePushChannel());
            if (cluePushChannels.contains("fail")) {
                return PageResultReturn.setPageResult(new ArrayList<>(), current, size);
            }
            cluePushChannel = cluePushChannels.get(0);
        }

        PageHelper.startPage(current, size);
        List<CarClueChannelVo> list = carClueRelationalMappingMapper.selectList(search,cluePushChannel);
        return PageResultReturn.setPageResult(list, current, size);
    }

    @Override
    public ApiResult<Boolean> checkCleanFile() {
        ClueFileRecordingExample clueFileRecordingExample = new ClueFileRecordingExample();
        clueFileRecordingExample.createCriteria()
                .andFileCleanStatusEqualTo(ClueFileRecordingStatusEnum.AWAIT_CLEAN.getValue())
                .andIsDelEqualTo(Constants.DATA_VALID);
        int i = clueFileRecordingMapper.countByExample(clueFileRecordingExample);
        if(i > 0){
            return new ApiResult<Boolean>().fail("存在待清洗的文件！");
        }
        return new ApiResult<Boolean>().success(true);
    }

    @Override
    public List<CarClueManageConfig> getChannelConfig() {
        CarClueManageConfigExample carClueManageConfigExample = new CarClueManageConfigExample();
        carClueManageConfigExample.createCriteria().andIsDelEqualTo(1);
        return carClueManageConfigMapper.selectByExample(carClueManageConfigExample);
    }

    @Override
    public ApiResult<Boolean> updateChannelConfig(CarClueChannelConfigDTO dto) {
        if (dto == null) {
            return new ApiResult<Boolean>().fail("入参为空！");
        }
        CarClueManageConfig carClueManageConfig = new CarClueManageConfig();
        carClueManageConfig.setPullDate(dto.getPullDate());
        carClueManageConfig.setIntentionConfig(dto.getIntentionConfig());
        carClueManageConfig.setCleanType(dto.getCleanType());
        carClueManageConfig.setPullType(dto.getPullType());
        carClueManageConfig.setOptUserId(dto.getOptUserId());
        carClueManageConfig.setOptUserName(dto.getOptUserName());
        carClueManageConfig.setUpdateTime(new Date());
        carClueManageConfig.setIsDel(Constants.DATA_VALID);

        if (dto.getId() != null) {
            carClueManageConfig.setId(dto.getId());
            carClueManageConfigMapper.updateByPrimaryKeySelective(carClueManageConfig);
        } else {
            carClueManageConfig.setCreateTime(new Date());
            carClueManageConfigMapper.insertSelective(carClueManageConfig);
        }
        return new ApiResult<Boolean>().success(true);
    }

    @Override
    public ApiResult<Boolean> updateInitMapping(List<String> scope, MultipartFile multipartFile) {
        // 1. 参数校验
        if(CollectionUtils.isEmpty(scope)){
            return new ApiResult<Boolean>().fail("外采更新范围为空！");
        }
        if (multipartFile.isEmpty()) {
            return new ApiResult<Boolean>().fail("文件为空！");
        }
        String fileName = multipartFile.getOriginalFilename();
        if (!fileName.endsWith(".xls") && !fileName.endsWith(".xlsx")) {
            return new ApiResult<Boolean>().fail("仅支持Excel文件(.xls, .xlsx)！");
        }

        // 2. 准备路径和锁
        final String syncDate = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        final String descPath = syncConfigService.getPath() + "channel/" + syncDate + "/";
        final String filePath = descPath + fileName;
        final String key = RedisKeyConstant.updateInitMapping + ":" + syncDate;
        final String lockValue = UUID.randomUUID().toString();

        try {
            // 3. 获取分布式锁
            if (!redisChgService.lock(key, lockValue, 3000L)) {
                return new ApiResult<Boolean>().fail("加锁失败！");
            }

            // 4. 创建目录并保存文件
            File parentDir = new File(descPath);
            if (!parentDir.exists() && !parentDir.mkdirs()) {
                return new ApiResult<Boolean>().fail("无法创建目录");
            }

            try (InputStream in = multipartFile.getInputStream();
                 FileOutputStream out = new FileOutputStream(filePath)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }

            // 5. 记录文件信息
            ClueFileRecording clueFileRecording = new ClueFileRecording();
            clueFileRecording.setUpdateScope(String.join(",", scope));
            clueFileRecording.setFileName(fileName);
            clueFileRecording.setFileAdress(descPath);
            clueFileRecording.setFileCleanStatus(ClueFileRecordingStatusEnum.AWAIT_CLEAN.getValue());
            clueFileRecording.setAppletDate(LocalDate.now().toString());
            clueFileRecording.setCreateTime(new Date());
            clueFileRecording.setUpdateTime(new Date());
            clueFileRecording.setIsDel(Constants.DATA_VALID);
            clueFileRecordingMapper.insertSelective(clueFileRecording);
            return new ApiResult<Boolean>().success(true);
        } catch (Exception e) {
            return new ApiResult<Boolean>().fail("更新初始外采信息异常："+e.getMessage());
        } finally {
            // 6. 确保锁被释放
            redisChgService.unlock(key, lockValue);
        }
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

}
