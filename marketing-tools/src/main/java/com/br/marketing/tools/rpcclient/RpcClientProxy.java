package com.br.marketing.tools.rpcclient;


import com.br.marketing.tools.rpcclient.rpcclientImpl.DecodeGrpcClient;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RpcClientProxy {



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

}
