package com.br.marketing.monitor;

/**
 * prometheus监控指标类
 *
 * @author zhen.Li
 * @dateTime 2023-05-18 13:51
 */
public class PrometheusMonitorUtils {


    /**
     * 统计上传接口ApiCode维度请求和
     */
    public static final String COUNT_UPLOAD_API_REQUEST_APICODE_METRIC_NAME = "countUploadApiRequestApiCodeMetricName";

    /**
     * 统计转化接口Cid维度请求和
     */
    public static final String COUNT_TRANSFER_API_REQUEST_CID_METRIC_NAME = "countTransferApiRequestCidMetricName";


    /**
     * 电销批量接口调用请求
     */

    public static final String COUNT_DAAS_BATCH_USERDATA_METRIC_NAME = "countDaasBatchUserDataMetricName";

    /**
     * 电销单条接口调用请求
     */
    public static final String COUNT_DAAS_SINGLE_USERDATA_METRIC_NAME = "countDaasSingleUserDataMetricName";

    /**
     * 电销黑名单接口调用请求
     */
    public static final String COUNT_DAAS_BLACK_DATA_METRIC_NAME = "countDaasBlackDataMetricName";

    /**
     * 电销转化接口调用请求
     */
    public static final String COUNT_DAAS_TRANSFER_METRIC_NAME = "countDaasTransferDataMetricName";

    /**
     * 电销Ibu接口调用请求
     */
    public static final String COUNT_DAAS_IBU_DATA_METRIC_NAME = "countDaasIbuDataMetricName";

    /**
     * 客服转化接口调用请求
     */
    public static final String COUNT_ROBOTAI_TRANSFER_METRIC_NAME = "countRobotAITransferDataMetricName";

    /**
     * 客服黑名单接口调用请求
     */
    public static final String COUNT_ROBOTAI_BLACK_METRIC_NAME = "countRobotAIBlackDataMetricName";

    /**
     * 决策接口调用请求
     */
    public static final String COUNT_POLICY_API_METRIC_NAME = "countPolicyAPIMetricName";

    /**
     * 跑分接口调用请求
     */
    public static final String COUNT_CORE_SCORE_API_METRIC_NAME = "countCoreScoreAPIMetricName";

    /**
     * 跑分接口线程使用情况
     */
    public static final String COUNT_CORE_SCORE_API_THREAD_METRIC_NAME = "countCoreScoreThreadMetricName";

    /**
     * 跑分接口重试线程使用情况
     */
    public static final String COUNT_RETRY_SCORE_API_THREAD_METRIC_NAME = "countRetryScoreThreadMetricName";

    /**
     * 查询ApiCode
     */
    public static final String CUSTOMER_APICODE_METRIC_NAME = "customerApiCodeMetricName";

    /**
     * 携程cps撞库量级统计
     */
    public static final String COUNT_XIECHENG_CPS_COLLIDING_DATA_METRIC_NAME = "countXieChengCpsCollidingDataMetricName";


    /**
     * 哈啰-https://open.hellobike.com/openapi-三方营销数据回传接口(
     */
    public static final String COUNT_HALO_CALLBACK_API_METRIC_NAME = "countHaloCallBackAPIMetricName";

}
