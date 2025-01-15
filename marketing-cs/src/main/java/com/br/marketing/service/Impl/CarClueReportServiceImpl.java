package com.br.marketing.service.Impl;

import cn.hutool.core.util.ObjectUtil;
import com.br.marketing.common.commondto.ApiResult;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.commonentity.PageResultReturn;
import com.br.marketing.dto.CarClueReportDTO;
import com.br.marketing.entity.CallRecordLog;
import com.br.marketing.entity.CarClueInfo;
import com.br.marketing.mapper.*;
import com.br.marketing.service.*;
import com.br.marketing.vo.CarClueInfoVo;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.lang.reflect.InvocationTargetException;
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
    private CallRecordLogMapper callRecordLogMapper;

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
        params.put("callBackTimeStart", request.getCallBackTimeStart());
        params.put("callBackTimeEnd", request.getCallBackTimeEnd());

        PageHelper.startPage(current, size);
        List<CarClueInfoVo> list = carClueInfoMapper.selectList(params);
        list.forEach((CarClueInfoVo carClueInfoVo) -> {
            CallRecordLog callRecordLog = callRecordLogMapper.selectByrecordId(carClueInfoVo.getId());
            String status = ObjectUtil.isNotEmpty(callRecordLog) ? callRecordLog.getInboundStatus().toString() : "0";
            carClueInfoVo.setStatus(status);
        });

        return PageResultReturn.setPageResult(list, current, size);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<Boolean> editCarClue(CarClueInfoVo vo) {
        CarClueInfo clueInfo = new CarClueInfo();

        try {
            BeanUtils.copyProperties(clueInfo, vo);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        clueInfo.setBrand(vo.getBrand());
        clueInfo.setSeries(vo.getSeries());
        clueInfo.setUpdateTime(new Date());
        int update = carClueInfoMapper.updateByPrimaryKeySelective(clueInfo);
        if (StringUtils.isEmpty(update) || update <= 0) {
            log.error("编辑车线索信息失败！");
        }
        return new ApiResult<Boolean>().success(true);
    }
}
