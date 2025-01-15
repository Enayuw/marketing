package com.br.marketing.service.carclue.push;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.entity.CarClueInfo;
import com.br.marketing.mapper.CarClueInfoMapper;
import com.br.marketing.service.carclue.clueenums.CarClueDataStatusEnum;
import com.br.marketing.service.carclue.clueenums.CarCluePushStatusEnum;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public abstract class AbstractClueChannelPush {


    @Resource
    CarClueInfoMapper carClueInfoMapper;

    /**
     * 组装和推送逻辑
     * @param carClueInfo
     * @return
     */
    public abstract Result push(CarClueInfo carClueInfo);

    void updateClueStatus(Long id, CarCluePushStatusEnum carCluePushStatusEnum) {
        CarClueInfo updateEntity = new CarClueInfo();
        updateEntity.setId(id);
        updateEntity.setClueDataStatus(carCluePushStatusEnum.getValue());
        carClueInfoMapper.updateByPrimaryKeySelective(updateEntity);
    }

    /**
     * 过滤规则的名称
     * @return
     */
    abstract String label();
}
