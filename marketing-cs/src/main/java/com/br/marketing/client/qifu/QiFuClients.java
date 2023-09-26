package com.br.marketing.client.qifu;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.client.HttpProxyClient;
import com.br.marketing.client.qifu.enums.CodeEnum;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

/**
 * 奇富科技（原360数科）客户端
 *
 * @author Guo Zeqiang
 * @dateTime 2023-09-20 14:23
 */
@Component
@Slf4j
public class QiFuClients {

    @Value("${api.qifu.saveReachDeleteRecordUrl:}")
    private String saveReachDeleteRecordUrl;

    /**
     * 客户公钥
     */
    @Value("${api.qifu.qifuPublicKey:}")
    private String qifuPublicKey;

    /**
     * 私钥
     */
    @Value("${api.qifu.brPrivateKey:}")
    private String brPrivateKey;

    /**
     * appId
     */
    @Value("${api.qifu.appId:bairong}")
    private String appId;

    @Value("${api.qifu.isProxy:true}")
    private boolean isProxy;

    @Resource
    private HttpProxyClient httpProxyClient;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    /**
     * 2023-09-21 19:17
     * 日志打印情况
     */
    private static final List<Boolean> IS_LOG_DEFAULT_LIST;
    private static final String IS_LOG_API_NAME_DELETE_RECORD = "agentOperationSaveReachDeleteRecord";
    private static final String CODE_KEY = "httpcode";
    private static final String CONTENT_KEY = "content";


    static {
        IS_LOG_DEFAULT_LIST = Arrays.asList(false, false);
    }


    /**
     * 2023-09-21 15:11
     * 保存触达删除记录接口
     *
     * @param saveReachDeleteRecordReq 触达删除记录
     * @return 接口响应信息 {@link ResponseData}、加密信息{@link ResultDataObj}及业务信息 {@link SaveReachDeleteRecordResp}
     */
    public Result<ResponseData<SaveReachDeleteRecordResp>> sendSaveReachDeleteRecordData(
            SaveReachDeleteRecordReq saveReachDeleteRecordReq) {
        Result<ResponseData<SaveReachDeleteRecordResp>> resultResp = new Result<>();
        try {
            Result<String> result = sendData(saveReachDeleteRecordReq, saveReachDeleteRecordUrl
                    , IS_LOG_API_NAME_DELETE_RECORD);
            if (ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                ResponseData<SaveReachDeleteRecordResp> responseData = JSON.parseObject(result.getData()
                        , new TypeReference<ResponseData<SaveReachDeleteRecordResp>>() {
                        });
                resultResp.setDate(responseData);
                switch (CodeEnum.valueof(responseData.getCode())) {
                    // 成功
                    case GWS100:
                        // 解密业务数据
                        responseData.decryptData(qifuPublicKey, brPrivateKey
                                , new TypeReference<SaveReachDeleteRecordResp>() {
                                });
                        resultResp.setCode(ResultCode.SUCCESS.getValue());
                        return resultResp;
                    // 重试
                    case GWS805:
                        resultResp.setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
                        return resultResp;
                    default:
                }
                resultResp.setCode(ResultCode.FAIL.getValue());
                return resultResp;
            }
            resultResp.setCode(result.getCode());
            resultResp.setMessage(result.getMessage());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            resultResp.setCode(ResultCode.FAIL.getValue());
            resultResp.setMessage(e.getMessage());
        }
        return resultResp;
    }


    /**
     * 2023-09-21 15:14
     * 发送数据
     *
     * @param bizData      业务数据
     * @param url          地址
     * @param isLogApiName 记录日志接口名称
     * @return 响应的字符串
     */
    public Result<String> sendData(BizData bizData, String url, String isLogApiName) {
        Result<String> result = new Result<>();
        RequestParam requestParam = new RequestParam(appId, bizData, qifuPublicKey, brPrivateKey);
        Map<String, List<Boolean>> apiLogMark = Objects.isNull(marketingCommonConfig.getApiLogMark())
                ? Collections.emptyMap() : marketingCommonConfig.getApiLogMark();
        List<Boolean> isLogs = apiLogMark.getOrDefault(isLogApiName, IS_LOG_DEFAULT_LIST);
        try {
            Map<String, String> httpResponseMap = httpProxyClient.sendByCodeWithLog(requestParam
                    , url
                    , isProxy
                    , MediaType.APPLICATION_JSON_UTF8_VALUE
                    , JSON.toJSONString(bizData)
                    , isLogs.get(0)
                    , isLogs.get(1));
            // httpcode不为200，需要重试
            if (String.valueOf(HttpStatus.SC_OK).equals(httpResponseMap.get(CODE_KEY))) {
                result.setDate(httpResponseMap.get(CONTENT_KEY));
                result.setCode(ResultCode.SUCCESS.getValue());
                result.setMessage("");
                if (isLogs.get(1)) {
                    log.warn("奇富保存触达删除记录接口-请求参数:{};业务数据:{};响应:{}"
                            , requestParam, bizData.toString(), httpResponseMap.get(CONTENT_KEY));
                }
                return result;
            }
            String errorMsg = String.format("奇富保存触达删除记录接口网络异常-请求参数:{%s};响应:{%s}"
                    , bizData.toString(), httpResponseMap.get(CONTENT_KEY));
            result.setMessage(errorMsg);
            if (isLogs.get(1)) {
                log.error(errorMsg);
            }
        } catch (Exception e) {
            String eMsg = "奇富保存触达删除记录接口异常:" + e.getMessage();
            log.error(eMsg, e);
            result.setMessage(eMsg);
        }
        result.setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
        return result;
    }
}
