package com.br.marketing.client.zbank;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.entity.InterfaceLog;
import com.br.marketing.mapper.InterfaceLogMapper;
import com.zbank.file.bean.FileInfo;
import com.zbank.file.bean.StreamDownLoadInfo;
import com.zbank.file.exception.EmptyFileException;
import com.zbank.file.exception.SDKException;
import com.zbank.file.sdk.FileSDK;
import com.zbank.open.SDK;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
    @Value("${api.zbank.api.serviceId:CMBrLabelRatingRe}")
    private String serviceId;

    /**
     * 渠道唯一标识（由众邦银行提供）
     */
    @Value("${api.zbank.file.channelId:}")
    private String channelId;

    /**
     * 用于调用文件查询方法(queryFileList)时使用的slotKey
     */
    @Value("${api.zbank.file.slotKey:}")
    private String slotKey;

    @Resource
    private InterfaceLogMapper interfaceLogMapper;

    private final static ThreadPoolExecutor THREAD_POOL = BrExecutors.getThreadPool(5, 50, 100);


    /**
     * 2023-11-08 19:42
     * 标签评级
     */
    public String labelRatingRe(Object obj) throws Exception {
        return apiCall(obj, serviceId);
    }

    /**
     * 2023-11-08 19:42
     * 标签评级
     */
    public String labelRatingRe(Object obj, String requestId) throws Exception {
        return apiCall(obj, serviceId, requestId);
    }

    /**
     * 2023-11-08 19:42
     * api调用，记录接口日志
     */
    public String apiCall(Object obj, String serviceId, String requestId) throws Exception {
        String jsonString = apiCall(obj, serviceId);
        JSONObject localInterfaceLogContext = sdk.getLocalInterfaceLogContext();
        THREAD_POOL.execute(() -> {
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
}
