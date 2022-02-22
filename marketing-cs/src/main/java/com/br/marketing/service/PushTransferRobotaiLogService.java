package com.br.marketing.service;

import com.br.marketing.client.robotaiapi.input.TransferRobotOutboundDTO;
import com.br.marketing.client.robotaiapi.output.TransferRobotOutboundVO;
import com.br.marketing.client.robotaiapi.output.UnsuccessfulData;
import com.br.marketing.entity.MarketingTransferInfo;
import com.br.marketing.entity.PushTransferRobotaiLog;

/**
 * @author zeqiang.guo@brgroup.com
 * @dateTime 2021/11/5 21:37
 */
public interface PushTransferRobotaiLogService {

    int saveLog(MarketingTransferInfo transferInfo, TransferRobotOutboundDTO robotOutboundDTO, TransferRobotOutboundVO<UnsuccessfulData> outboundVO);

    int save2Log(MarketingTransferInfo transferInfo, TransferRobotOutboundDTO robotOutboundDTO, TransferRobotOutboundVO<UnsuccessfulData> outboundVO);

    int save(PushTransferRobotaiLog pushTransferRobotaiLog);
}
