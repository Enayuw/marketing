package com.br.marketing.service.carclue.impl;

import com.alibaba.fastjson.JSON;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.HxClueCallBackReqDTO;
import com.br.marketing.entity.CarClueInfo;
import com.br.marketing.entity.CarClueInfoExample;
import com.br.marketing.mapper.CarClueInfoMapper;
import com.br.marketing.service.carclue.ICarClueService;
import org.apache.commons.collections.CollectionUtils;
import org.checkerframework.checker.units.qual.C;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Service
public class ICarClueServiceImpl implements ICarClueService {

    @Resource
    CarClueInfoMapper carClueInfoMapper;

    @Override
    public Result callBackClue(@Validated HxClueCallBackReqDTO reqDTO) {
        String clueId = reqDTO.getClueId();
        if(StringUtils.isBlank(clueId)){
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("clueId不能为空");
        }
        CarClueInfoExample infoExample = new CarClueInfoExample();
        infoExample.createCriteria().andClueIdEqualTo(clueId);
        List<CarClueInfo> carClueInfos = carClueInfoMapper.selectByExample(infoExample);
        if(CollectionUtils.isEmpty(carClueInfos)){
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("线索信息不存在");
        }
        CarClueInfo updateEntity = new CarClueInfo();
        updateEntity.setClueCallbackResult(JSON.toJSONString(reqDTO));
        // 2025-02-27 海星开发同步该接口返回的pushState就是入库状态
        updateEntity.setClueCallbackFinalState(reqDTO.getPushState());
//        updateEntity.setClueCallbackPushState(reqDTO.getPushState());
        updateEntity.setCallBackTime(new Date());
        updateEntity.setId(carClueInfos.get(0).getId());
        carClueInfoMapper.updateByPrimaryKeySelective(updateEntity);
        return new Result().setCode(ResultCode.SUCCESS.getValue()).setMessage("推送成功");
    }
}
