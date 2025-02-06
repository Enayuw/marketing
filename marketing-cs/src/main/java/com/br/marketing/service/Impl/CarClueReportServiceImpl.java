package com.br.marketing.service.Impl;

import cn.hutool.core.util.ObjectUtil;
import com.br.common.log.AlertLog;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.CarClueReportDTO;
import com.br.marketing.entity.CarClueInfo;
import com.br.marketing.mapper.*;
import com.br.marketing.service.*;
import com.br.marketing.service.carclue.clueenums.CarClueCompleteStatusEnum;
import com.br.marketing.service.carclue.clueenums.CarClueDataStatusEnum;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.vo.CarClueInfoVo;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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
    EntityOptServiceImpl entityOptService;
    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Override
    public PageResultReturn getReportList(CarClueReportDTO request) {
        Integer current = request.getCurrent();
        Integer size = request.getSize();

        Map params = new HashMap();
        params.put("createTimeStart", request.getCreateTimeStart());
        params.put("createTimeEnd", request.getCreateTimeEnd());
        params.put("intention", request.getIntention());
        params.put("clueDataStatusList", request.getClueDataStatus());
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

        List<String> allowedFields = Arrays.asList("create_time", "update_time", "push_time", "call_back_time");
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
            String cluePushChannel = getCluePushChannel(carClueInfoVo.getCluePushChannel());
            carClueInfoVo.setCluePushChannel(cluePushChannel);
        });

        return PageResultReturn.setPageResult(list, current, size);
    }

    public String encryptCell(String cell) {
        if (cell == null || cell.isEmpty()) {
            return "";
        }
        if (cell.length() < 7) {
            return cell;
        }
        return cell.substring(0, 3) + "****" + cell.substring(7);
    }



    @Override
    public ApiResult<Boolean> editCarClues(List<CarClueInfo> voList) {
        if (voList == null || voList.isEmpty()) {
            return new ApiResult<Boolean>().fail(false, "更新列表不能为空");
        }
        List<Integer> list = Arrays.asList(CarClueDataStatusEnum.ABNORMAL_CLUE.getValue(), CarClueDataStatusEnum.LACK_CLUE.getValue());
        for (CarClueInfo vo : voList) {
            CarClueInfo clueInfo = new CarClueInfo();
            try {
                clueInfo.setId(vo.getId());
                clueInfo.setBrand(vo.getBrand());
                clueInfo.setSeries(vo.getSeries());
                clueInfo.setClueDataStatus(CarClueDataStatusEnum.READY.getValue());
                if (list.contains(vo.getClueDataStatus())) {
                    if (CarClueDataStatusEnum.ABNORMAL_CLUE.getValue().equals(vo.getClueDataStatus())) {
                        clueInfo.setClueCompleteStatus(CarClueCompleteStatusEnum.AETIFICAL_ABNORMAL_COMPLETE.getValue());
                    } else {
                        clueInfo.setClueCompleteStatus(CarClueCompleteStatusEnum.AETIFICAL_LACK_COMPLETE.getValue());
                    }
                } else {
                    log.warn(" 此条数据不是异常线索或缺失线索，无法修改完成状态！ 数据ID： " + vo.getId());
                    continue;
                }
                clueInfo.setUpdateTime(new Date());
                entityOptService.writeOptLog(vo.getId(), clueInfo, vo);
                carClueInfoMapper.updateByPrimaryKeySelective(clueInfo);
            } catch (Exception e) {
                log.warn(AlertLog.buildWarnMessage(
                        AlarmSendCodeEnum.CARCLUE_SERVICEERROR.getCode(),
                        "编辑车线索信息失败！voId: " + vo.getId()), e);
            }
        }
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

    public String getCluePushChannel(String apiCode) {
        Map<String, Object> configMap = marketingCommonConfig.getCarClueApiCodeMapping();

        if (configMap == null || !configMap.containsKey("apiCodeAndCarClue")) {
            return null;
        }

        Map<String, String> apiCodeAndCarClue = (Map<String, String>) configMap.get("apiCodeAndCarClue");

        return apiCodeAndCarClue.getOrDefault(apiCode, null);
    }

    private String camelToSnake(String str) {
        if (str == null) {
            return null;
        }
        return str.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

}
