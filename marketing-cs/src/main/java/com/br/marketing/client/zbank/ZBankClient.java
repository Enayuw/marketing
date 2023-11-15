package com.br.marketing.client.zbank;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.DateHelper;
import com.br.marketing.entity.InterfaceLog;
import com.br.marketing.mapper.InterfaceLogMapper;
import com.br.marketing.service.SyncConfigService;
import com.zbank.file.bean.FileInfo;
import com.zbank.file.bean.StreamDownLoadInfo;
import com.zbank.file.common.utils.Md5EncodeUtil;
import com.zbank.file.exception.EmptyFileException;
import com.zbank.file.exception.SDKException;
import com.zbank.file.sdk.FileSDK;
import com.zbank.open.SDK;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 武汉众邦银行API接口服务调用
 *
 * @author Guo Zeqiang
 * @dateTime 2023-11-08 17:31
 */
@Component
@Slf4j
public class ZBankClient {
    @Resource
    private SDK sdk;

    @Resource
    private FileSDK fileSdk;

    /**
     * 业务接口
     */
    @Value("${api.zbank.baseUrl.api.serviceId:CMBrLabelRatingRe}")
    private String serviceId;

    /**
     * 渠道唯一标识（由众邦银行提供）
     */
    @Value("${api.zbank.baseUrl.file.channelId:2023042701}")
    private String channelId;

    /**
     * 用于调用文件查询方法(queryFileList)时使用的slotKey
     */
    @Value("${api.zbank.baseUrl.file.slotKey:rEFhYy7SRHzCrsMnzjqPoQ==}")
    private String slotKey;

    @Resource
    private SyncConfigService syncConfigService;

    @Resource
    private InterfaceLogMapper interfaceLogMapper;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(
            DateHelper.LINE_DATE_COLON_TIME_FORMAT);

    private final static ThreadPoolExecutor THREAD_POOL = BrExecutors.getThreadPool(1, 5, 5);


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
     * api调用
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
                l = fileSdk.queryFileList(channelId, name, slotKey, beginDate, endDate
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
            downLoadInfo = fileSdk.downloadStream(fileId, channelId, seqNo);
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
            log.error(e.getMessage(), e);
            throw e;
        } catch (EmptyFileException e) {
            System.out.println("此处应该是下载空文件的异常处理逻辑！！");
            log.error(e.getMessage(), e);
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
     * @return 一致返回true;
     */
    private boolean checkFileMd5(String resultMd5, String localMd5) {
        if (resultMd5.equals(localMd5)) {
            log.warn("md5一致");
            return true;
        }
        log.warn("md5不一致，resultMd5=" + resultMd5 + ", localMd5=" + localMd5);
        return false;
    }
}
