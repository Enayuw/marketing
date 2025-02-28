package com.br.marketing.client.qifu.callrealtime;

import com.br.marketing.client.qifu.BizData;
import com.br.marketing.client.qifu.QryUserRealMessage;
import lombok.Data;

import java.util.List;

/**
 * @ClassName QryCallRealTimeResp
 * @Description 外呼信息查询接口 返参
 * @Author zhen.li1
 * @Date 2025/02/24 16:00
 */
@Data
public class QryCallRealTimeResp extends BizData {

    /**
     * 查询结果列表
     */
    private List<CallRealTimeDTO> dataDetails;


}
