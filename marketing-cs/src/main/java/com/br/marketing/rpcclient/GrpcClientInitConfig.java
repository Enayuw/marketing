package com.br.marketing.rpcclient;

import com.br.grpc.encodemapping.EncodeMappingGrpc;
import com.br.grpc.mom.broker_layer_api.BrokerLayerGrpc;
import com.br.grpc.service.usercenterrely.UserCenterGreeterGrpc;
import com.br.grpc.utils.BrGrpcUtils;
import io.grpc.ManagedChannel;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Objects;

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
        _environment = environment;
        if (isDomain()) {
            domainInit();
        } else {
            BrGrpcUtils.initChannels(USER_CENTER_SERVICE_NAME);
            BrGrpcUtils.initChannels(ENCODE_SERVICE_NAME);
            BrGrpcUtils.initChannels(BROKER_SERVICE_NAME);
            BrGrpcUtils.initChannels(STRATEGY_SERVICE_NAME);
        }
    }

    void domainInit() throws Exception {
        ManagedChannel[] managedChannels = BrGrpcUtils.initChannels(DOMAIN_SERVICE_NAME);
        for (int i = 0; i < managedChannels.length; i++) {
            //用户中心
            UserCenterGreeterGrpc.UserCenterGreeterBlockingStub _userBlockingStub = (UserCenterGreeterGrpc.UserCenterGreeterBlockingStub)
                    BrGrpcUtils.newBlockStub(DOMAIN_SERVICE_NAME, managedChannels[i], UserCenterGreeterGrpc.class);

//            _userBlockingStub = _userBlockingStub.withDeadlineAfter(10, TimeUnit.SECONDS);
            _userBlockingStub.ping(com.br.grpc.service.usercenterrely.PingRequest.newBuilder().build());

            //加解密
            EncodeMappingGrpc.EncodeMappingBlockingStub _encodeBlockingStub = (EncodeMappingGrpc.EncodeMappingBlockingStub)
                    BrGrpcUtils.newBlockStub(DOMAIN_SERVICE_NAME, managedChannels[i], EncodeMappingGrpc.class);
//            _encodeBlockingStub = _encodeBlockingStub.withDeadlineAfter(10, TimeUnit.SECONDS);
            _encodeBlockingStub.ping(com.br.grpc.encodemapping.PingRequest.newBuilder().build());

            //生产者
            BrokerLayerGrpc.BrokerLayerFutureStub _futureBrokerStub = (BrokerLayerGrpc.BrokerLayerFutureStub)
                    BrGrpcUtils.newFutureStub(DOMAIN_SERVICE_NAME, managedChannels[i], BrokerLayerGrpc.class);
//            _futureBrokerStub = _futureBrokerStub.withDeadlineAfter(10, TimeUnit.SECONDS);
            _futureBrokerStub.ping(com.br.grpc.mom.broker_layer_api.PingRequest.newBuilder().build());

            //策略
//            StrategyClientGrpc.StrategyClientBlockingStub _strategyBlockingStub = (StrategyClientGrpc.StrategyClientBlockingStub)
//                    BrGrpcUtils.newBlockStub(DOMAIN_SERVICE_NAME, managedChannels[i], UserCenterGreeterGrpc.class);
//            _strategyBlockingStub.ping(com.br.grpc.client.strategy.Request.newBuilder().build());
        }
    }


    public static UserCenterGreeterGrpc.UserCenterGreeterBlockingStub grpcUserCenter() {
        UserCenterGreeterGrpc.UserCenterGreeterBlockingStub _userBlockingStub = null;
        try {
            _userBlockingStub = (UserCenterGreeterGrpc.UserCenterGreeterBlockingStub)
                    BrGrpcUtils.newBlockStub(isDomain() ? DOMAIN_SERVICE_NAME : USER_CENTER_SERVICE_NAME, UserCenterGreeterGrpc.class);
        } catch (Exception e) {
            log.error("获取用户中心grpc客户端错误", e);
        }
        return _userBlockingStub;
    }


    public static EncodeMappingGrpc.EncodeMappingBlockingStub grpcEncode() {
        EncodeMappingGrpc.EncodeMappingBlockingStub _encodeBlockingStub = null;
        try {
            _encodeBlockingStub = (EncodeMappingGrpc.EncodeMappingBlockingStub)
                    BrGrpcUtils.newBlockStub(isDomain() ? DOMAIN_SERVICE_NAME : ENCODE_SERVICE_NAME, EncodeMappingGrpc.class);
        } catch (Exception e) {
            log.error("加解密grpc客户端错误", e);
        }
        return _encodeBlockingStub;
    }


    public static BrokerLayerGrpc.BrokerLayerFutureStub grpcBroker() {
        BrokerLayerGrpc.BrokerLayerFutureStub _futureBrokerStub = null;
        try {
            _futureBrokerStub = (BrokerLayerGrpc.BrokerLayerFutureStub)
                    BrGrpcUtils.newFutureStub(isDomain() ? DOMAIN_SERVICE_NAME : BROKER_SERVICE_NAME, BrokerLayerGrpc.class);
        } catch (Exception e) {
            log.error("获取生产者grpc客户端错误", e);
        }
        return _futureBrokerStub;
    }

    static Boolean isDomain() {
        String env = _environment.getActiveProfiles()[0];
        String grpcInitFlag = _environment.getProperty("grpc.init");
        return "dev".equals(env) || "pre".equals(env) || Objects.equals(grpcInitFlag, "true");
    }

}
