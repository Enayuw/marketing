package com.br.marketing.service.carclue.web.impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.FastDfsClient;
import com.br.marketing.client.RedisChgService;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.constants.rediskey.RedisKeyConstant;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.CarClueChannelConfigDTO;
import com.br.marketing.dto.CarClueChannelDTO;
import com.br.marketing.entity.CarClueManageConfig;
import com.br.marketing.entity.CarClueManageConfigExample;
import com.br.marketing.entity.ClueFileRecording;
import com.br.marketing.entity.ClueFileRecordingExample;
import com.br.marketing.entity.auth.MarketingUserDetail;
import com.br.marketing.mapper.CarClueManageConfigMapper;
import com.br.marketing.mapper.CarClueRelationalMappingMapper;
import com.br.marketing.mapper.ClueFileRecordingMapper;
import com.br.marketing.service.Impl.EntityOptServiceImpl;
import com.br.marketing.service.SyncConfigService;
import com.br.marketing.service.carclue.clueenums.ClueFileRecordingStatusEnum;
import com.br.marketing.service.carclue.web.CarClueChannelService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.vo.CarClueChannelConfigVO;
import com.br.marketing.vo.CarClueChannelVo;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
    private FastDfsClient fastDfsClient;
    @Resource
    EntityOptServiceImpl entityOptService;
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

        String maxDate = carClueRelationalMappingMapper.getMaxCleanDateByApiCode(cluePushChannel);
        PageHelper.startPage(current, size);
        List<CarClueChannelVo> list = carClueRelationalMappingMapper.selectList(search, cluePushChannel, maxDate);
        return PageResultReturn.setPageResult(list, current, size);
    }

    @Override
    public ApiResult<Boolean> checkCleanFile() {
        ClueFileRecordingExample clueFileRecordingExample = new ClueFileRecordingExample();
        clueFileRecordingExample.createCriteria()
                .andFileCleanStatusEqualTo(ClueFileRecordingStatusEnum.AWAIT_CLEAN.getValue())
                .andIsDelEqualTo(Constants.DATA_VALID);
        int i = clueFileRecordingMapper.countByExample(clueFileRecordingExample);
        if (i > 0) {
            return new ApiResult<Boolean>().fail("存在待清洗的文件！");
        }
        return new ApiResult<Boolean>().success(true);
    }

    @Override
    public ApiResult<CarClueChannelConfigVO> getChannelConfig() {

        CarClueManageConfigExample carClueManageConfigExample = new CarClueManageConfigExample();
        carClueManageConfigExample.createCriteria().andIsDelEqualTo(1);
        List<CarClueManageConfig> carClueManageConfigs = carClueManageConfigMapper.selectByExample(carClueManageConfigExample);

        if (CollectionUtils.isEmpty(carClueManageConfigs)) {
            return new ApiResult<CarClueChannelConfigVO>().fail("渠道商配置为空！");
        }
        CarClueManageConfig carClueManageConfig = carClueManageConfigs.get(0);

        CarClueChannelConfigVO carClueChannelConfigVO = new CarClueChannelConfigVO();
        carClueChannelConfigVO.setId(carClueManageConfig.getId());
        carClueChannelConfigVO.setPullDate(carClueManageConfig.getPullDate());
        carClueChannelConfigVO.setIntentionConfig(JSONObject.parseObject(carClueManageConfig.getIntentionConfig()));
        carClueChannelConfigVO.setCleanType(carClueManageConfig.getCleanType());
        carClueChannelConfigVO.setPullType(carClueManageConfig.getPullType());
        carClueChannelConfigVO.setOptUserId(carClueManageConfig.getOptUserId());
        carClueChannelConfigVO.setOptUserName(carClueManageConfig.getOptUserName());
        carClueChannelConfigVO.setCreateTime(carClueManageConfig.getCreateTime());
        carClueChannelConfigVO.setUpdateTime(carClueManageConfig.getUpdateTime());
        carClueChannelConfigVO.setIsDel(carClueManageConfig.getIsDel());
        return new ApiResult<CarClueChannelConfigVO>().success(carClueChannelConfigVO);
    }

    @Override
    public ApiResult<Boolean> updateChannelConfig(CarClueChannelConfigDTO dto,MarketingUserDetail user) {
        if (dto == null) {
            return new ApiResult<Boolean>().fail("入参为空！");
        }
        Long userId = Long.valueOf(user.getId());
        String userName = user.getUserName();

        CarClueManageConfig carClueManageConfig = new CarClueManageConfig();
        carClueManageConfig.setPullDate(dto.getPullDate());
        carClueManageConfig.setIntentionConfig(JSONObject.toJSONString(dto.getIntentionConfig()));
        carClueManageConfig.setCleanType(dto.getCleanType());
        carClueManageConfig.setPullType(dto.getPullType());
        carClueManageConfig.setOptUserId(userId);
        carClueManageConfig.setOptUserName(userName);
        carClueManageConfig.setUpdateTime(new Date());
        carClueManageConfig.setIsDel(Constants.DATA_VALID);
        if (dto.getId() != null) {
            CarClueManageConfig carClueOld = carClueManageConfigMapper.selectByPrimaryKey(dto.getId());
            carClueManageConfig.setId(dto.getId());
            carClueManageConfigMapper.updateByPrimaryKeySelective(carClueManageConfig);
            //增加日志
            entityOptService.writeOptLog(dto.getId(), carClueManageConfig, carClueOld);
        } else {
            carClueManageConfig.setCreateTime(new Date());
            carClueManageConfigMapper.insertSelective(carClueManageConfig);
            //增加日志
            entityOptService.writeOptLog(carClueManageConfig.getId(), carClueManageConfig, null);
        }

        return new ApiResult<Boolean>().success(true);
    }

    @Override
    public ApiResult<Boolean> updateInitMapping(List<String> scope, MultipartFile multipartFile) {
        // 1. 参数校验
        if (CollectionUtils.isEmpty(scope)) {
            return new ApiResult<Boolean>().fail("scope参数不能为空！");
        }
        if (multipartFile.isEmpty()) {
            return new ApiResult<Boolean>().fail("文件不能为空！");
        }
        String fileName = multipartFile.getOriginalFilename();
        if (!fileName.endsWith(".xls") && !fileName.endsWith(".xlsx")) {
            return new ApiResult<Boolean>().fail("仅支持Excel文件(.xls, .xlsx)！");
        }

        // 2. 准备锁
        final String syncDate = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        final String key = RedisKeyConstant.updateInitMapping + ":" + syncDate;
        final String lockValue = UUID.randomUUID().toString();

        try {
            // 3. 获取分布式锁
            if (!redisChgService.lock(key, lockValue, 3000L)) {
                return new ApiResult<Boolean>().fail("加锁失败！");
            }

            // 4. 保存文件至fastDfs
            String url;
            try {
                url = fastDfsClient.uploadFile(multipartFile);
            } catch (IOException e) {
                redisChgService.unlock(key, lockValue);
                return new ApiResult<Boolean>().fail("保存文件至fastDfs失败！" + e);
            }

            // 5. 记录文件信息
            ClueFileRecording clueFileRecording = new ClueFileRecording();
            clueFileRecording.setUpdateScope(String.join(",", scope));
            clueFileRecording.setFileName(fileName);
            clueFileRecording.setFileAdress(url);
            clueFileRecording.setFileCleanStatus(ClueFileRecordingStatusEnum.AWAIT_CLEAN.getValue());
            clueFileRecording.setAppletDate(LocalDate.now().toString());
            clueFileRecording.setCreateTime(new Date());
            clueFileRecording.setUpdateTime(new Date());
            clueFileRecording.setIsDel(Constants.DATA_VALID);
            clueFileRecordingMapper.insertSelective(clueFileRecording);
            //增加日志
            Long id = clueFileRecording.getId();
            entityOptService.writeOptLog(id, clueFileRecording, null);
            return new ApiResult<Boolean>().success(true);
        } catch (Exception e) {
            return new ApiResult<Boolean>().fail("更新初始外采信息异常：" + e.getMessage());
        } finally {
            // 6. 确保锁被释放
            redisChgService.unlock(key, lockValue);
        }
    }

    public List<String> getValueByKey(String key) {
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
