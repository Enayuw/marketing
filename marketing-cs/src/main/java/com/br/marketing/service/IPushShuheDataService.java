package com.br.marketing.service;

import com.br.marketing.client.AlarmApiClient;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.dto.ResponseCustomDTO;

/**
 * 数禾推转化数据接口
 *
 * @author Guo Zeqiang
 * @dateTime 2022/2/10 14:24
 */
public interface IPushShuheDataService {

    ResponseCustomDTO saveShuheTransferDataTwoVersion(String apiCode, String jsonData);

    Result<Boolean> consumerShTransfer(String msg);

    default void sendAlarmMgs(String title, String error, AlarmApiClient alarmClient) {
        try {
            alarmClient.sendAlarm(error, title, AlarmSendCodeEnum.EXCEPTION_COMMON.getCode());
        } catch (Exception ignored) {

        }
    }

    default void sendAlarmMgsUrgent(String title, String error, AlarmApiClient alarmClient) {
        try {
            alarmClient.sendAlarm(error, title, AlarmSendCodeEnum.EXCEPTION_USUAL_NOTICE.getCode());
        } catch (Exception ignored) {

        }
    }
    //
    // /**
    // * 构建数禾上下文
    // */
    // void handlerContext(ShuHeProcessHandlerContext context, MarketingTransferSyncUser transfer);
    //
    // /**
    // * 删除数禾上下文
    // */
    // void removeHandlerContext();

    /**
     * 保存上传数据
     *
     * @param apiCode apiCode
     * @param jsonData 业务数据
     * @return ResponseShuheDTO
     * @author Guo Zeqiang
     */
    ResponseCustomDTO saveUploadData(String apiCode, String jsonData);

    Result<Boolean> consumerShUpload(String msg);

}
