package com.br.marketing.service.Impl.dataProcess;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.marketingapi.input.UploadDataDTO;
import com.br.marketing.dto.MarketingPreUserDTO;
import com.br.marketing.dto.MarketingPreUserDetailDTO;
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

/**
 * @Description ZhongBangUploadDataProxy
 * @Author hong.chen
 * @CreateTime 2023/11/15
 */
@Component
@Slf4j
public class ZhongBangSyncDataProxy extends UploadDataProxy {

    @Override
    public Boolean canStart(DataProcessingConfig config) {
        String apiCode =  marketingCommonConfig.getZhongBangDataProcessApiCode();
        boolean isTransferFile = config.getLocalFile().getFileName().startsWith("transform_");
        if (isTransferFile) {
            // 判断是否有下载中的上传数据文件，有则返回false
            LocalFileExample localFileExample = new LocalFileExample();
            LocalFileExample.Criteria criteria = localFileExample.createCriteria();
            criteria.andApiCodeEqualTo(apiCode).andStatusEqualTo("1").andFileNameLike("original_caifu_%");

            LocalFileExample.Criteria orCriteria = localFileExample.createCriteria();
            orCriteria.andApiCodeEqualTo(apiCode).andStatusEqualTo("1").andFileNameLike("original_daikuan_%");

            localFileExample.or(orCriteria);
            List<LocalFile> localFiles = localFileMapper.selectByExample(localFileExample);
            if (localFiles.size() > 0) {
                return false;
            }

            // T-1日到T日，防止跨天传输判断有误
            String CreateTimeDate = LocalDateTime.now().minusDays(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String recordDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            int goingSize = marketingSyncInfoMapper.getUnresolvedCount(apiCode, CreateTimeDate, recordDate);
            if (goingSize > 0) {
                log.warn("众邦数据清洗，上传数据文件还没有落库完成，转化数据文件需要等待，待完成量级：{}", goingSize);
                return false;
            }
        }
        return true;
    }

    @Override
    Object subAssembleData(List<PullCustomerFileData> customerFileDataList, DataProcessingConfig config) {
        boolean caifu = config.getLocalFile().getFileName().startsWith("original_caifu_");
        boolean daikuan = config.getLocalFile().getFileName().startsWith("original_daikuan_");
        boolean transform = config.getLocalFile().getFileName().startsWith("transform_");
        // 上传文件-财富
        if (caifu) {
            return handleCaifu(customerFileDataList, config);
        }
        // 上传文件-信贷
        if (daikuan) {
            return handleDaiKuan(customerFileDataList, config);
        }
        // 转化文件
        if (transform) {
            return handleTransform(customerFileDataList, config);
        }

        return null;

    }

    private void handleYesOrNo(JSONObject reserveField1, String fieldName, String value) {
        if ("是".equals(value)) {
            reserveField1.put(fieldName, 1);
        } else if ("否".equals(value)) {
            reserveField1.put(fieldName, 0);
        } else {
            reserveField1.put(fieldName, "");
            // todo 是否需要报警
            log.error("众邦转化数据清洗,字段:{},枚举非是否", fieldName);
        }
    }

    private UploadDataDTO handleCaifu(List<PullCustomerFileData> customerFileDataList, DataProcessingConfig config) {
        String fileHeader = config.getFileHeader();
        List<String> header = new ArrayList<>(Arrays.asList(fileHeader.split(",")));
        String dataSplit = config.getDataSplit();

        List<MarketingPreUserDetailDTO> syncUsers = new ArrayList<>();
        for (PullCustomerFileData data : customerFileDataList) {
            List<String> dataList = new ArrayList<>(Arrays.asList(data.getFileData().split(dataSplit, -1)));

            MarketingPreUserDetailDTO syncUser = new MarketingPreUserDetailDTO();
            JSONObject reserveField1 = new JSONObject();

            // dataItems
            syncUser.setCell(dataList.get(header.indexOf("cell")));
            syncUser.setName(dataList.get(header.indexOf("name")));
            syncUser.setId(dataList.get(header.indexOf("id")));
            syncUser.setCustNum(dataList.get(header.indexOf("custNum")));


            // reserveField1
            // original_caifu_yyyymmdd对应1
            reserveField1.put("userType", 1);

            String gender = dataList.get(header.indexOf("gender"));
            if ("女".equals(gender)) {
                reserveField1.put("gender", 0);
            } else if ("男".equals(gender)) {
                reserveField1.put("gender", 1);
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

            String applyResult = dataList.get(header.indexOf("applyResult"));
            reserveField1.put("applyResult", applyResult);

            String ifLent = dataList.get(header.indexOf("ifLent"));
            reserveField1.put("ifLent", ifLent);

            String age = dataList.get(header.indexOf("age"));
            reserveField1.put("age", age);

            String region = dataList.get(header.indexOf("region"));
            reserveField1.put("region", region);

            String productStartTime = dataList.get(header.indexOf("productStartTime"));
            reserveField1.put("productStartTime", productStartTime);

            String productEndTime = dataList.get(header.indexOf("productEndTime"));
            reserveField1.put("productEndTime", productEndTime);

            String ifApplyAmount = dataList.get(header.indexOf("ifApplyAmount"));
            reserveField1.put("ifApplyAmount", ifApplyAmount);

            syncUser.setReserveField1(reserveField1.toJSONString());
            syncUsers.add(syncUser);
        }

        String taskId = getTaskId();
        // 说明和示例不一致，apicode后面缺_
        String requestId = getRequestId(getTaskId());

        MarketingPreUserDTO marketingPreUserDTO = new MarketingPreUserDTO();
        marketingPreUserDTO.setTaskId(taskId);
//        marketingPreUserDTO.setTaskId(apiCode.concat("_").concat(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))));
//        marketingPreUserDTO.setRequestId(taskId.concat("_").concat(UUID.randomUUID().toString()));
        marketingPreUserDTO.setRequestId(requestId);
        marketingPreUserDTO.setDataItems(syncUsers);
        UploadDataDTO uploadDataDTO = new UploadDataDTO();
        String apiCode =  marketingCommonConfig.getZhongBangDataProcessApiCode();
        uploadDataDTO.setApiCode(apiCode);
        uploadDataDTO.setJsonData(JSON.toJSONString(marketingPreUserDTO));
        return uploadDataDTO;
    }

    private UploadDataDTO handleDaiKuan(List<PullCustomerFileData> customerFileDataList, DataProcessingConfig config) {
        String fileHeader = config.getFileHeader();
        List<String> header = new ArrayList<>(Arrays.asList(fileHeader.split(",")));
        String dataSplit = config.getDataSplit();

        List<MarketingPreUserDetailDTO> syncUsers = new ArrayList<>();
        for (PullCustomerFileData data : customerFileDataList) {
            List<String> dataList = new ArrayList<>(Arrays.asList(data.getFileData().split(dataSplit, -1)));

            MarketingPreUserDetailDTO syncUser = new MarketingPreUserDetailDTO();
            JSONObject reserveField1 = new JSONObject();

            // dataItems
            syncUser.setCell(dataList.get(header.indexOf("cell")));
            syncUser.setName(dataList.get(header.indexOf("name")));
            syncUser.setId(dataList.get(header.indexOf("id")));
            syncUser.setCustNum(dataList.get(header.indexOf("custNum")));


            // reserveField1
            // original_caifu_yyyymmdd对应1
            reserveField1.put("userType", 1);

            String gender = dataList.get(header.indexOf("gender"));
            if ("女".equals(gender)) {
                reserveField1.put("gender", 0);
            } else if ("男".equals(gender)) {
                reserveField1.put("gender", 1);
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

//            String applyResult = dataList.get(header.indexOf("applyResult"));
//            reserveField1.put("applyResult", applyResult);

            String ifLent = dataList.get(header.indexOf("ifLent"));
            reserveField1.put("ifLent", ifLent);

            String age = dataList.get(header.indexOf("age"));
            reserveField1.put("age", age);

            String region = dataList.get(header.indexOf("region"));
            reserveField1.put("region", region);

//            String productStartTime = dataList.get(header.indexOf("productStartTime"));
//            reserveField1.put("productStartTime", productStartTime);

//            String productEndTime = dataList.get(header.indexOf("productEndTime"));
//            reserveField1.put("productEndTime", productEndTime);
//
//            String ifApplyAmount = dataList.get(header.indexOf("ifApplyAmount"));
//            reserveField1.put("ifApplyAmount", ifApplyAmount);

            syncUser.setReserveField1(reserveField1.toJSONString());
            syncUsers.add(syncUser);
        }


        MarketingPreUserDTO marketingPreUserDTO = new MarketingPreUserDTO();
        String taskId = getTaskId();
        marketingPreUserDTO.setTaskId(taskId);
//        marketingPreUserDTO.setTaskId(apiCode.concat("_").concat(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))));
//        marketingPreUserDTO.setRequestId(taskId.concat("_").concat(UUID.randomUUID().toString()));
        marketingPreUserDTO.setRequestId(getRequestId(taskId));
        marketingPreUserDTO.setDataItems(syncUsers);
        UploadDataDTO uploadDataDTO = new UploadDataDTO();
        String apiCode =  marketingCommonConfig.getZhongBangDataProcessApiCode();
        uploadDataDTO.setApiCode(apiCode);
        uploadDataDTO.setJsonData(JSON.toJSONString(marketingPreUserDTO));
        return uploadDataDTO;
    }

    private UploadDataDTO handleTransform(List<PullCustomerFileData> customerFileDataList, DataProcessingConfig config) {
        String fileHeader = config.getFileHeader();
        List<String> header = new ArrayList<>(Arrays.asList(fileHeader.split(",")));
        String dataSplit = config.getDataSplit();

        List<TransferDataItemDTO> dataItems = new ArrayList<>();
        for (PullCustomerFileData data : customerFileDataList) {
            List<String> dataList = new ArrayList<>(Arrays.asList(data.getFileData().split(dataSplit, -1)));

            TransferDataItemDTO transferData = new TransferDataItemDTO();
            JSONObject reserveField1 = new JSONObject();

            // dataItems
            // custNum
            // userType
            // ifLogin
            // ifApply
            // applyTime
            // ifLent
            // lentTime

            // reserveField1

            transferData.setReserveField1(reserveField1.toJSONString());
            dataItems.add(transferData);
        }

        TransferDataDTO transferDataDTO = new TransferDataDTO();
        transferDataDTO.setDataItems(dataItems);
        transferDataDTO.setRequestId(getRequestId(getTaskId()));

        UploadDataDTO uploadDataDTO = new UploadDataDTO();
        String apiCode =  marketingCommonConfig.getZhongBangDataProcessApiCode();
        uploadDataDTO.setApiCode(apiCode);
        uploadDataDTO.setJsonData(JSON.toJSONString(transferDataDTO));
        return uploadDataDTO;
    }

    private String getTaskId() {
        String apiCode =  marketingCommonConfig.getZhongBangDataProcessApiCode();
        String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String taskId = yyyyMMdd.concat("_").concat(apiCode);
        return taskId;
    }

    private String getRequestId(String taskId) {
        // 说明和示例不一致，apicode后面缺_
        return taskId.concat("_").concat(UUID.randomUUID().toString().substring(0, 5)) + System.currentTimeMillis();
    }



}
