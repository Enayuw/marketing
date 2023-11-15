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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * @Description ZhongBangUploadDataProxy
 * @Author hong.chen
 * @CreateTime 2023/11/15
 */
@Component
@Slf4j
public class ZhongBangSyncDataProxy extends UploadDataProxy {
    @Override
    Object subAssembleData(List<PullCustomerFileData> customerFileDataList, DataProcessingConfig config, String fileName) {
        String fileHeader = config.getFileHeader();
        List<String> header = new ArrayList<>(Arrays.asList(fileHeader.split(",")));
//        String[] header = fileHeader.split(",");
//        header
        String dataSplit = config.getDataSplit();

        List<MarketingPreUserDetailDTO> syncUsers = new ArrayList<>();
        for (PullCustomerFileData data : customerFileDataList) {
            List<String> dataList = new ArrayList<>(Arrays.asList(data.getFileData().split(dataSplit, -1)));

            MarketingPreUserDetailDTO syncUser = new MarketingPreUserDetailDTO();
            JSONObject reserveField1 = new JSONObject();
            syncUser.setReserveField1(reserveField1.toJSONString());

            // dataItems
            syncUser.setCell(dataList.get(header.indexOf("cell")));
            syncUser.setName(dataList.get(header.indexOf("name")));
            syncUser.setId(dataList.get(header.indexOf("id")));
            syncUser.setCustNum(dataList.get(header.indexOf("custNum")));


            // reserveField1
            // todo filename区分usertype，但是客户给了userType
            String userType = dataList.get(header.indexOf("userType"));
            reserveField1.put("userType", userType);

            String gender = dataList.get(header.indexOf("gender"));
            if ("女".equals(gender)) {
                reserveField1.put("gender", 0);
            } else if ("男".equals(gender)) {
                reserveField1.put("gender", 1);
            } else {
                reserveField1.put("gender", "");
            }

            String ifRegister = dataList.get(header.indexOf("ifRegister"));
            handleYesOrNo(reserveField1, "ifRegister", ifRegister);

            String registerTime = dataList.get(header.indexOf("registerTime"));
            reserveField1.put("registerTime", registerTime);

            String ifLogin = dataList.get(header.indexOf("ifLogin"));
            handleYesOrNo(reserveField1, "ifLogin", ifLogin);

            String loginTime = dataList.get(header.indexOf("loginTime"));
            reserveField1.put("loginTime", loginTime);

            String ifApply = dataList.get(header.indexOf("ifApply"));
            handleYesOrNo(reserveField1, "ifApply", ifApply);

            String applyResult = dataList.get(header.indexOf("applyResult"));
            handleYesOrNo(reserveField1, "applyResult", applyResult);

            String ifLent = dataList.get(header.indexOf("ifLent"));
            handleYesOrNo(reserveField1, "ifLent", ifLent);

            String age = dataList.get(header.indexOf("age"));
            reserveField1.put("age", age);

            String region = dataList.get(header.indexOf("region"));
            reserveField1.put("region", region);

            String productStartTime = dataList.get(header.indexOf("productStartTime"));
            reserveField1.put("productStartTime", productStartTime);

            String productEndTime = dataList.get(header.indexOf("productEndTime"));
            reserveField1.put("productEndTime", productEndTime);

            String ifApplyAmount = dataList.get(header.indexOf("ifApplyAmount"));
            handleYesOrNo(reserveField1, "ifApplyAmount", ifApplyAmount);

            syncUsers.add(syncUser);
        }

        // todo 改为配置
        String apiCode = "7433800";
        String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String taskId = yyyyMMdd.concat("_").concat(apiCode);
        // todo 说明和示例不一致，apicode后面缺_
        String requestId = taskId.concat("_").concat(UUID.randomUUID().toString().substring(0, 5)) + System.currentTimeMillis();

        MarketingPreUserDTO marketingPreUserDTO = new MarketingPreUserDTO();
        marketingPreUserDTO.setTaskId(taskId);
//        marketingPreUserDTO.setTaskId(apiCode.concat("_").concat(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))));
//        marketingPreUserDTO.setRequestId(taskId.concat("_").concat(UUID.randomUUID().toString()));
        marketingPreUserDTO.setRequestId(requestId);
        marketingPreUserDTO.setDataItems(syncUsers);
        UploadDataDTO uploadDataDTO = new UploadDataDTO();
        uploadDataDTO.setApiCode(apiCode);
        uploadDataDTO.setJsonData(JSON.toJSONString(marketingPreUserDTO));
        return uploadDataDTO;
    }

    private void handleYesOrNo(JSONObject reserveField1, String fieldName, String value) {
        if ("是".equals(value)) {
            reserveField1.put(fieldName, 1);
        } else if ("否".equals(value)) {
            reserveField1.put(fieldName, 0);
        } else {
            reserveField1.put(fieldName, "");
            // todo 是否需要报警
            log.error("众邦转化数据清洗,字段:{},枚举非是否");
        }
    }
}
