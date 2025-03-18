package com.br.marketing.service.carclue.push.impl;

import com.alibaba.fastjson.JSONObject;
import com.br.cloud.web.MethodType;
import com.br.cloud.web.PrometheusTimeMethod;
import com.br.common.encryption.BrCipherMaker;
import com.br.common.log.AlertLog;
import com.br.marketing.client.carclue.CarClueClient;
import com.br.marketing.client.carclue.dto.HxClueCommitDTO;
import com.br.marketing.common.annoation.RetryMethod;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.CarClueInfo;
import com.br.marketing.mapper.CarClueInfoMapper;
import com.br.marketing.service.carclue.clueenums.CarCluePushStatusEnum;
import com.br.marketing.service.carclue.clueenums.ChannelRule;
import com.br.marketing.service.carclue.push.AbstractClueChannelPush;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * @ClassName YiCarClueChannelPush
 * @Description 易车KA推送车线索
 * @Author kongbx
 * @Date 2025/1/19 15:12
 */
@Service
@Slf4j
public class YiCarKAClueChannelPush extends AbstractClueChannelPush {

    @Resource
    CarClueClient carClueClient;
    @Resource
    CarClueInfoMapper carClueInfoMapper;

    @Resource
    MarketingCommonConfig marketingCommonConfig;

    @Override
    @RetryMethod(isOrNoDbRetry = true)
    @PrometheusTimeMethod(buckets = {0.02d, 0.05d, 0.2d, 0.5d, 1d}, methodType = MethodType.REMOTE)
    public Result push(CarClueInfo carClueInfo, Integer retry) {
        try {
            JSONObject jo = marketingCommonConfig.getHxClientConfig();
            String task = jo.getString("ycKaTask");
            CarClueInfo clueInfo = new CarClueInfo();
            clueInfo.setId(carClueInfo.getId());
            clueInfo.setUpdateTime(new Date());
            clueInfo.setPushTime(new Date());
            HxClueCommitDTO hxClueCommitDTO = new HxClueCommitDTO();
            if(!StringUtils.isEmpty(carClueInfo.getCell())){
                hxClueCommitDTO.setPhone(BrCipherMaker.getInstance().decode(carClueInfo.getCell()));
            }
            hxClueCommitDTO.setMember(carClueInfo.getMember());
            hxClueCommitDTO.setProvince(carClueInfo.getClueMatchProvince());
            hxClueCommitDTO.setCity(carClueInfo.getClueMatchCity());
            hxClueCommitDTO.setBrand(carClueInfo.getClueMatchBrand());
            hxClueCommitDTO.setSeries(carClueInfo.getClueMatchSeries());
            if(carClueInfo.getClueMatchSeriesId() != null){
                hxClueCommitDTO.setSeriesId(Integer.parseInt(carClueInfo.getClueMatchSeriesId()));
            }
            hxClueCommitDTO.setPushTask(task);
            hxClueCommitDTO.setAssignId(carClueInfo.getDemandId());
            hxClueCommitDTO.setBuyTime(LocalDate.now().plusDays(90).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            Result<String> clueRes = carClueClient.commitClue(hxClueCommitDTO);
            if (ResultCode.SUCCESS.getValue().equals(clueRes.getCode())) {
                clueInfo.setCluePushStatus(CarCluePushStatusEnum.SUCCESS.getValue());
                clueInfo.setClueId(clueRes.getData());
            } else {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.CARCLUE_SERVICEERROR.getCode()
                        , "车线索-易车KA，推送线索异常,result= " + clueRes.getMessage()));

                clueInfo.setCluePushStatus(CarCluePushStatusEnum.FAIL.getValue());
                clueInfo.setCluePushErrorReason(clueRes.getMessage());
                carClueInfoMapper.updateByPrimaryKeySelective(clueInfo);
                return new Result<>().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
            }
            carClueInfoMapper.updateByPrimaryKeySelective(clueInfo);
            return new Result<>().setCode(ResultCode.SUCCESS.getValue());
        } catch (Exception e) {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.CARCLUE_SERVICEERROR.getCode()
                    , "车线索-易车KA，推送线索异常,线索id= " + carClueInfo.getClueId()));
            return new Result<>().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
        }
    }

    @Override
    public String label() {
        return ChannelRule.PushChannelRuleEnum.YC_KA_PUSH.getLabel();
    }

}
