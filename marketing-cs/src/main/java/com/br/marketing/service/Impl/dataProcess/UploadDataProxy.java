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
    Object call(Object data, DataProcessingConfig config , AtomicInteger errorMark) {
        if (data instanceof UploadDataDTO) {
            UploadDataDTO uploadDataDTO = (UploadDataDTO) data;
            UploadDataUrlDTO uploadDataUrlDTO = new UploadDataUrlDTO();
            uploadDataUrlDTO.setUrl(config.getUrl());
            uploadDataUrlDTO.setUploadDataDTO(uploadDataDTO);
            Result result =marketingApiService.callUploadDataByUrlRetry(uploadDataUrlDTO, null);
            if (!ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                errorMark.getAndIncrement();
            }
            return result;
        }

        return null;
    }
}
