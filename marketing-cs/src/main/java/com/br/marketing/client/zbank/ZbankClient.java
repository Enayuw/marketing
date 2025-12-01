package com.br.marketing.client.zbank;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.br.cloud.web.MethodType;
import com.br.cloud.web.PrometheusTimeMethod;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.InterfaceLog;
import com.br.marketing.mapper.datasource.log.InterfaceLogMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.zbank.file.bean.FileDownLoadInfo;
import com.zbank.file.bean.FileInfo;
import com.zbank.file.bean.StreamDownLoadInfo;
import com.zbank.file.bean.UploadInfo;
import com.zbank.file.exception.EmptyFileException;
import com.zbank.file.exception.SDKException;
import com.zbank.file.sdk.FileSDK;
import com.zbank.open.SDK;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 众邦财富API接口服务调用
 *
 * @author Guo Zeqiang
 * @dateTime 2023-11-08 17:31
 */
@Component
@Slf4j
public class ZbankClient {
    @Resource
    private SDK sdk;

    @Resource
    private FileSDK fileSdk;

    /**
     * 业务接口
     */
    @Value("${api.zbank.api.serviceId.labelRating:CMBrLabelRatingRe}")
    private String serviceIdLabelRating;

    /**
     * 众邦财富评分回传接口
     */
    @Value("${api.zbank.api.serviceId.CMBrScoDaFeBack:CMBrScoDaFeBack}")
    private String CMBrScoDaFeBack;

    /**
     * 录音文件回传接口
     */
    @Value("${api.zbank.api.serviceId.recodFile:CMBrRecodFileRe}")
    private String serviceIdRecodFile;

    /**
     * 众邦AI回传接口
     */
    @Value("${api.zbank.api.serviceId.AICallBack:CMBrAIOCCallBack}")
    private String serviceIdAICallBack;

    /**
     * 渠道唯一标识（由众邦银行提供）
     */
    @Value("${api.zbank.file.channelId:2023042701}")
    private String channelId;

    /**
     * 用于调用文件查询方法(queryFileList)时使用的slotKey
     */
    @Value("${api.zbank.file.slotKey:rEFhYy7SRHzCrsMnzjqPoQ==}")
    private String slotKey;

    @Resource
    private InterfaceLogMapper interfaceLogMapper;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    private final static ThreadPoolExecutor THREAD_POOL = BrExecutors.getThreadPool(5, 50, 100);


    /**
     * 2023-11-08 19:42
     * 标签评级
     */

    public String labelRatingRe(Object obj) throws Exception {
        return apiCall(obj, serviceIdLabelRating);
    }

    /**
     * 2023-11-08 19:42
     * 标签评级
     */
    @PrometheusTimeMethod(buckets = {0.02d, 0.05d, 0.2d, 0.5d, 1d}, methodType = MethodType.REMOTE)
    public String labelRatingRe(Object obj, String requestId) throws Exception {
        return apiCall(obj, serviceIdLabelRating, requestId);
    }

    /**
     * 众邦信贷评分回传接口
     *
     * @param obj
     * @param requestId
     * @return
     * @throws Exception
     */
    @PrometheusTimeMethod(buckets = {0.02d, 0.05d, 0.2d, 0.5d, 1d}, methodType = MethodType.REMOTE)
    public String cMBrScoDaFeBack(Object obj, String requestId) throws Exception {
//        return "{\"msg\":\"服务调用异常:106100400008,请查证！\",\"result\":{},\"code\":\"106100720036\"}";
        return apiCall(obj, CMBrScoDaFeBack, requestId);
    }

    /**
     * 录音明细回调
     *
     * @param obj
     * @param requestId
     * @return
     * @throws Exception
     */
    @PrometheusTimeMethod(buckets = {0.02d, 0.05d, 0.2d, 0.5d, 1d}, methodType = MethodType.REMOTE)
    public String recodFileRe(Object obj, String requestId) throws Exception {
        return apiCall(obj, serviceIdRecodFile, requestId);
    }

    /**
     * 众邦AI定制化回调
     *
     * @param obj
     * @param requestId
     * @return
     * @throws Exception
     */
    @PrometheusTimeMethod(buckets = {0.02d, 0.05d, 0.2d, 0.5d, 1d}, methodType = MethodType.REMOTE)
    public String recodAIFileRe(Object obj, String requestId) throws Exception {
        return apiCall(obj, serviceIdAICallBack, requestId);
    }

