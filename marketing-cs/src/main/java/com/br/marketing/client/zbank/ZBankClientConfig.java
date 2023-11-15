package com.br.marketing.client.zbank;

import com.zbank.file.common.http.config.HttpConfig;
import com.zbank.file.sdk.FileSDK;
import com.zbank.file.secure.SM2AESPackSecure;
import com.zbank.open.SDK;
import com.zbank.open.common.Config;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

/**
 * 武汉众邦银行API接口服务sdk配置
 *
 * @author Guo Zeqiang
 * @dateTime 2023-11-08 17:31
 */
@Configuration
@Slf4j
public class ZBankClientConfig {

    @Value("${otherConfig.proxy.proxy_host:00}")
    private String proxyHost;
    @Value("${otherConfig.proxy.proxy_port:00}")
    private int proxyPort;
    @Value("${otherConfig.proxy.proxy_username:00}")
    private String userName;
    @Value("${otherConfig.proxy.proxy_password:00}")
    private String password;

    /**
     * 连接建立超时时间，单位ms
     */
    private static final int CONN_TIMEOUT = 30000;
    /**
     * 请求响应超时时间，单位ms
     */
    private static final int SOCKET_TIMEOUT = 50000;

    /**
     * 文件请求响应超时时间，单位ms
     */
    private static final int FILE_SOCKET_TIMEOUT = 60000;

    // api sdk参数
    /**
     * 访问URL（由众邦银行提供）
     */
    @Value("${api.zbank.isPorxy:true}")
    private Boolean isPorxy;

    /**
     * 访问URL（由众邦银行提供）
     */
    @Value("${api.zbank.baseUrl:https://iodev-uat.z-bank.com}")
    private String url;


    /**
     * 开放平台开放平台公钥（由众邦银行提供）
     */
    @Value("${api.zbank.baseUrl.api.serverPubKey:049191E0402CE98C8F31564880AC47AC888DACB24B127407D351AD83725CDB4529713E585CBB14C14E4EBBE97828D64B1F2DC101E113F227B6E6ACD9A378311DC3}")
    private String serverPubKey;

    /**
     * appId（由众邦银行提供）
     */
    @Value("${api.zbank.baseUrl.api.appId:2a0f9f71_29e5_466c_95a7_8cab99d93880}")
    private String appId;

    /**
     * appSecretKey（由众邦银行提供）
     */
    @Value("${api.zbank.baseUrl.api.appSecretKey:65ed7e3b-bcff-4f5b-a029-185a538e3ee8}")
    private String appSecretKey;

    /**
     * 渠道自己的私钥字符串，生成方式和提取方式请参照【证书的生成及提取】目录下的文档说明，另，证书生成完成之后将【server.crt】文件提供给众邦银行
     */
    @Value("${api.zbank.baseUrl.api.priKey:1DF4C616DE52063F5BB9525121160DF2F0607122A5FE69EB382D57020B27EA6A}")
    private String priKey;

    /**
     * 业务接口
     */
    @Value("${api.zbank.baseUrl.api.serviceId:CMBrLabelRatingRe}")
    private String serviceId;

    // 文件sdk参数

    /**
     * 用于加密的密钥（由众邦银行提供），行外渠道加密使用
     */
    @Value("${api.zbank.baseUrl.file.encryptKey:0463455a993b27010c80ceaca36f8faddcc5bb942b242faad8196ccda08d9ba556a669d6682d62d5278dbdc7a65d87ea8071635825725c35b92607eb379b369949}")
    private String encryptKey;
    /**
     * 用于加密的校验和字符串（由众邦银行提供），行外渠道加密使用
     */
    @Value("${api.zbank.baseUrl.file.cksStr:YRPZSSUEDOXHGNBYYYDGWPDASZJIIHXMJBZZFZTOSSHWABKGBHSBTUSJAMDFHIRX}")
    private String cksStr;


    /**
     * 2023-11-08 19:18
     * 武汉众邦银行API接口服务调用
     */
    @Bean
    @Lazy
    public SDK zBankClientApiSdk() {
        // SDK对象不需要每次在接口调用时创建和初始化，只需要初始化一次即可。SDK对象的个数与商户申请的appid个数有关， 即如果申请了两个appid就创建两个sdk对象
        SDK sdk = new SDK();
        try {
            //SDK初始化
            sdk.init(appId, appSecretKey, priKey, serverPubKey, url, CONN_TIMEOUT, SOCKET_TIMEOUT);
            if (isPorxy) {
                Config config = sdk.getConfig();
                // 设置代理的host
                config.setProxyHost(proxyHost);
                // 设置代理的port
                config.setProxyPort(proxyPort);
                config.setProxyUsername(userName);
                config.setProxyPassword(password);
            }
            // 开启记录接口日志
            sdk.getConfig().getInterfaceLogServiceIdList().add(serviceId);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return sdk;
    }

    /**
     * 2023-11-08 19:18
     * 武汉众邦银行File文件服务调用
     */
    @Bean
    @Lazy
    public FileSDK zBankClientFileSdk() {
        //1、初始化配置服务方接口url。
        FileSDK sdk = FileSDK.build(url);
        //2、配置httpClient相关参数：连接超时时间、响应超时时间、http代理、SS5代理等。详见HttpConfig类
        HttpConfig config = new HttpConfig();
        config.setSocketTimeout(FILE_SOCKET_TIMEOUT);
        sdk.config(config);
        //配置加解密参数
        sdk.config(new SM2AESPackSecure(encryptKey, cksStr));
        return sdk;
    }
}
