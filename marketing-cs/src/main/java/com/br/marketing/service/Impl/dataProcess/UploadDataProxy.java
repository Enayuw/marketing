package com.br.marketing.service.Impl.dataProcess;

import com.br.marketing.client.marketingapi.input.UploadDataDTO;
import com.br.marketing.client.marketingapi.input.UploadDataUrlDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.entity.PullCustomerFileData;
import com.br.marketing.entity.dataProcess.DataProcessingConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Description 接口数据代理类
 * @Author hong.chen
 * @CreateTime 2023/11/14
 */
@Component
@Slf4j
public abstract class UploadDataProxy extends DataProcessAbstractProxy {
    abstract Object subAssembleData(List<PullCustomerFileData> customerFileDataList, DataProcessingConfig config);

    @Override
    Object assembleData(List<PullCustomerFileData> customerFileDataList, DataProcessingConfig config) {
        return subAssembleData(customerFileDataList, config);
    }

    @Override
    Object call(Object data, DataProcessingConfig config, AtomicInteger errorMark) {
        Result callResult = new Result().success();
        if (data instanceof UploadDataDTO) {
            // 处理单个UploadDataDTO
            UploadDataDTO uploadDataDTO = (UploadDataDTO) data;
            UploadDataUrlDTO uploadDataUrlDTO = new UploadDataUrlDTO();
            uploadDataUrlDTO.setUrl(config.getUrl());
            uploadDataUrlDTO.setUploadDataDTO(uploadDataDTO);
            Result result = marketingApiService.callUploadDataByUrlRetry(uploadDataUrlDTO, null);
            if (!ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                log.error("单条上传数据失败，apiCode: {}, 错误信息: {}",
                        uploadDataDTO.getApiCode(), result.getMessage());
                errorMark.getAndIncrement();
                callResult.setCode(ResultCode.FAIL.getValue());
            }
        } else if (data instanceof List) {
            List<UploadDataDTO> uploadDataDTOList = (List<UploadDataDTO>) data;
            for (UploadDataDTO uploadDataDTO : uploadDataDTOList) {
                UploadDataUrlDTO uploadDataUrlDTO = new UploadDataUrlDTO();
                uploadDataUrlDTO.setUrl(config.getUrl());
                uploadDataUrlDTO.setUploadDataDTO(uploadDataDTO);
                Result result = marketingApiService.callUploadDataByUrlRetry(uploadDataUrlDTO, null);
                if (!ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                    errorMark.getAndIncrement();
                    log.error("批量上传数据失败，apiCode: {}, 错误信息: {}",
                            uploadDataDTO.getApiCode(), result.getMessage());
                    callResult.setCode(ResultCode.FAIL.getValue());
                }
            }
        }
        return callResult;
    }
}
