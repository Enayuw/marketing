package com.br.marketing.client.zbank;

import com.alibaba.fastjson.JSON;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.service.SyncConfigService;
import com.zbank.file.bean.FileInfo;
import com.zbank.file.bean.StreamDownLoadInfo;
import com.zbank.file.common.http.config.HttpConfig;
import com.zbank.file.common.utils.Md5EncodeUtil;
import com.zbank.file.exception.EmptyFileException;
import com.zbank.file.exception.SDKException;
import com.zbank.file.sdk.FileSDK;
import com.zbank.file.secure.SM2AESPackSecure;
import com.zbank.open.SDK;
import com.zbank.open.common.Config;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 武汉众邦银行API接口服务调用
 *
 * @author Guo Zeqiang
 * @dateTime 2023-11-08 17:31
 */
@Configuration
@Slf4j
public class ZBankClient {

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
     * 渠道唯一标识（由众邦银行提供）
     */
    @Value("${api.zbank.baseUrl.file.channelId:2023042701}")
    private String channelId;
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
     * 用于调用文件查询方法(queryFileList)时使用的slotKey
     */
    @Value("${api.zbank.baseUrl.file.slotKey:rEFhYy7SRHzCrsMnzjqPoQ==}")
    private String slotKey;


    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DateHelper.LINE_DATE_COLON_TIME_FORMAT);

    @Resource
    private SyncConfigService syncConfigService;

    /**
     * 2023-11-08 19:18
     * 武汉众邦银行API接口服务调用
     */
    @Bean
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


    /**
     * 2023-11-08 19:42
     * 标签评级
     */
    public String labelRatingRe(Object obj) {
        return apiCall(obj, serviceId);
    }

    /**
     * 2023-11-08 19:42
     * api调用
     */
    public String apiCall(Object obj, String serviceId) {
        try {
            return zBankClientApiSdk().invoke(JSON.toJSONString(obj), serviceId);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }


    /**
     * 2023-11-08 19:42
     * 查询文件
     */
    public List<FileInfo> queryFileList(Set<String> fileNameSet, int pageNo) {
        String beginDate = LocalDate.now().atStartOfDay().format(DATE_TIME_FORMATTER);
        String endDate = LocalDate.now().atTime(23, 59, 59, 999999)
                .atZone(ZoneId.systemDefault()).format(DATE_TIME_FORMATTER);
        List<FileInfo> list = new ArrayList<>();
        for (String name : fileNameSet) {
            List<FileInfo> l;
            try {
                String seqNo = UUID.randomUUID().toString().replace("-", "").toLowerCase(Locale.ROOT)
                        + System.nanoTime();
                l = zBankClientFileSdk().queryFileList(channelId, name, slotKey, beginDate, endDate
                        , pageNo == 0 ? -1 : pageNo, seqNo);
                list.addAll(l);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }

        }
        return list;
    }

    /**
     * 2023-11-10 9:34
     * 获取服务端的输出流，将流写入到本地磁盘（当然也可以写入到其他任何位置，比如网络）
     */
    public boolean downloadWholeFile(String fileId, String apiCode) throws SDKException, IOException {
        // 接口请求唯一流水号 需要保证每笔请求流水号唯一，便于交易日志定位
        String seqNo = UUID.randomUUID().toString();
        // 使用SDK成功上传后返回的fileId 或者是通过之前的SDK调用FileService接口上传到影像平台返回的fileId或url；
        // 注意：使用影像平台的fileId或url下载后的文件 无文件名
        StreamDownLoadInfo downLoadInfo = null;
        FileOutputStream ou = null;
        try {
            downLoadInfo = zBankClientFileSdk().downloadStream(fileId, channelId, seqNo);
            File f = new File(syncConfigService.getPullCustomerFilePath(apiCode)
                    .concat(LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE))
                    .concat(File.separator).concat(downLoadInfo.getFileName()));
            ou = new FileOutputStream(f);
            byte[] bytes = new byte[1024 * 1024];
            int n;
            while ((n = downLoadInfo.read(bytes)) != -1) {
                ou.write(bytes, 0, n);
                ou.flush();
            }
            // 校验文件Md5
            return checkFileMd5(downLoadInfo.getFileMd5(), Md5EncodeUtil.encode(f));
        } catch (SDKException e) {
            throw e;
        } catch (EmptyFileException e) {
            System.out.println("此处应该是下载空文件的异常处理逻辑！！");
        } finally {
            if (downLoadInfo != null) {
                downLoadInfo.releaseResouces();
            }
            if (ou != null) {
                ou.close();
            }
        }
        return false;
    }

    /**
     * 比较文件md5
     *
     * @param resultMd5 服务端响应信息中的md5值
     * @param localMd5  本地下载后的文件生成的md5值
     * @return
     */
    public static boolean checkFileMd5(String resultMd5, String localMd5) {
        if (resultMd5.equals(localMd5)) {
            log.info("md5一致");
            return true;
        }
        log.info("md5不一致，resultMd5=" + resultMd5 + ", localMd5=" + localMd5);
        return false;
    }


    public static class CallEffect {
        private Request request;

        public CallEffect(Request request) {
            this.request = request;
        }

        public CallEffect() {
        }

        public Request getRequest() {
            return request;
        }

        public void setRequest(Request request) {
            this.request = request;
        }

        @Override
        public String toString() {
            return "CallEffect{" +
                    "request=" + request +
                    '}';
        }
    }

    public static class Request {
        /**
         * 2023-11-09 9:42
         * 各渠道由互联网开放平台提供
         */
        private String appID;
        /**
         * 2023-11-09 9:42
         * 交易流水号
         */
        private String TxnSrlNo;
        /**
         * 2023-11-09 9:42
         * 测试环境日期根据后端跑批变化，生产环境为自然日期
         */
        private String TxnDt;
        /**
         * 2023-11-09 9:42
         * 测试环境时间根据后端跑批变化，生产环境为自然时间
         */
        private String TxnTs;
        /**
         * 2023-11-09 9:42
         */
        private String UseCrdtApplySrlNo;
        /**
         * 2023-11-09 9:42
         */
        private String IdentTp;
        /**
         * 2023-11-09 9:42
         */
        private String IdentNo;
        /**
         * 2023-11-09 9:42
         */
        private String CnlInd;
        /**
         * 2023-11-09 9:42
         * 通过调用开放平台 Java SDK 获取
         */
        private String appAccessToken;

        public Request(String appID, String txnSrlNo, String txnDt, String txnTs, String useCrdtApplySrlNo, String identTp, String identNo, String cnlInd, String appAccessToken) {
            this.appID = appID;
            this.TxnSrlNo = txnSrlNo;
            this.TxnDt = txnDt;
            this.TxnTs = txnTs;
            this.UseCrdtApplySrlNo = useCrdtApplySrlNo;
            this.IdentTp = identTp;
            this.IdentNo = identNo;
            this.CnlInd = cnlInd;
            this.appAccessToken = appAccessToken;
        }

        public Request() {
        }

        public String getAppID() {
            return appID;
        }

        public void setAppID(String appID) {
            this.appID = appID;
        }

        public String getTxnSrlNo() {
            return TxnSrlNo;
        }

        public void setTxnSrlNo(String txnSrlNo) {
            TxnSrlNo = txnSrlNo;
        }

        public String getTxnDt() {
            return TxnDt;
        }

        public void setTxnDt(String txnDt) {
            TxnDt = txnDt;
        }

        public String getTxnTs() {
            return TxnTs;
        }

        public void setTxnTs(String txnTs) {
            TxnTs = txnTs;
        }

        public String getUseCrdtApplySrlNo() {
            return UseCrdtApplySrlNo;
        }

        public void setUseCrdtApplySrlNo(String useCrdtApplySrlNo) {
            UseCrdtApplySrlNo = useCrdtApplySrlNo;
        }

        public String getIdentTp() {
            return IdentTp;
        }

        public void setIdentTp(String identTp) {
            IdentTp = identTp;
        }

        public String getIdentNo() {
            return IdentNo;
        }

        public void setIdentNo(String identNo) {
            IdentNo = identNo;
        }

        public String getCnlInd() {
            return CnlInd;
        }

        public void setCnlInd(String cnlInd) {
            CnlInd = cnlInd;
        }

        public String getAppAccessToken() {
            return appAccessToken;
        }

        public void setAppAccessToken(String appAccessToken) {
            this.appAccessToken = appAccessToken;
        }

        @Override
        public String toString() {
            return "Request{" +
                    "appID='" + appID + '\'' +
                    ", TxnSrlNo='" + TxnSrlNo + '\'' +
                    ", TxnDt='" + TxnDt + '\'' +
                    ", TxnTs='" + TxnTs + '\'' +
                    ", UseCrdtApplySrlNo='" + UseCrdtApplySrlNo + '\'' +
                    ", IdentTp='" + IdentTp + '\'' +
                    ", IdentNo='" + IdentNo + '\'' +
                    ", CnlInd='" + CnlInd + '\'' +
                    ", appAccessToken='" + appAccessToken + '\'' +
                    '}';
        }
    }
}
