package com.br.marketing.service.Impl.dataProcess;

import com.br.marketing.client.marketingapi.input.UploadDataDTO;
import com.br.marketing.client.marketingapi.input.UploadDataUrlDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.entity.PullCustomerFileData;
import com.br.marketing.entity.dataProcess.DataProcessingConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Description UploadDataProxy
 * @Author hong.chen
 * @CreateTime 2023/11/14
 */
@Component
@Slf4j
public class UploadDataProxy extends DataProcessAbstractProxy{

    @Override
    Object assembleData(List<PullCustomerFileData> customerFileDataList, DataProcessingConfig config, String fileName) {
        return customerFileDataList;
//        return subAssembleData(customerFileDataList,config,fileName);
//        return "{\"code\":1,\"message\":\"成功\",\"data\":null}";
    }

    @Override
    Object call(String apiCode, Object data, String url) {
        UploadDataUrlDTO uploadDataUrlDTO = new UploadDataUrlDTO();
        uploadDataUrlDTO.setUrl(url);
        UploadDataDTO uploadDataDTO = new UploadDataDTO();
        if (data instanceof UploadDataDTO) {
            uploadDataDTO = (UploadDataDTO) data;
        }
        uploadDataUrlDTO.setUrl(url);
        uploadDataUrlDTO.setUploadDataDTO(uploadDataDTO);
        Result result = marketingApiService.callUploadDataByUrlRetry(uploadDataUrlDTO,null);
//        Result<Boolean> booleanResult = callMarketingUpload(uploadDataDTO, null, url);
        return result;
//        return null;
    }

//    @RetryMethod(retryNowNum = 2, isOrNoDbRetry = true)
//    private Result<Boolean> callMarketingUpload(UploadDataDTO dto, Integer retry, String url) {
//        try {
//            ThirdApiResultTransfer res = new ApiCallerUtil(getRestTemplate(), getInterfaceLogMapper(), getInterfaceLogDbpool())
//                    .setUrl(url)
//                    .setContentType(MediaType.APPLICATION_FORM_URLENCODED)
//                    .setRequestParam(dto)
//                    .postTransferStr();
//            if (Integer.valueOf(200).equals(res.getHttpCode())) {
//                JSONObject jsonObject = JSON.parseObject(res.getResult());
//                String code = jsonObject.getString("code");
//                if (!"00".equals(code)) {
//                    return new Result().setCode(ResultCode.FAIL.getValue());
//                }
//                return new Result().setCode(ResultCode.SUCCESS.getValue());
//            } else {
//                return new Result<>().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
//            }
//        } catch (Exception ex) {
//            log.error("调用营销接口报错：" + ex.getMessage(), ex);
//            return new Result<>().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
//        }
//    }

//    abstract Object subAssembleData(List<PullCustomerFileData> customerFileDataList, DataProcessingConfig config, String fileName);
}
