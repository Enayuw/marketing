package com.br.marketing.service.Impl.dataProcess;

import com.br.marketing.client.marketingapi.input.UploadDataDTO;
import com.br.marketing.client.marketingapi.input.UploadDataUrlDTO;
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
public abstract class UploadDataProxy extends DataProcessAbstractProxy {

    @Override
    Object assembleData(List<PullCustomerFileData> customerFileDataList, DataProcessingConfig config) {
        return subAssembleData(customerFileDataList, config);
    }

    @Override
    Object call(Object data, DataProcessingConfig config) {
        if (data instanceof UploadDataDTO) {
            UploadDataUrlDTO uploadDataUrlDTO = new UploadDataUrlDTO();
            UploadDataDTO uploadDataDTO;
            uploadDataDTO = (UploadDataDTO) data;
            uploadDataUrlDTO.setUrl(config.getUrl());
            uploadDataUrlDTO.setUploadDataDTO(uploadDataDTO);
            return marketingApiService.callUploadDataByUrlRetry(uploadDataUrlDTO, null);
        }
        return null;
    }

    abstract Object subAssembleData(List<PullCustomerFileData> customerFileDataList, DataProcessingConfig config);
}
