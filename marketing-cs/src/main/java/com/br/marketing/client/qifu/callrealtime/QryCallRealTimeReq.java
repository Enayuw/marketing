package com.br.marketing.client.qifu.callrealtime;

import com.br.marketing.client.qifu.BizData;
import lombok.Data;

import java.util.List;

/**
 * @ClassName QryCallRealTimeReq
 * @Description 外呼信息查询接口 入参
 * @Author zhen.li1
 * @Date 2025/02/24 15:28
 */
@Data
public class QryCallRealTimeReq extends BizData {

    /**
     * seriaNo集合
     */
    private List<String> serialNoList;


    /**
     * 流水号
     */
    private String requestNo;


    /**
     * 呼叫类型
     */
    private String callType;








}
