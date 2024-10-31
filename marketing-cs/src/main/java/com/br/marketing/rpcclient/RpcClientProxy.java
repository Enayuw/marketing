package com.br.marketing.rpcclient;


import com.br.marketing.entity.MerchantParam;
import com.br.marketing.entity.RequestLog;
import com.br.marketing.rpcclient.rpcclientImpl.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RpcClientProxy {

    /**
     * 查询商户信息
     *
     * @param apiCode
     * @return
     */
    public static MerchantParam getMerchantParam(String apiCode) {
//        log.warn(GrpcClientInitConfig.isGrpc()?"grpc:getMerchantParam:查询商户信息":"ice:getMerchantParam:查询商户信息");
        return UserCenterGrpcClient.getMerchantParam(apiCode);
    }


    /**1`
     * 查询商户名称
     *
     * @param apiCode
     * @return
     */
    public static String getCompanyMsg(String apiCode) {
//        log.warn(GrpcClientInitConfig.isGrpc()?"grpc:getCompanyMsg:查询商户名称":"ice:getCompanyMsg:查询商户名称");
        return UserCenterGrpcClient.getCompanyMsg(apiCode);
    }

    /**1`
     * 查询商户名称
     *
     * @param apiCode
     * @return
     */
    public static String getCustomerMsg(String apiCode) {
//        log.warn(GrpcClientInitConfig.isGrpc()?"grpc:getCompanyMsg:查询商户名称":"ice:getCompanyMsg:查询商户名称");
        return UserCenterGrpcClient.getCustomerMsg(apiCode);
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
//        log.warn(GrpcClientInitConfig.isGrpc()?"grpc:decode:解密":"ice:decode:解密");
        return DecodeGrpcClient.query(param, type, alogrithm, swiftNumber);
    }

    /**
     * 发送mom上传日志
     *
     * @param content
     */
    public static void sendUploadLog(String content) {
//        log.warn(GrpcClientInitConfig.isGrpc()?"grpc:sendUploadLog:发送mom上传日志":"ice:sendUploadLog:发送mom上传日志");
        BrokerGrpcClient.sendUploadLog(content);
    }


    public static void sendRequestLog(RequestLog requestLog) {
//        log.warn(GrpcClientInitConfig.isGrpc()?"grpc:sendRequestLog:mom接口日志":"ice:sendRequestLog:mom接口日志");
        BrokerGrpcClient.sendRequestLog(requestLog);
    }
}
