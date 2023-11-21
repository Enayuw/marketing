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
import java.util.regex.Pattern;

/**
 * @Description 众邦财富上传数据清洗-信贷
 * @Author hong.chen
 * @CreateTime 2023/11/17
 */
@Component
@Slf4j
public class ZhongBangSyncXinDaiProxy extends UploadDataProxy {
    @Override
    Object subAssembleData(List<PullCustomerFileData> customerFileDataList, DataProcessingConfig config) {
        String apiCode = config.getApiCode();
        String fileHeader = config.getFileHeader();
        List<String> header = new ArrayList<>(Arrays.asList(fileHeader.split(",")));
        String dataSplit = config.getDataSplit();

        List<MarketingPreUserDetailDTO> syncUsers = new ArrayList<>();
        for (PullCustomerFileData data : customerFileDataList) {
            List<String> dataList = new ArrayList<>(Arrays.asList(data.getFileData().split(Pattern.quote(dataSplit), -1)));

            MarketingPreUserDetailDTO syncUser = new MarketingPreUserDetailDTO();
            JSONObject reserveField1 = new JSONObject();

            // dataItems
            syncUser.setCell(dataList.get(header.indexOf("cell")));
            syncUser.setName(dataList.get(header.indexOf("name")));
            syncUser.setId(dataList.get(header.indexOf("id")));
            syncUser.setCustNum(dataList.get(header.indexOf("custNum")).trim());


            // reserveField1
            // original_daikuan_yyyymmdd对应2
            reserveField1.put("userType", "2");

            String gender = dataList.get(header.indexOf("gender"));
            if ("女".equals(gender)) {
                reserveField1.put("gender", "0");
            } else if ("男".equals(gender)) {
                reserveField1.put("gender", "1");
            } else {
                reserveField1.put("gender", "");
                log.error("众邦转化数据清洗,字段:gender,枚举非男女,id:{}", data.getId());
            }

            String ifRegister = dataList.get(header.indexOf("ifRegister"));
            reserveField1.put("ifRegister", ifRegister);

            String registerTime = dataList.get(header.indexOf("registerTime"));
            reserveField1.put("registerTime", registerTime);

            String ifLogin = dataList.get(header.indexOf("ifLogin"));
            reserveField1.put("ifLogin", ifLogin);

            String loginTime = dataList.get(header.indexOf("loginTime"));
            reserveField1.put("loginTime", loginTime);

            String ifApply = dataList.get(header.indexOf("ifApply"));
            reserveField1.put("ifApply", ifApply);

            String age = dataList.get(header.indexOf("age"));
            reserveField1.put("age", age);

            String region = dataList.get(header.indexOf("region"));
            reserveField1.put("region", region);

            syncUser.setReserveField1(reserveField1.toJSONString());
            syncUsers.add(syncUser);
        }

        MarketingPreUserDTO marketingPreUserDTO = new MarketingPreUserDTO();
        String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String taskId = yyyyMMdd.concat("_").concat(apiCode);
        String requestId = taskId.concat("_").concat(UUID.randomUUID().toString().substring(0, 5)) + System.currentTimeMillis();

        marketingPreUserDTO.setTaskId(taskId);
        marketingPreUserDTO.setRequestId(requestId);
        marketingPreUserDTO.setDataItems(syncUsers);
        UploadDataDTO uploadDataDTO = new UploadDataDTO();
        uploadDataDTO.setApiCode(apiCode);
        uploadDataDTO.setJsonData(JSON.toJSONString(marketingPreUserDTO));
        return uploadDataDTO;
    }
}
