package com.br.marketing.service;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.dto.PushShDXDTO;

public interface PushDataService {
    Result pushDassData(Long id);

    Result pushSevenTransferData(Long id);

    Result pushHaierData();

    Result queryHaierData();

    /**
     * 目前该方法不适用当前需求
     * @param id
     * @return
     */
    @Deprecated
    Result<Boolean> pushHaierTransferData(Long id);
    /**
     * 数禾推送电销
     * @param pushShDXDTO
     * @return code=1处理成功 data=true有推送 data=false无需推送
     */
    Result<Boolean> pushShDX(PushShDXDTO pushShDXDTO);

    /**
     * 单条推电销 a/b 一天推一条,true-->推;false-->不推
     * @param apiCode
     * @param custNum
     * @param status
     * @return
     */
    Boolean pushShDXSingleMutex(String apiCode, String custNum, String status, String userType);

    /**
     * 推送壹钱包营销数据
     * @param localid
     * @return
     */
    Result<Boolean> pushSftpToDbData(Long localid);

    /**
     * 推送携程营销数据
     * @param data
     * @return
     */
    Result<Boolean> pushXieChengToDbData(String data);

    String getHaierRequestId(String type);
}
