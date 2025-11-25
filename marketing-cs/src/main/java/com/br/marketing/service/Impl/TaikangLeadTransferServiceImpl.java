package com.br.marketing.service.Impl;

import com.br.common.log.AlertLog;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.entity.CallRecording;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.mapper.CallRecordingMapper;
import com.br.marketing.mapper.MarketingSyncInfoMapper;
import com.br.marketing.service.TaikangLeadTransferService;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TaikangLeadTransferServiceImpl implements TaikangLeadTransferService {

    @Resource
    private CallRecordingMapper callRecordingMapper;

    @Resource
    private MarketingSyncInfoMapper marketingSyncInfoMapper;

    @Override
    public Result<Boolean> transferData(String id) {
        Result<Boolean> result = new Result<>();
        CallRecording callRecording = callRecordingMapper.selectByPrimaryKey(Long.valueOf(id));
        if (callRecording != null) {
            MarketingSyncUser syncUser = marketingSyncInfoMapper.getNewestByCusnumAndStatus(callRecording.getApiCode(), callRecording.getCustNum());
            String cell = syncUser.getCell();
            String decode = BrCipherMaker.getInstance().decode(cell);
            if (decode == null) {
                log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TAIKANG_MARKING_SERVICEERROR.getCode(), "泰康大健康线索线索推送客户，该cell:" + cell +
                        "解密失败，请关注！！！"));
            }

        } else {
            log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.TAIKANG_MARKING_SERVICEERROR.getCode(), "泰康大健康线索线索推送客户，未查询到该id:" + id +
                    "对应通话明细，请关注！！！"));
        }
        return result;
    }
}
