package com.br.marketing.service.Impl.dataProcess;

import com.br.marketing.entity.PullCustomerFileData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Description ZhongBangUploadDataProxy
 * @Author hong.chen
 * @CreateTime 2023/11/15
 */
@Component
@Slf4j
public class ZhongBangSyncDataProxy extends UploadDataProxy {
    @Override
    Object subAssembleData(List<PullCustomerFileData> customerFileDataList) {
        System.out.println("bbbb");
        // todo filename区分usertype
//        List<MarketingPreUserDetailDTO> syncUsers = new ArrayList<>();
//
//
//
//        // todo
//        String apiCode = "7433800";
//        String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
//        String taskId = apiCode.concat("_").concat(yyyyMMdd);
//
//        MarketingPreUserDTO marketingPreUserDTO = new MarketingPreUserDTO();
//        marketingPreUserDTO.setTaskId(taskId);
////        marketingPreUserDTO.setTaskId(apiCode.concat("_").concat(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))));
//        marketingPreUserDTO.setRequestId(taskId.concat("_").concat(UUID.randomUUID().toString()));
//        marketingPreUserDTO.setDataItems(syncUsers);
//        UploadDataDTO uploadDataDTO = new UploadDataDTO();
//        uploadDataDTO.setApiCode(apiCode);
//        uploadDataDTO.setJsonData(JSON.toJSONString(marketingPreUserDTO));
        return null;
    }
}