    /**
     * 2023-11-08 19:42
     * api调用，记录接口日志
     */
    public String apiCall(Object obj, String serviceId, String requestId) throws Exception {
        String jsonString = apiCall(obj, serviceId);
        JSONObject localInterfaceLogContext = sdk.getLocalInterfaceLogContext();
        THREAD_POOL.execute(() -> {
            if (localInterfaceLogContext == null) {
                return;
            }
            try {
                InterfaceLog interfaceLog = localInterfaceLogContext.toJavaObject(InterfaceLog.class);
                interfaceLog.setRequestId(requestId);
                interfaceLog.setCreateTime(new Date());
                interfaceLogMapper.insertSelective(interfaceLog);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        });
        return jsonString;
    }

    /**
     * 2023-11-08 19:42
     * api调用
     */
    public String apiCall(Object obj, String serviceId) throws Exception {
        try {
            return sdk.invoke(JSON.toJSONString(obj), serviceId);
        } catch (Exception e) {
            if (e instanceof SDKException || e instanceof JSONException) {
                log.error(e.getMessage(), e);
                return "";
            }
            throw e;
        }
    }


    /**
     * 2023-11-08 19:42
     * 查询文件
     */
    public List<FileInfo> queryFileList(String fileName, String beginDate, String endDate, int pageNo) {
        List<FileInfo> l = new ArrayList<>();
        Map<String, String> infoMap = marketingCommonConfig.getZhongBangDownloadFileInfoMap();
        if (!CollectionUtils.isEmpty(infoMap)) {
            String channelIdKey = "channelId";
            if (infoMap.containsKey(channelIdKey)) {
                channelId = infoMap.get(channelIdKey);
            }
            infoMap.forEach((k, v) -> {
                if ("password".equals(k) || "encryptKey".equals(k)) {
                    return;
                }
                log.warn("众邦财富文件下载配置信息:{}={}", k, v);
            });
        }
        String fileSlotKey = marketingCommonConfig.getZhongBangDownloadFileSlotKey();
        if (StringUtils.isNotBlank(fileSlotKey)) {
            slotKey = fileSlotKey;
        }
        try {
            String seqNo = "" + System.nanoTime() + RandomStringUtils.randomNumeric(4);
            l.addAll(fileSdk.queryFileList(channelId, fileName, slotKey, beginDate, endDate
                    , pageNo == 0 ? -1 : pageNo, seqNo));
        } catch (SDKException e) {
            log.error(e.getMessage(), e);
        }
        return l;
    }

    /**
     * 2023-11-10 9:34
     * 获取服务端的输出流，将流写入到本地磁盘（当然也可以写入到其他任何位置，比如网络）
     */
    public StreamDownLoadInfo downloadWholeFile(FileInfo fileInfo) {
        // 接口请求唯一流水号 需要保证每笔请求流水号唯一，便于交易日志定位
        String seqNo = "" + System.nanoTime() + RandomStringUtils.randomNumeric(3);
        // 使用SDK成功上传后返回的fileId 或者是通过之前的SDK调用FileService接口上传到影像平台返回的fileId或url；
        // 注意：使用影像平台的fileId或url下载后的文件 无文件名
        try {
            return fileSdk.downloadStream(fileInfo.getFileId(), channelId, seqNo);
        } catch (EmptyFileException | SDKException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 2023-12-01 9:34
     * 将文件下载到本地磁盘指定的目录。
     * 此下载方式内部已进行了文件Md5值校验，无需重复校验
     */
    public FileDownLoadInfo downLoadSplitFileMergeInLocal(FileInfo fileInfo, String dir) {
        updateChannelId();
        String seqNo = "" + System.nanoTime() + RandomStringUtils.randomNumeric(3);
        try {
            return fileSdk.downloadFile(fileInfo.getFileId(), channelId, dir, seqNo, false, true);
        } catch (EmptyFileException | SDKException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 2023-12-01 9:34
     * 将文件上传至服务器
     */
    public UploadInfo uploadFile(File file) throws SDKException {
        updateChannelId();
        String seqNo = "" + System.nanoTime() + "_" + RandomStringUtils.randomNumeric(3);
        return fileSdk.upload(file, channelId, seqNo, true);
    }

    /**
     * 2023-12-01 9:34
     * 将文件下载到本地磁盘指定的目录。
     * 此下载方式内部已进行了文件Md5值校验，无需重复校验
     * 将文件上传至服务器
     */
    public UploadInfo uploadInputStream(InputStream inputStream, String fileName, long fileSize, String fileMd5) throws SDKException {
        updateChannelId();
        String seqNo = "" + System.nanoTime() + "_" + RandomStringUtils.randomNumeric(3);
        return fileSdk.upload(inputStream, fileMd5, fileName, fileSize, channelId, seqNo, true, true);
    }

    /**
     * 2024-05-08 17:00
     * 更新配置ChannelId
     */
    private void updateChannelId() {
        Map<String, String> infoMap = marketingCommonConfig.getZhongBangDownloadFileInfoMap();
        String channelIdKey = "channelId";
        if (!CollectionUtils.isEmpty(infoMap) && infoMap.containsKey(channelIdKey)) {
            channelId = infoMap.get(channelIdKey);
        }
    }
}
