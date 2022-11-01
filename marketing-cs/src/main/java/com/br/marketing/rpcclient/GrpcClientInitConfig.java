package com.br.marketing.rpcclient;

import com.br.grpc.service.usercenterrely.PingRequest;
import com.br.grpc.service.usercenterrely.UserCenterGreeterGrpc;
import com.br.grpc.utils.BrGrpcUtils;
import io.grpc.ManagedChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class GrpcClientInitConfig {

    @Autowired
    Environment environment;

    private static final String USER_CENTER_SERVICE_NAME = "grpc-user-center-service";
//    private static final String USER_CENTER_SERVICE_NAME = "grpc-user-center-service";

    @PostConstruct
    void init() throws Exception {
        String env = environment.getActiveProfiles()[0];
        if ("dev".equals(env) || "pre".equals(env)) {
            domainInit();
        } else {
            BrGrpcUtils.initChannels(USER_CENTER_SERVICE_NAME);
        }
    }

    void domainInit() throws Exception {
        String serviceName = "grpc.brapp.com";
        ManagedChannel[] managedChannels = BrGrpcUtils.initChannels(serviceName);
        for (int i = 0; i < managedChannels.length; i++) {
            UserCenterGreeterGrpc.UserCenterGreeterBlockingStub blockingStub = (UserCenterGreeterGrpc.UserCenterGreeterBlockingStub)
                    BrGrpcUtils.newBlockStub(serviceName, managedChannels[i], UserCenterGreeterGrpc.class);
            //如果此处有多个服务初始化，可继续在循环中进行ping操作，因为channel资源针对域名是资源共享的
            blockingStub.ping(PingRequest.newBuilder().build());
        }
    }
}
