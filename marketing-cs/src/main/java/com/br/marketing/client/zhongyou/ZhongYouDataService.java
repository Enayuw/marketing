package com.br.marketing.client.zhongyou;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.marketingapi.input.PushTransferDataDetailDTO;
import com.br.marketing.client.marketingapi.input.UploadDataDTO;
import com.br.marketing.common.annoation.RetryMethod;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.rocketmq.MarketingAssistConstants;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.config.RocketMqSwitch;
import com.br.marketing.dto.MarketingPreUserDTO;
import com.br.marketing.dto.MarketingPreUserDetailDTO;
import com.br.marketing.dto.TransferDataDTO;
import com.br.marketing.dto.TransferDataItemDTO;
import com.br.marketing.entity.LocalFile;
import com.br.marketing.entity.ZhongyouDataCountDTO;
import com.br.marketing.entity.ZhongyouFileData;
import com.br.marketing.mapper.LocalFileMapper;
import com.br.marketing.mapper.ZhongyouFileDataMapper;
import com.br.marketing.rabbitmq.RabbitMqProducter;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.service.PushInfoService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.ThreadPoolAdjustmentUtil;
import com.br.rocketmq.rocketmq.template.RocketMqTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;

import static com.br.marketing.common.utils.MQConstants.ROUTING_KEY_MARKETING_ZHONGYOU_DATA_CLEAN;

/**
 * 描述：： 中邮数据处理接口
 * <p>
 * ------------------------------------
 *
 * @program: marketing
 * @ClassName ZhongYouDataService
 * @author: it-yml
 * @create: 2023-08-03 15:10
 * @Version 1.0
 * --------------------------------------
 **/
@Service
@Slf4j
public class ZhongYouDataService {
    public static final String ZHONGYOUOUTMARKETING = "zhongyououtmarketing";
    @Resource
    private ZhongYouClient zhongYouClient;


    @Resource
    private LocalFileMapper localFileMapper;

    @Resource
    private ZhongyouFileDataMapper zhongyouFileDataMapper;

    @Resource
    private RabbitMqProducter producter;
    @Resource
    private RocketMqTemplate template;
    @Resource
    private RocketMqSwitch rocketMqSwitch;

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    @Resource
    TableCreateServiceImpl tableCreateService;

    @Resource
    ZhongYouClientData zhongYouClientData;

    @Resource
    private PushInfoService pushInfoService;

    @Resource
    @Qualifier("zhongYouCleanThreadPool")
    ThreadPoolExecutor zhongYouCleanThreadPool;

    @RetryMethod(retryNowNum = 2)
    public Result<List<Long>> saveFileNameList(LocalDate date) {
        // 拉取数据
        String fileDate = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        HashMap<String, String> stringStringHashMap =
                zhongYouClient.sendByCodeWithLog(
                        zhongYouClientData.fileNameListData(fileDate),
                        zhongYouClientData.getQueryUrl(),
                        zhongYouClientData.getIsProxy(),
                        false,
                        null);
        if (!"200".equals(stringStringHashMap.get("httpcode"))) {
            log.error("中邮文件列表接口httpcode非200异常，重试");
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
        }
        if(StringUtils.isEmpty(stringStringHashMap.get("content"))){
            log.error("中邮获取文件列表responseData数据为空");
            return new Result<List<Long>>().setCode(ResultCode.FAIL.getValue());
        }
        List<Long> ids = saveFile(stringStringHashMap.get("content"));
        return new Result<List<Long>>().setCode(ResultCode.SUCCESS.getValue()).setDate(ids);
    }

    @RetryMethod(retryNowNum = 2)
    public Result saveFileData(Long fileId) {
        LocalFile zhongyouFile = localFileMapper.selectByPrimaryKey(fileId);
        HashMap<String, String> stringStringHashMap =
                zhongYouClient.sendByCodeWithLog(
                        zhongYouClientData.fileDownLoadData(zhongyouFile.getFileName()),
                        zhongYouClientData.getDownloadUrl(),
                        zhongYouClientData.getIsProxy(),
                        true,
                        fileId);
        if (!"200".equals(stringStringHashMap.get("httpcode"))) {
            log.error("中邮文件内容接口httpcode非200异常");
            return new Result().setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
        }
        sendZhongyouDataMq(fileId);
        return new Result<List<Long>>().setCode(ResultCode.SUCCESS.getValue());
    }

