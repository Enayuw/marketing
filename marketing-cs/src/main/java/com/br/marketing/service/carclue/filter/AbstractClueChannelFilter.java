package com.br.marketing.service.carclue.filter;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.entity.CarClueInfo;
import com.br.marketing.mapper.CarClueInfoMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public abstract class AbstractClueChannelFilter {


    @Resource
    CarClueInfoMapper carClueInfoMapper;

    public Result filter(CarClueInfo carClueInfo) {
        Result action = action(carClueInfo);
        if (action.isSuccess()) {
            updateClueStatus(carClueInfo.getId());
            return new Result().setCode(ResultCode.SUCCESS.getValue());
        }
        return new Result().setCode(ResultCode.FAIL.getValue());
    }

    void updateClueStatus(Long id) {
        String filterLabel = filterLabel();
        CarClueInfo updateEntity = new CarClueInfo();
        updateEntity.setId(id);
        //todo 等李震的枚举值创建进行赋值
//        updateEntity.setClueDataStatus();
        StringBuilder sb = new StringBuilder();
        sb.append("【").append(filterLabel()).append("】");
        updateEntity.setClueErrorReason(sb.toString());
        carClueInfoMapper.updateByPrimaryKeySelective(updateEntity);
    }

    /**
     * 线索命中该规则需要把数据置为无效状态 并且更新线索的数据
     * code 1-命中；0-为命中；
     * 如果命中 需要把命中
     *
     * @param carClueInfo
     * @return
     */
    abstract Result<String> action(CarClueInfo carClueInfo);

    /**
     * 过滤规则的名称
     * @return
     */
    abstract String filterLabel();
}
