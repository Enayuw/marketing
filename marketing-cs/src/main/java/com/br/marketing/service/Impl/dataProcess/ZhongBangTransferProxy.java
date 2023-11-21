package com.br.marketing.service.Impl.dataProcess;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.marketingapi.input.UploadDataDTO;
import com.br.marketing.dto.TransferDataDTO;
import com.br.marketing.dto.TransferDataItemDTO;
import com.br.marketing.entity.LocalFile;
import com.br.marketing.entity.LocalFileExample;
import com.br.marketing.entity.PullCustomerFileData;
import com.br.marketing.entity.dataProcess.DataProcessingConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    public static final String ORIGINAL_CAIFU = "original_caifu_";
    public static final String ORIGINAL_DAIKUAN = "original_daikuan_";

    @Override
    public Boolean canStart(DataProcessingConfig config) {
        String apiCode = config.getApiCode();
        // 判断是否有待处理的上传数据文件，有则返回false
        LocalFileExample localFileExample = new LocalFileExample();
        LocalFileExample.Criteria criteria = localFileExample.createCriteria();
        criteria.andApiCodeEqualTo(apiCode).andPushStatusNotEqualTo("2").andFileNameLike(ORIGINAL_CAIFU + "%");

        LocalFileExample.Criteria orCriteria = localFileExample.createCriteria();
        orCriteria.andApiCodeEqualTo(apiCode).andPushStatusNotEqualTo("2").andFileNameLike(ORIGINAL_DAIKUAN + "%");

        localFileExample.or(orCriteria);
        List<LocalFile> localFiles = localFileMapper.selectByExample(localFileExample);
        if (localFiles.size() > 0) {
            log.warn("众邦数据清洗，上传数据文件还没有落库完成，转化数据文件需要等待");
            return false;
        }

        // 判断是否有尾量数据没落到明细表。判断条件：T-1日到T日，防止跨天传输判断有误
        String CreateTimeDate = LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String recordDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        int goingSize = marketingSyncInfoMapper.getUnresolvedCount(apiCode, CreateTimeDate, recordDate);
        if (goingSize > 0) {
            log.warn("众邦数据清洗，上传数据文件还有尾量数据没有落库完成，转化数据文件需要等待。上传数据待完成量级：{}", goingSize);
            return false;
        }

        return true;
    }

    @Override
    Object subAssembleData(List<PullCustomerFileData> customerFileDataList, DataProcessingConfig config) {
        String apiCode = config.getApiCode();
        String fileHeader = config.getFileHeader();
        List<String> header = new ArrayList<>(Arrays.asList(fileHeader.split(",")));
        String dataSplit = config.getDataSplit();

        List<TransferDataItemDTO> dataItems = new ArrayList<>();
        for (PullCustomerFileData data : customerFileDataList) {
            List<String> dataList = new ArrayList<>(Arrays.asList(data.getFileData().split(Pattern.quote(dataSplit), -1)));

            TransferDataItemDTO transferData = new TransferDataItemDTO();
            JSONObject reserveField1 = new JSONObject();

            // dataItems
            // custNum
            transferData.setCustNum(dataList.get(header.indexOf("custNum")).trim());
            // loginTime
            transferData.setLoginTime(dataList.get(header.indexOf("loginTime")));
            // ifApply
            transferData.setIfApply(dataList.get(header.indexOf("ifApply")));
            // applyTime
            transferData.setApplyTime(dataList.get(header.indexOf("applyTime")));
            // ifLent
            transferData.setIfLent(dataList.get(header.indexOf("ifLent1")));
            // lentTime
            transferData.setLentTime(dataList.get(header.indexOf("lentTime")));
            // lentAmount
            transferData.setLentAmount(dataList.get(header.indexOf("lentAmount")));
            // userType
            transferData.setUserType(dataList.get(header.indexOf("userType")));

            // reserveField1
            reserveField1.put("applyproductName", dataList.get(header.indexOf("applyproductName")));
            reserveField1.put("pushTime", dataList.get(header.indexOf("pushTime")));
            reserveField1.put("applyAmount", dataList.get(header.indexOf("applyAmount")));
            reserveField1.put("fileName", config.getLocalFile().getFileName());

            transferData.setReserveField1(reserveField1.toJSONString());
            dataItems.add(transferData);
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
}
