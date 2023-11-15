package com.br.marketing.service.Impl.dataProcess;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.marketingapi.input.UploadDataDTO;
import com.br.marketing.client.net.ApiCallerUtil;
import com.br.marketing.common.annoation.RetryMethod;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.net.ThirdApiResultTransfer;
import com.br.marketing.entity.PullCustomerFileData;
import com.br.marketing.mapper.InterfaceLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @Description UploadDataProxy
 * @Author hong.chen
 * @CreateTime 2023/11/14
 */
@Component
@Slf4j
public class UploadDataProxy extends DataProcessAbstractProxy {
    @Autowired
    RestTemplate restTemplate;

    @Qualifier("interfaceLogDbpool")
    @Autowired
    ThreadPoolExecutor interfaceLogDbpool;

    @Autowired
    InterfaceLogMapper interfaceLogMapper;

    @Override
    Object assembleData(List<PullCustomerFileData> customerFileDataList) {
        return null;
//        return subAssembleData(customerFileDataList);
    }

    @Override
    Object call(String apiCode, Object data, String url) {
        UploadDataDTO uploadDataDTO = new UploadDataDTO();
        uploadDataDTO.setApiCode(apiCode);
        // todo
        uploadDataDTO.setJsonData(data.toString());
        return callMarketingUpload(uploadDataDTO, null, url);
    }

    @RetryMethod(retryNowNum = 2, isOrNoDbRetry = true)
    public Result<Boolean> callMarketingUpload(UploadDataDTO dto, Integer retry, String url) {
        try {
            ThirdApiResultTransfer res = new ApiCallerUtil(restTemplate, interfaceLogMapper, interfaceLogDbpool)
                    .setUrl(url)
                    .setContentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .setRequestParam(dto)
                    .postTransferStr();
            if (Integer.valueOf(200).equals(res.getHttpCode())) {
                JSONObject jsonObject = JSON.parseObject(res.getResult());
                String code = jsonObject.getString("code");
                if (!"00".equals(code)) {
                    return new Result().setCode(ResultCode.FAIL.getValue());
                }
                return new Result().setCode(ResultCode.SUCCESS.getValue());
            } else {
                return new Result<>().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
            }
        } catch (Exception ex) {
            log.error("调用营销接口报错：" + ex.getMessage(), ex);
            return new Result<>().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
        }
    }

//    abstract Object subAssembleData(List<PullCustomerFileData> customerFileDataList);
}
