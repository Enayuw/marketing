package com.br.marketing.monitor;

/**
 * prometheus监控指标类
 *
 * @author zhen.Li
 * @dateTime 2023-05-18 13:51
 */
public class PrometheusMonitorUtils {


    /**
     * 上传接口
     */
    public static final String UPLOAD_API_KEY = "upload-api";

    /**
     * 转化接口
     */
    public static final String TRANSFER_API_KEY = "transfer-api";

    /**
     * 统计上传接口ApiCode维度请求和
     */
    public static final String COUNT_UPLOAD_API_REQUEST_APICODE_METRIC_NAME = "countUploadApiRequestApiCodeMetricName";

    /**
     * 统计转化接口Cid维度请求和
     */
    public static final String COUNT_TRANSFER_API_REQUEST_CID_METRIC_NAME = "countTransferApiRequestCidMetricName";


}
