package com.br.marketing.service.Impl.dataProcess;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.marketingapi.input.UploadDataDTO;
import com.br.marketing.dto.TransferDataDTO;
import com.br.marketing.dto.TransferDataItemDTO;
import com.br.marketing.entity.PullCustomerFileData;
import com.br.marketing.entity.dataProcess.DataProcessingConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * @Description 亿联转化数据清洗
 * @Author zhen.Li1
 * @CreateTime 2024/01/17
 */
@Component
@Slf4j
public class YiLianTransferCleanProxy extends UploadDataProxy {


    @Override
    Object subAssembleData(List<PullCustomerFileData> customerFileDataList, DataProcessingConfig config) {
        String apiCode = config.getApiCode();
        String fileHeader = config.getFileHeader();
        List<String> header = new ArrayList<>(Arrays.asList(fileHeader.split(",")));
        String dataSplit = config.getDataSplit();

        List<TransferDataItemDTO> dataItems = new ArrayList<>();
        for (PullCustomerFileData data : customerFileDataList) {
            try {
                assembleData(header, dataSplit, dataItems, data);
            } catch (Exception e) {
                log.error("亿联转化数据清洗，客户数据处理异常，数据表id：{}", data.getId(), e.getMessage());
            }
        }

        if (CollectionUtils.isEmpty(dataItems)) {
            return null;
        }
        String requestId = apiCode.concat("_").concat(UUID.randomUUID().toString().substring(0, 5)) + System.currentTimeMillis();

        TransferDataDTO transferDataDTO = new TransferDataDTO();
        transferDataDTO.setDataItems(dataItems);
        transferDataDTO.setRequestId(requestId);

        UploadDataDTO uploadDataDTO = new UploadDataDTO();
        uploadDataDTO.setApiCode(apiCode);
        uploadDataDTO.setJsonData(JSON.toJSONString(transferDataDTO));
        return uploadDataDTO;

    }


    private void assembleData(List<String> header, String dataSplit, List<TransferDataItemDTO> dataItems,
                              PullCustomerFileData data) {
        List<String> dataList = new ArrayList<>(Arrays.asList(data.getFileData().split(Pattern.quote(dataSplit), -1)));
        TransferDataItemDTO transferData = new TransferDataItemDTO();
        JSONObject reserveField1 = new JSONObject();
        // dataItems
        // custNum 校验
        if (StringUtils.isEmpty(dataList.get(header.indexOf("br_uid")))) {
            log.error("亿联转化数据清洗custNum为空，数据表id={}", data.getId());
            return;
        }
        transferData.setCustNum(dataList.get(header.indexOf("br_uid")).trim());
        // loginTime

        transferData.setLoginTime(dataList.get(header.indexOf("login_time")).trim());
        // ifApply
        transferData.setIfApply(stringEscape(dataList.get(header.indexOf("apply_is")).trim()));
        // ifLent
        transferData.setIfLent(stringEscape(dataList.get(header.indexOf("cash_is")).trim()));
        // lentTime
        transferData.setLentTime(dataList.get(header.indexOf("cash_time")).trim());
        // lentAmount
        transferData.setLentAmount(dataList.get(header.indexOf("cash_amt")).trim());
        transferData.setIfRegister(stringEscape(dataList.get(header.indexOf("register_result")).trim()));
        transferData.setRegisterTime(dataList.get(header.indexOf("register_time")).trim());
        transferData.setIfLogin(stringEscape(dataList.get(header.indexOf("login_result")).trim()));
        transferData.setApplyResult(stringEscape(dataList.get(header.indexOf("apply_result")).trim()));
        transferData.setAuditAmount(dataList.get(header.indexOf("apply_amt")).trim());
        transferData.setAuditTime(dataList.get(header.indexOf("apply_time")).trim());
        // userType
        transferData.setUserType("1");
        // reserveField1
        reserveField1.put("idcardScanningResult", stringEscape(dataList.get(header.indexOf("idcard_scanning_result")).trim()));
        reserveField1.put("idcardScanningTime", timeFormatStr(dataList.get(header.indexOf("idcard_scanning_time")).trim()));
        reserveField1.put("faceRecognizationResultCredit", stringEscape(dataList.get(header.indexOf("face_recognization_result_credit")).trim()));
        reserveField1.put("faceRecognizationTimeCredit", timeFormatStr(dataList.get(header.indexOf("face_recognization_time_credit")).trim()));
        reserveField1.put("faceRecognizationResultDraw", stringEscape(dataList.get(header.indexOf("face_recognization_result_draw")).trim()));
        reserveField1.put("faceRecognizationTimeDraw", timeFormatStr(dataList.get(header.indexOf("face_recognization_time_draw")).trim()));
        reserveField1.put("cashResult", stringEscape(dataList.get(header.indexOf("cash_result")).trim()));
        reserveField1.put("dataDt", timeFormatStr(dataList.get(header.indexOf("data_dt")).trim()));
        reserveField1.put("etlunifyTime", dataList.get(header.indexOf("etlunify_time")).trim());

        transferData.setReserveField1(reserveField1.toJSONString());
        dataItems.add(transferData);
    }

    private String stringEscape(String value) {
        if ("是".equals(value) || "成功".equals(value)) {
            return "1";
        } else if ("否".equals(value) || "失败".equals(value)) {
            return "0";
        } else {
            return value;
        }
    }

    private String timeFormatStr(String time){
        if (StringUtils.isEmpty(time)) {
            return "";
        }
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        if (Pattern.matches("^\\d{4}-\\d{2}-\\d{2}$|^\\d{4}/\\d{1,2}/\\d{1,2}$", time)) {
            String s = time.replace("/", "-");
            try {
                return df.format(df.parse(s));
            } catch (ParseException e) {
                log.error("亿联转化清洗时间格式转换异常",e.getMessage());
            }
        }
        return time;

    }
}
