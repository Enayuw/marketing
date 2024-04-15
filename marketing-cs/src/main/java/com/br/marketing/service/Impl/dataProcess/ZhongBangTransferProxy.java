package com.br.marketing.service.Impl.dataProcess;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.marketingapi.input.UploadDataDTO;
import com.br.marketing.dto.TransferDataDTO;
import com.br.marketing.dto.TransferDataItemDTO;
import com.br.marketing.entity.PullCustomerFileData;
import com.br.marketing.entity.dataProcess.DataProcessingConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * @Description 众邦财富转化数据清洗
 * @Author hong.chen
 * @CreateTime 2023/11/17
 */
@Component
@Slf4j
public class ZhongBangTransferProxy extends UploadDataProxy {
    @Override
    Object subAssembleData(List<PullCustomerFileData> customerFileDataList, DataProcessingConfig config) {
        String apiCode = config.getApiCode();
        String fileHeader = config.getFileHeader();
        List<String> header = new ArrayList<>(Arrays.asList(fileHeader.split(",")));
        String dataSplit = config.getDataSplit();

        List<TransferDataItemDTO> dataItems = new ArrayList<>();
        for (PullCustomerFileData data : customerFileDataList) {
            try {
                single(config, header, dataSplit, dataItems, data);
            } catch (Exception e) {
                log.error("众邦转化数据清洗，客户数据处理异常，数据表id：{}", data.getId());
            }
        }

        if (CollectionUtils.isEmpty(dataItems)) {
            return null;
        }

        String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String taskId = yyyyMMdd.concat("_").concat(apiCode);
        String requestId = taskId.concat("_").concat(UUID.randomUUID().toString().substring(0, 5)) + System.currentTimeMillis();

        TransferDataDTO transferDataDTO = new TransferDataDTO();
        transferDataDTO.setDataItems(dataItems);
        transferDataDTO.setRequestId(requestId);

        UploadDataDTO uploadDataDTO = new UploadDataDTO();
        uploadDataDTO.setApiCode(apiCode);
        uploadDataDTO.setJsonData(JSON.toJSONString(transferDataDTO));
        return uploadDataDTO;
    }

    private void single(DataProcessingConfig config, List<String> header, String dataSplit, List<TransferDataItemDTO> dataItems,
                                  PullCustomerFileData data) {
        List<String> dataList = new ArrayList<>(Arrays.asList(data.getFileData().split(Pattern.quote(dataSplit), -1)));

        TransferDataItemDTO transferData = new TransferDataItemDTO();
        JSONObject reserveField1 = new JSONObject();

        // dataItems
        // custNum
        transferData.setCustNum(dataList.get(header.indexOf("custNum")).trim());
        // loginTime
        transferData.setLoginTime(dataList.get(header.indexOf("loginTime")).trim());
        // ifApply
        transferData.setIfApply(dataList.get(header.indexOf("ifApply")).trim());
        // applyTime
        transferData.setApplyTime(dataList.get(header.indexOf("applyTime")).trim());
        // ifLent
        transferData.setIfLent(dataList.get(header.indexOf("ifLent1")).trim());
        // lentTime
        transferData.setLentTime(dataList.get(header.indexOf("lentTime")).trim());
        // lentAmount
        transferData.setLentAmount(dataList.get(header.indexOf("lentAmount")).trim());
        // userType
        transferData.setUserType(dataList.get(header.indexOf("userType")).trim());

        // reserveField1
        reserveField1.put("applyproductName", dataList.get(header.indexOf("applyproductName")).trim());
        reserveField1.put("pushTime", dataList.get(header.indexOf("pushTime")).trim());
        reserveField1.put("applyAmount", dataList.get(header.indexOf("applyAmount")).trim());
        reserveField1.put("fileName", config.getLocalFile().getFileName());

        transferData.setReserveField1(reserveField1.toJSONString());
        dataItems.add(transferData);
    }
}
