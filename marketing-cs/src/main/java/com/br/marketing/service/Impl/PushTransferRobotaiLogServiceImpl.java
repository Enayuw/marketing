package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSON;
import com.br.common.util.DateUtils;
import com.br.common.util.StringUtils;
import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.client.robotaiapi.input.ConversionData;
import com.br.marketing.client.robotaiapi.input.TransferRobotOutboundDTO;
import com.br.marketing.client.robotaiapi.output.TransferRobotOutboundVO;
import com.br.marketing.client.robotaiapi.output.UnsuccessfulData;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.Constants;
import com.br.marketing.entity.MarketingTransferInfo;
import com.br.marketing.entity.PushTransferRobotaiLog;
import com.br.marketing.mapper.PushTransferRobotaiLogMapper;
import com.br.marketing.service.PushTransferRobotaiLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @author zeqiang.guo@brgroup.com
 * @dateTime 2021/11/5 21:37
 */
@Service
@Slf4j
public class PushTransferRobotaiLogServiceImpl implements PushTransferRobotaiLogService {

    @Resource
    private PushTransferRobotaiLogMapper pushTransferRobotaiLogMapper;
    @Resource
    private AlarmApiClient alarmClient;
    @Value("${otherConfig.alarm.secretKey:00}")
    private String secretKey;
    @Value("${otherConfig.alarm.appName:00}")
    private String appName;

    @Override
    public int saveLog(MarketingTransferInfo transferInfo, TransferRobotOutboundDTO robotOutboundDTO, TransferRobotOutboundVO<UnsuccessfulData> outboundVO) {
        List<ConversionData> conversionData = robotOutboundDTO.getJsonData().getConversionData();
        String apiCode = transferInfo.getApiCode();
        final String message = outboundVO.getMessage();
        PushTransferRobotaiLog pushTransferRobotaiLog = new PushTransferRobotaiLog(
                transferInfo.getId()
                , apiCode
                , transferInfo.getRequestId()
                , JSON.toJSONString(outboundVO.getData())
                , outboundVO.getCode()
                , StringUtils.isNotEmpty(message) && message.length() > 255 ? message.substring(0, 255) : message
                , robotOutboundDTO.getJsonData().getConversionData().size()
                , JSON.toJSONString(robotOutboundDTO)
                , String.valueOf(Math.abs(Integer.parseInt(conversionData.get(0).getCid())))
        );
        int insert = pushTransferRobotaiLogMapper.insert(pushTransferRobotaiLog);
        String TITLE = "接口转化(通用标准)数据同步到智能客服警告";
        if (insert == 0) {
            String smg = String.format("apiCode为[%s]的客户转化数据推送失败后记录日志到db失败;日期[%s];"
                    , apiCode, DateUtils.format(new Date()));
            log.error(smg.concat("\n#返回记录：").concat(outboundVO.getData().toString()).concat("\n发送记录：")
                    .concat(robotOutboundDTO.toString()));
            alarmClient.sendAlarm(smg, TITLE, AlarmSendCodeEnum.EXCEPTION_COMMON.getCode());
        }
        String responseBody = pushTransferRobotaiLog.getResponseBody();
        String smg = String.format("apiCode:[%s];requestId:[%s];transferInfoId:[%s];tCid:[%s]的客户转化数据推送失败或部分失败!" +
                        "\n日期[%s];" +
                        "\n应答内容:[%s:%s]" +
                        "\n未成功数据情况:[%s]"
                , apiCode, transferInfo.getRequestId(), transferInfo.getId(), pushTransferRobotaiLog.gettCid()
                , DateUtils.format(new Date()), outboundVO.getCode(), outboundVO.getMessage()
                , responseBody.length() > 300 ? responseBody.substring(0, 300).concat("...") : responseBody);
        Object data = outboundVO.getData();
        log.warn(smg.concat("\n#失败记录：").concat(data == null ? "" : data.toString()));
        alarmClient.sendAlarm(smg, TITLE, AlarmSendCodeEnum.EXCEPTION_COMMON.getCode());
        return insert;
    }

    @Override
    public int save2Log(MarketingTransferInfo transferInfo, TransferRobotOutboundDTO robotOutboundDTO, TransferRobotOutboundVO<UnsuccessfulData> outboundVO) {
        List<ConversionData> conversionData = robotOutboundDTO.getJsonData().getConversionData();
        String apiCode = transferInfo.getApiCode();
        PushTransferRobotaiLog pushTransferRobotaiLog = new PushTransferRobotaiLog(
                transferInfo.getId()
                , apiCode
                , transferInfo.getRequestId()
                , JSON.toJSONString(outboundVO.getData())
                , outboundVO.getCode()
                , outboundVO.getMessage()
                , robotOutboundDTO.getJsonData().getConversionData().size()
                , JSON.toJSONString(robotOutboundDTO)
                , String.valueOf(Math.abs(Integer.parseInt(conversionData.get(0).getCid())))
        );
        pushTransferRobotaiLog.setPushStatus(3);
        pushTransferRobotaiLog.setCompensateTimes(-1);
        int insert = pushTransferRobotaiLogMapper.insert(pushTransferRobotaiLog);
        String TITLE = "接口转化(通用标准)数据同步到智能客服警告";
        if (insert == 0) {
            String smg = String.format("apiCode为[%s]的客户转化数据推送失败后记录日志到db失败;日期[%s];"
                    , apiCode, DateUtils.format(new Date()));
            log.error(smg.concat("\n#返回记录：").concat(outboundVO.getData().toString()).concat("\n发送记录：")
                    .concat(robotOutboundDTO.toString()));
            alarmClient.sendAlarm(smg, TITLE, AlarmSendCodeEnum.EXCEPTION_COMMON.getCode());
        }
        return insert;
    }

    @Override
    public int save(PushTransferRobotaiLog pushTransferRobotaiLog) {
        Assert.notNull(pushTransferRobotaiLog, "保存内容不可为空");
        return pushTransferRobotaiLogMapper.insert(pushTransferRobotaiLog);
    }
}
