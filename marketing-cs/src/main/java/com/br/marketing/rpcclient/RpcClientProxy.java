package com.br.marketing.rpcclient;


import com.br.marketing.entity.MerchantParam;
import com.br.marketing.entity.RequestLog;
import com.br.marketing.rpcclient.rpcclientImpl.*;

public class RpcClientProxy {

    /**
     * 查询商户信息
     *
     * @param apiCode
     * @return
     */
    public static MerchantParam getMerchantParam(String apiCode) {
        return GrpcClientInitConfig.isGrpc()
                ? UserCenterGrpcClient.getMerchantParam(apiCode)
                : UserCenterIceClient.getMerchantParam(apiCode);
    }


    /**
     * 查询商户名称
     *
     * @param apiCode
     * @return
     */
    public static String getCompanyMsg(String apiCode) {
        return GrpcClientInitConfig.isGrpc()
                ? UserCenterGrpcClient.getCompanyMsg(apiCode)
                : UserCenterIceClient.getCompanyMsg(apiCode);
    }

    /**
     * 解密
     *
     * @param param
     * @param type
     * @param alogrithm
     * @param swiftNumber
     * @return
     */
    public static String decode(String param, String type, String alogrithm, String swiftNumber) {
        return GrpcClientInitConfig.isGrpc()
                ? DecodeGrpcClient.query(param, type, alogrithm, swiftNumber)
                : DecodeClient.query(param, type, alogrithm, swiftNumber);
    }

    /**
     * 发送mom上传日志
     *
     * @param content
     */
    public static void sendUploadLog(String content) {
        if (GrpcClientInitConfig.isGrpc()) {
            BrokerGrpcClient.sendUploadLog(content);
        } else {
            BrokerIceClient.sendUploadLog(content);
        }
    }


    public static void sendRequestLog(RequestLog requestLog) {
        if (GrpcClientInitConfig.isGrpc()) {
            BrokerGrpcClient.sendRequestLog(requestLog);
        } else {
            BrokerIceClient.sendRequestLog(requestLog);
        }
    }
}
