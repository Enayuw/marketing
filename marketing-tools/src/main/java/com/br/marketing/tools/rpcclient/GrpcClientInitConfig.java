package com.br.marketing.tools.rpcclient;

import com.br.grpc.encodemapping.EncodeMappingGrpc;
import com.br.grpc.utils.BrGrpcUtils;
import io.grpc.ManagedChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
@Slf4j
public class GrpcClientInitConfig {

    private static Environment _environment;

    @Autowired
    Environment environment;

    private static final String DOMAIN_SERVICE_NAME = "grpc.brapp.com";

    private static final String USER_CENTER_SERVICE_NAME = "grpc-user-center-service";
    private static final String ENCODE_SERVICE_NAME = "grpc-encode-mapping-service";
    private static final String BROKER_SERVICE_NAME = "grpc-broker-layer-service";
    private static final String STRATEGY_SERVICE_NAME = "grpc-strategy-service";


    @PostConstruct
    void init() throws Exception {
            domainInit();
    }

    void domainInit() throws Exception {
        ManagedChannel[] managedChannels = BrGrpcUtils.initChannels(DOMAIN_SERVICE_NAME);
        for (int i = 0; i < managedChannels.length; i++) {
            //加解密
            EncodeMappingGrpc.EncodeMappingBlockingStub _encodeBlockingStub = (EncodeMappingGrpc.EncodeMappingBlockingStub)
                    BrGrpcUtils.newBlockStub(DOMAIN_SERVICE_NAME, managedChannels[i], EncodeMappingGrpc.class);
            _encodeBlockingStub.ping(com.br.grpc.encodemapping.PingRequest.newBuilder().build());
        }
    }

    public static EncodeMappingGrpc.EncodeMappingBlockingStub grpcEncode() {
        EncodeMappingGrpc.EncodeMappingBlockingStub _encodeBlockingStub = null;
        try {
            _encodeBlockingStub = (EncodeMappingGrpc.EncodeMappingBlockingStub)
                    BrGrpcUtils.newBlockStub( DOMAIN_SERVICE_NAME, EncodeMappingGrpc.class);
        } catch (Exception e) {
            log.error("加解密grpc客户端错误", e);
        }
        return _encodeBlockingStub;
    }

}
