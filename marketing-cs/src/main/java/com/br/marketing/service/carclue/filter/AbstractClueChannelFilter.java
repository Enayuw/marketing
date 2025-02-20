package com.br.marketing.service.carclue.filter;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.entity.CarClueInfo;
import com.br.marketing.mapper.CarClueInfoMapper;
import com.br.marketing.service.carclue.clueenums.CarClueDataStatusEnum;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public abstract class AbstractClueChannelFilter {


    @Resource
    CarClueInfoMapper carClueInfoMapper;

    /**
     * 过滤的规则的调用入口
     * code 1-命中过滤规则 0-没命中过滤规则
     * @param carClueInfo
     * @return
     */
    public Result filter(CarClueInfo carClueInfo, String apiCode) {
        Result action = action(carClueInfo, apiCode);
        if (action.isSuccess()) {
            //updateClueStatus(carClueInfo.getId());
            carClueInfo.setClueDataStatus(CarClueDataStatusEnum.INVALID_CLUE.getValue());
            StringBuilder sb = new StringBuilder();
            sb.append("[").append(label()).append("]");
            carClueInfo.setClueErrorReason(sb.toString());
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(carClueInfo);
        }
        return new Result().setCode(ResultCode.FAIL.getValue());
    }

    void updateClueStatus(Long id) {
        CarClueInfo updateEntity = new CarClueInfo();
        updateEntity.setId(id);
        updateEntity.setClueDataStatus(CarClueDataStatusEnum.INVALID_CLUE.getValue());
        StringBuilder sb = new StringBuilder();
        sb.append("【").append(label()).append("】");
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
    abstract Result<String> action(CarClueInfo carClueInfo,String apiCode);

    /**
     * 过滤规则的名称
     * @return
     */
    public abstract String label();
}