    /**
     * 发送mq
     *
     * @param fileId 文件id
     */
    private void sendZhongyouDataMq(Long fileId) {
        List<ZhongyouDataCountDTO> zhongyouDataCountDTOList = zhongyouFileDataMapper.selectZhongyouCount(fileId);
        if (zhongyouDataCountDTOList.size() == 2) {
            String fileData = zhongyouDataCountDTOList.get(0).getFileData();
            Integer num = zhongyouDataCountDTOList.get(1).getNum();
            if (!Integer.valueOf(fileData).equals(num)) {
                log.error("中邮文件数据量级不匹配：文件给定量级-> {},实际入库量级-> {}", fileData, num);
            }
            if(rocketMqSwitch.rocketMQSwitchFlag(null, MarketingAssistConstants.TAG_MARKETING_ZHONGYOU_DATA_CLEAN)){
                rocketMqSwitch.syncSend(MarketingAssistConstants.TOPIC
                        , MarketingAssistConstants.TAG_MARKETING_ZHONGYOU_DATA_CLEAN, String.valueOf(fileId));
            }else{
                producter.send(ROUTING_KEY_MARKETING_ZHONGYOU_DATA_CLEAN, String.valueOf(fileId));
            }
        }else {
            log.error("中邮文件内容数据异常 fileId ：{}",fileId);
        }

    }

    private List<Long> saveFile(String content) {
        JSONObject jsonData = JSONObject.parseObject(content);
        JSONArray fileNames = jsonData.getJSONArray("fileNames");
        if (fileNames.isEmpty()) {
            log.error("中邮文件查询列表为空");
        }
        List<Long> ids = new ArrayList<>();
        for (int i = 0; i < fileNames.size(); i++) {
            String fileName = JSONObject.parseObject(fileNames.getString(i)).getString("fileName");
            LocalFile zhongyouFile = zhongyouFileBuild(fileName);
            if (localFileMapper.insertSelective(zhongyouFile) > 0) {
                ids.add(zhongyouFile.getId());
            }
        }
        return ids;
    }

    /**
     * 中邮文件实体创建
     *
     * @param fileName 文件名称
     * @return ZhongYouFile 实体
     */
    private  LocalFile zhongyouFileBuild(String fileName) {
        LocalFile zhongyouFile = new LocalFile();
        zhongyouFile.setFileName(fileName);
        zhongyouFile.setCid(tableCreateService.getCId(marketingCommonConfig.getZhongyouApiCode()));
        zhongyouFile.setApiCode(marketingCommonConfig.getZhongyouApiCode());
        zhongyouFile.setFileType(ZHONGYOUOUTMARKETING);
        zhongyouFile.setStatus("1");
        zhongyouFile.setCreateTime(new Date());
        zhongyouFile.setUpdateTime(new Date());
        return zhongyouFile;
    }

