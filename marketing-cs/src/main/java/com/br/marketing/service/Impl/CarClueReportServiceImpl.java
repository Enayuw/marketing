package com.br.marketing.service.Impl;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.enums.ServiceResultEnum;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.CarClueReportDTO;
import com.br.marketing.entity.CarClueInfo;
import com.br.marketing.mapper.*;
import com.br.marketing.service.*;
import com.br.marketing.service.carclue.clueenums.CarClueDataStatusEnum;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.vo.CarClueInfoVo;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

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
    private MarketingCommonConfig marketingCommonConfig;

    @Override
    public PageResultReturn getReportList(CarClueReportDTO request) {
        Integer current = request.getCurrent();
        Integer size = request.getSize();
        Map params = new HashMap();
        params.put("createTimeStart", request.getCreateTimeStart());
        params.put("createTimeEnd", request.getCreateTimeEnd());
        params.put("intention", request.getIntention());
        params.put("clueDataStatus", request.getClueDataStatus());
        params.put("updateTimeStart", request.getUpdateTimeStart());
        params.put("updateTimeEnd", request.getUpdateTimeEnd());
        params.put("cluePushChannel", request.getCluePushChannel());
        params.put("cluePushStatus", request.getCluePushStatus());
        params.put("pushTimeStart", request.getPushTimeStart());
        params.put("pushTimeEnd", request.getPushTimeEnd());
        params.put("status", request.getStatus());
        params.put("callBackTimeStart", request.getCallBackTimeStart());
        params.put("callBackTimeEnd", request.getCallBackTimeEnd());

        PageHelper.startPage(current, size);
        List<CarClueInfoVo> list = carClueInfoMapper.selectList(params);
        list.forEach((CarClueInfoVo carClueInfoVo) -> {
            String encryptCell = encryptCell(carClueInfoVo.getCell());
            carClueInfoVo.setCell(encryptCell);
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
    public ApiResult<Boolean> editCarClues(List<CarClueInfoVo> voList) {
        if (voList == null || voList.isEmpty()) {
            return new ApiResult<Boolean>().fail(false, "更新列表不能为空");
        }

        List<CarClueInfo> clueInfoList = new ArrayList<>();
        for (CarClueInfoVo vo : voList) {
            CarClueInfo clueInfo = new CarClueInfo();
            try {
                BeanUtils.copyProperties(clueInfo, vo);
                clueInfo.setBrand(vo.getBrand());
                clueInfo.setSeries(vo.getSeries());
                clueInfo.setClueDataStatus(CarClueDataStatusEnum.READY.getValue());
                clueInfo.setUpdateTime(new Date());
                clueInfoList.add(clueInfo);
            } catch (Exception e) {
                log.warn(AlertLog.buildWarnMessage(
                        AlarmSendCodeEnum.CARCLUE_SERVICEERROR.getCode(),
                        "编辑车线索信息失败！voId: " + vo.getId()), e);
            }
        }

        try {
            int updatedRows = carClueInfoMapper.batchUpdate(clueInfoList);
            if (updatedRows <= 0) {
                log.warn(AlertLog.buildWarnMessage(
                        AlarmSendCodeEnum.CARCLUE_SERVICEERROR.getCode(),
                        "批量编辑车线索信息失败！更新行数为0"));
                return new ApiResult<Boolean>().fail(false, ServiceResultEnum.FAILED);
            }
            return new ApiResult<Boolean>().success(true);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(
                    AlarmSendCodeEnum.CARCLUE_SERVICEERROR.getCode(),
                    "批量编辑车线索信息失败！"), e);
            return new ApiResult<Boolean>().fail(false, ServiceResultEnum.FAILED);
        }
    }



    @Override
    public List<CarClueInfoVo> getCarInfoLike(String search) {
        List<CarClueInfoVo> list = carClueInfoMapper.getCarInfoLike(search);

        List<CarClueInfoVo> vos = list.stream().map(marketingCustomer -> {
            CarClueInfoVo vo = new CarClueInfoVo();
            org.springframework.beans.BeanUtils.copyProperties(marketingCustomer, vo);
            vo.setId(marketingCustomer.getId());
            return vo;
        }).collect(Collectors.toList());

        return vos;
    }

    @Override
    public ApiResult<String> getValueByKey(String key) {
        try {
            Map<String, JSONObject> clueApiCodeMapping = marketingCommonConfig.getCarClueApiCodeMapping();
            JSONObject jsonObject = clueApiCodeMapping.get("channel");
            if (ObjectUtil.isEmpty(jsonObject)) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.CARCLUE_SERVICEERROR.getCode(),
                        "渠道不存在！"));
            }
            String string = jsonObject.getString(key);
            String result = ObjectUtil.isNotEmpty(string) ? string : "fail";
            return new ApiResult<String>().success().setData(result);
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.CARCLUE_SERVICEERROR.getCode(),
                    "获取推送渠道映射失败！错误信息：" + e.getMessage()), e);
            return new ApiResult<String>().fail("处理失败，请稍后重试！");
        }
    }


}
