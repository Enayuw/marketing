package com.br.marketing.service.carclue.callback;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.entity.CarClueInfo;
import com.br.marketing.mapper.CarClueInfoMapper;
import com.br.marketing.service.carclue.clueenums.CarClueCallBackStatusEnum;
import com.br.marketing.service.carclue.clueenums.CarCluePushStatusEnum;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public abstract class AbstractClueChannelCallBack {


    @Resource
    CarClueInfoMapper carClueInfoMapper;

    /**
     * 回调逻辑
     *
     * @param carClueInfo
     * @return
     */
    public abstract Result callback(CarClueInfo carClueInfo);

    void updateClueStatus(Long id, CarClueCallBackStatusEnum callBackStatusEnum, String message) {
        CarClueInfo updateEntity = new CarClueInfo();
        updateEntity.setId(id);
        updateEntity.setClueDataStatus(callBackStatusEnum.getValue());
        updateEntity.setClueCallbackResult(CarClueCallBackStatusEnum.FAIL.getValue().equals(callBackStatusEnum.getValue()) ? message : null);
        carClueInfoMapper.updateByPrimaryKeySelective(updateEntity);
    }

    /**
     * 过滤规则的名称
     *
     * @return
     */
    public abstract String label();
}
