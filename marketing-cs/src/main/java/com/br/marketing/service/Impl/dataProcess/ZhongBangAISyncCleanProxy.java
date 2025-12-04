package com.br.marketing.service.Impl.dataProcess;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.marketingapi.input.UploadDataDTO;
import com.br.marketing.dto.MarketingPreUserDTO;
import com.br.marketing.dto.MarketingPreUserDetailDTO;
import com.br.marketing.entity.PullCustomerFileData;
import com.br.marketing.entity.dataProcess.DataProcessingConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @Description 众邦AI上传数据清洗
 * @Author zhen.Li1
 * @CreateTime 2025/11/15
 */
@Component
@Slf4j
public class ZhongBangAISyncCleanProxy extends UploadDataProxy {

    @Override
    Object subAssembleData(List<PullCustomerFileData> customerFileDataList, DataProcessingConfig config) {
        return handleAISyncByTaskId(customerFileDataList, config);
    }

    private List<UploadDataDTO> handleAISyncByTaskId(List<PullCustomerFileData> customerFileDataList, DataProcessingConfig config) {
        String apiCode = config.getApiCode();
        String fileHeader = config.getFileHeader();
        List<String> header = new ArrayList<>(Arrays.asList(fileHeader.split(",")));
        String dataSplit = config.getDataSplit();

        // 根据taskId进行数据分组
        Map<String, List<PullCustomerFileData>> taskIdGroupMap = new HashMap<>();
        for (PullCustomerFileData data : customerFileDataList) {
            try {
                List<String> dataList = new ArrayList<>(Arrays.asList(data.getFileData().split(Pattern.quote(dataSplit), -1)));
                String taskId = dataList.get(header.indexOf("taskId")).trim();
                taskIdGroupMap.computeIfAbsent(taskId, k -> new ArrayList<>()).add(data);
            } catch (Exception e) {
                log.error("众邦AI数据分组时解析taskId异常，数据表id：{}", data.getId(), e);
            }
        }
        // 对每个taskId分组分别处理
        List<UploadDataDTO> resultList = new ArrayList<>();

        for (Map.Entry<String, List<PullCustomerFileData>> entry : taskIdGroupMap.entrySet()) {
            String taskId = entry.getKey();
            List<PullCustomerFileData> groupData = entry.getValue();

            log.info("处理taskId: {}, 数据条数: {}", taskId, groupData.size());

            List<MarketingPreUserDetailDTO> syncUsers = new ArrayList<>();
            for (PullCustomerFileData data : groupData) {
                try {
                    asembleSingleData(header, dataSplit, syncUsers, data);
                } catch (Exception e) {
                    log.error("众邦AI上传数据清洗，客户数据处理异常，数据表id：{}", data.getId());
                }
            }
            if (!CollectionUtils.isEmpty(syncUsers)) {
                String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                String requestId = yyyyMMdd.concat("_").concat(apiCode).concat("_").concat(UUID.randomUUID().toString().substring(0, 5))
                        + System.currentTimeMillis();

                MarketingPreUserDTO marketingPreUserDTO = new MarketingPreUserDTO();
                // 使用实际的taskId
                marketingPreUserDTO.setTaskId(taskId);
                marketingPreUserDTO.setRequestId(requestId);
                marketingPreUserDTO.setDataItems(syncUsers);

                UploadDataDTO uploadDataDTO = new UploadDataDTO();
                uploadDataDTO.setApiCode(apiCode);
                uploadDataDTO.setJsonData(JSON.toJSONString(marketingPreUserDTO));
                resultList.add(uploadDataDTO);
            }
        }
        return resultList;
    }

    private void asembleSingleData(List<String> header, String dataSplit, List<MarketingPreUserDetailDTO> syncUsers, PullCustomerFileData data) {
        List<String> dataList = new ArrayList<>(Arrays.asList(data.getFileData().split(Pattern.quote(dataSplit), -1)));

        MarketingPreUserDetailDTO syncUser = new MarketingPreUserDetailDTO();
        JSONObject reserveField1 = new JSONObject();
        // dataItems
        syncUser.setCell(dataList.get(header.indexOf("cell")).trim());
        syncUser.setCustNum(dataList.get(header.indexOf("custNum")).trim());
        // reserveField1
        reserveField1.put("userType", dataList.get(header.indexOf("userType")).trim());

        String gender = dataList.get(header.indexOf("gender")).trim();
        reserveField1.put("gender", gender);

        String useTeble = dataList.get(header.indexOf("useTeble")).trim();
        reserveField1.put("useTeble", useTeble);

        String firstName = dataList.get(header.indexOf("firstName")).trim();
        reserveField1.put("firstName", firstName);

        String extend01 = dataList.get(header.indexOf("extend01")).trim();
        reserveField1.put("extend01", extend01);

        syncUser.setReserveField1(reserveField1.toJSONString());
        syncUsers.add(syncUser);
    }

}