    /**
     * 中邮文件数据清洗到上传转化
     * @param id
     * @return
     */
    public Result<Boolean> HandleZhongYouData(Long id) {
        try {
            Long st1 = System.currentTimeMillis();
            LocalFile localFile = localFileMapper.selectByPrimaryKey(id);
            String fileName = localFile.getFileName();
            List<String> strategyIdList = zhongyouFileDataMapper.selectZhongYoustrategyIds(id);
            //根据策略ID分组查询
            strategyIdList.forEach((String strategyId) -> {
                Long minId = null;
                Boolean isContiue = Boolean.TRUE;
                while (isContiue) {
                    if (marketingCommonConfig.getZhongYouCleanDataThreadNum() != null) {
                        ThreadPoolAdjustmentUtil.adjustThreadPoolSize(zhongYouCleanThreadPool, marketingCommonConfig.getZhongYouCleanDataThreadNum());
                        log.warn("中邮清洗数据线程调整，taskId={},corePoolSize={},maxPoolSize={}", strategyId,
                                zhongYouCleanThreadPool.getCorePoolSize(), zhongYouCleanThreadPool.getMaximumPoolSize());
                    }
                    List<ZhongyouFileData> zhongyouFileDataList = zhongyouFileDataMapper.selectZhongYouDataPage(id, minId, strategyId);
                    if (zhongyouFileDataList.size() <= 0) {
                        isContiue = Boolean.FALSE;
                        continue;
                    }
                    minId = zhongyouFileDataList.get(zhongyouFileDataList.size() - 1).getId() + 1;
                    zhongYouCleanThreadPool.submit(() -> {
                        try {
                            Result result = cleanData(zhongyouFileDataList, fileName);
                            if (!ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                                log.warn(result.getMessage());
                            }
                        } catch (Exception ex) {
                            log.error("中邮数据清洗异常", ex);
                        }
                    });
                }
            });
            log.warn("中邮清洗数据耗时：{} ms", System.currentTimeMillis() - st1);
        }catch (Exception e){
            log.error("中邮清洗数据异常：{}",e.getMessage());
        }

        return new Result().setCode(ResultCode.SUCCESS.getValue()).setDate(false).setMessage("成功");
    }
    private Result cleanData(List<ZhongyouFileData> zhongyouFileDataList, String fileName) {
        String apiCode = zhongyouFileDataList.get(0).getApiCode();
        MarketingPreUserDTO marketingPreUserDTO = new MarketingPreUserDTO();
        TransferDataDTO transferDataDTO = new TransferDataDTO();
        //构造上传,转化参数
        buildParam(apiCode, zhongyouFileDataList, marketingPreUserDTO, transferDataDTO, fileName);
        //上传接口
        UploadDataDTO uploadDataDTO = new UploadDataDTO();
        uploadDataDTO.setApiCode(apiCode);
        uploadDataDTO.setJsonData(JSON.toJSONString(marketingPreUserDTO));
        pushInfoService.pushUploadByRetry(uploadDataDTO, null);
        PushTransferDataDetailDTO dto = new PushTransferDataDetailDTO();
        dto.setApiCode(apiCode);
        dto.setJsonData(JSON.toJSONString(transferDataDTO));
        pushInfoService.pushTransferByRetry(dto, null);
        return new Result().setCode(ResultCode.SUCCESS.getValue()).setMessage("成功");
    }
    private void buildParam(String apiCode, List<ZhongyouFileData> zhongyouFileDataList, MarketingPreUserDTO uploadDataDTO,
                            TransferDataDTO transferDataDTO, String fileName) {
        List<MarketingPreUserDetailDTO> dataItems = new ArrayList<>();
        List<TransferDataItemDTO> transferDataItemDTOS = new ArrayList<>();
        zhongyouFileDataList.forEach((ZhongyouFileData zhongyouFileData) -> {
            List<String> list = new ArrayList<>(Arrays.asList(zhongyouFileData.getFileData().split("\\|\\|", -1)));
            MarketingPreUserDetailDTO detailDTO = new MarketingPreUserDetailDTO();
            TransferDataItemDTO transferDataItemDTO = new TransferDataItemDTO();
            JSONObject uploadJsonObject = new JSONObject();
            JSONObject transferJsonObject = new JSONObject();
            //同一批taskId一样
            uploadDataDTO.setTaskId(list.get(0));
            transferJsonObject.put("taskId", list.get(0));
            detailDTO.setCell(list.get(4));
            transferJsonObject.put("cell", list.get(4));
            detailDTO.setCustNum(list.get(3));
            transferDataItemDTO.setCustNum(list.get(3));
            uploadJsonObject.put("firstName", list.get(5));
            transferJsonObject.put("firstName", list.get(5));
            uploadJsonObject.put("userType", "00");
            transferDataItemDTO.setUserType("00");
            String gender = list.get(6);
            if ("女".equals(gender)) {
                uploadJsonObject.put("gender", 0);
                transferJsonObject.put("gender", 0);
            } else if ("男".equals(gender)) {
                uploadJsonObject.put("gender", 1);
                transferJsonObject.put("gender", 1);
            } else {
                uploadJsonObject.put("gender", "");
                transferJsonObject.put("gender", "");
            }
            uploadJsonObject.put("customName", list.get(1));
            transferDataItemDTO.setCustomName(list.get(1));
            uploadJsonObject.put("registerTime", list.get(7));
            transferDataItemDTO.setRegisterTime(list.get(7));
            uploadJsonObject.put("ifLogin", list.get(19));
            transferDataItemDTO.setIfLogin(list.get(19));
            if (org.apache.commons.lang3.StringUtils.isEmpty(list.get(8)) || org.apache.commons.lang3.StringUtils.isEmpty(list.get(20))) {
                uploadJsonObject.put("loginTime", "");
            } else {
                uploadJsonObject.put("loginTime", org.apache.commons.lang3.StringUtils.isNotEmpty(list.get(8)) ? list.get(8) : list.get(20));
                transferDataItemDTO.setLoginTime(org.apache.commons.lang3.StringUtils.isNotEmpty(list.get(8)) ? list.get(8) : list.get(20));
            }
            uploadJsonObject.put("ifApply", list.get(21));
            transferDataItemDTO.setIfApply(list.get(21));
            uploadJsonObject.put("applyDt", list.get(22));
            transferDataItemDTO.setApplyDt(list.get(22));
            uploadJsonObject.put("applyResult", list.get(23));
            transferDataItemDTO.setApplyResult(list.get(23));
            if (org.apache.commons.lang3.StringUtils.isEmpty(list.get(10)) || org.apache.commons.lang3.StringUtils.isEmpty(list.get(24))) {
                uploadJsonObject.put("auditTime", "");
            } else {
                uploadJsonObject.put("auditTime", org.apache.commons.lang3.StringUtils.isNotEmpty(list.get(10)) ? list.get(10) : list.get(24));
                transferDataItemDTO.setAuditTime(org.apache.commons.lang3.StringUtils.isNotEmpty(list.get(10)) ? list.get(10) : list.get(24));
            }
            uploadJsonObject.put("auditAmount", list.get(11));
            transferDataItemDTO.setAuditAmount(list.get(11));
            uploadJsonObject.put("ifLent", list.get(26));
            transferDataItemDTO.setIfLent(list.get(26));
            uploadJsonObject.put("lentTime", list.get(29));
            transferDataItemDTO.setLentTime(list.get(29));
            uploadJsonObject.put("lentAmount", list.get(30));
            transferDataItemDTO.setLentAmount(list.get(30));
            uploadJsonObject.put("unlentAmount", list.get(14));
            transferDataItemDTO.setUnlentAmount(list.get(14));
            uploadJsonObject.put("pushTime", list.get(2));
            transferJsonObject.put("pushTime", list.get(2));
            uploadJsonObject.put("loginChannel", list.get(9));
            transferJsonObject.put("loginChannel", list.get(9));
            uploadJsonObject.put("auditRate", list.get(12));
            transferJsonObject.put("auditRate", list.get(12));
            uploadJsonObject.put("couponType", list.get(13));
            transferJsonObject.put("couponType", list.get(13));
            uploadJsonObject.put("validityAmt", list.get(15));
            transferJsonObject.put("validityAmt", list.get(15));
            uploadJsonObject.put("rateType", list.get(16));
            transferJsonObject.put("rateType", list.get(16));
            uploadJsonObject.put("lentRate", list.get(17));
            transferJsonObject.put("lentRate", list.get(17));
            uploadJsonObject.put("validityRate", list.get(18));
            transferJsonObject.put("validityRate", list.get(18));
            uploadJsonObject.put("applyLentTime", list.get(25));
            transferJsonObject.put("applyLentTime", list.get(25));
            uploadJsonObject.put("extend01", list.get(32));
            transferJsonObject.put("extend01", list.get(32));
            uploadJsonObject.put("extend02", list.get(33));
            transferJsonObject.put("extend02", list.get(33));
            uploadJsonObject.put("lentAmountFirst", list.get(28));
            transferJsonObject.put("lentAmountFirst", list.get(28));
            uploadJsonObject.put("lentTimeFirst", list.get(27));
            transferJsonObject.put("lentTimeFirst", list.get(27));
            uploadJsonObject.put("cpsRate", list.get(31));
            transferJsonObject.put("cpsRate", list.get(31));
            uploadJsonObject.put("fileName", fileName);
            transferJsonObject.put("fileName", fileName);

            detailDTO.setReserveField1(uploadJsonObject.toJSONString());
            dataItems.add(detailDTO);

            transferDataItemDTO.setReserveField1(transferJsonObject.toJSONString());
            transferDataItemDTOS.add(transferDataItemDTO);
        });
        uploadDataDTO.setRequestId(apiCode + System.currentTimeMillis() + UUID.randomUUID());
        uploadDataDTO.setDataItems(dataItems);
        transferDataDTO.setDataItems(transferDataItemDTOS);
        transferDataDTO.setRequestId(apiCode + System.currentTimeMillis() + UUID.randomUUID());

    }
}