package com.br.marketing.service.clean.smy.impl;

import cn.hutool.core.lang.UUID;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.marketingapi.input.UploadDataDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.dto.MarketingPreUserDTO;
import com.br.marketing.dto.MarketingPreUserDetailDTO;
import com.br.marketing.dto.smy.request.NameValueDTO;
import com.br.marketing.dto.smy.request.SmyUploadRequestDTO;
import com.br.marketing.entity.CustomizeUploadDataSmy;
import com.br.marketing.mapper.CustomizeUploadDataSmyMapper;
import com.br.marketing.service.Impl.TableCreateServiceImpl;
import com.br.marketing.service.PushInfoService;
import com.br.marketing.service.clean.smy.SmyDataCleanService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SmyDataCleanServiceImpl implements SmyDataCleanService {
    @Resource
    private MarketingCommonConfig marketingCommonConfig;
    @Resource
    private CustomizeUploadDataSmyMapper customizeUploadDataSmyMapper;
    @Resource
    private PushInfoService pushInfoService;
    @Resource
    private TableCreateServiceImpl tableCreateService;

    @Override
    public void cleanCustomizedUploadData(String apiCode, String date) {

        String tCid = tableCreateService.getTcId(apiCode);
        JSONObject smyCustomizeDataConfig = marketingCommonConfig.getSmyCustomizeDataCleanConfig();
        Integer limit = smyCustomizeDataConfig.getInteger("limit");
        Integer threadNum = smyCustomizeDataConfig.getInteger("threadNum");
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(threadNum, threadNum, "SMY_CUSTOMIZED_UPLOAD_DATA_CLEAN_THREAD_POOL", 200);
        boolean mark = Boolean.TRUE;
        Long minId = customizeUploadDataSmyMapper.smyCleanCustomizedUploadDataOfMinId(tCid, apiCode, date);
        if (minId == null) {
            return;
        }
        minId = minId - 1;
        while (mark) {
            List<CustomizeUploadDataSmy> uploadDataSmyList = customizeUploadDataSmyMapper.smyCleanCustomizedUploadDataByMinId(tCid, apiCode, date,
                    minId, limit);
            if (uploadDataSmyList.size() <= 0) {
                mark = Boolean.FALSE;
                continue;
            }
            minId = uploadDataSmyList.get(uploadDataSmyList.size() - 1).getId();
            threadPool.submit(() -> {
                for (CustomizeUploadDataSmy customizeUploadDataSmy : uploadDataSmyList) {
                    SmyUploadRequestDTO smyUploadRequestDTO = JSONObject.parseObject(customizeUploadDataSmy.getRequestJsonData(),
                            SmyUploadRequestDTO.class);
                    Result<Boolean> result;
                    try {
                        MarketingPreUserDTO userDTO = new MarketingPreUserDTO();
                        userDTO.setTaskId(smyUploadRequestDTO.getCaseType());
                        userDTO.setRequestId(apiCode + "_" + smyUploadRequestDTO.getCaseType() + "_" + UUID.fastUUID().toString(true));
                        List<MarketingPreUserDetailDTO> dataUploadItems = buildUploadDataItems(smyUploadRequestDTO);
                        userDTO.setDataItems(dataUploadItems);
                        UploadDataDTO uploadDataDTO = new UploadDataDTO();
                        uploadDataDTO.setApiCode(apiCode);
                        uploadDataDTO.setJsonData(JSONObject.toJSONString(userDTO));
                        result = pushInfoService.pushUploadByRetry(uploadDataDTO, null);
                        if (ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                            customizeUploadDataSmyMapper.updateSyncStatusById(tCid, customizeUploadDataSmy.getId(), 1);
                        }
                    } catch (Exception e) {
                        log.warn(AlertLog.buildWarnMessage(AlarmSendCodeEnum.SAMOYE_CUSTOMIZE_UPLOAD_SERVICEERROR.getCode(),
                                "微聚定制上传数据清洗，子线程处理异常，前置表id：" + customizeUploadDataSmy.getId()), e);
                    }
                }
            });
        }
    }

    private List<MarketingPreUserDetailDTO> buildUploadDataItems(SmyUploadRequestDTO uploadRequestDTO) {
        List<NameValueDTO> nameList = uploadRequestDTO.getNameList();
        List<MarketingPreUserDetailDTO> dataItems = Lists.newArrayList();
        nameList.forEach((NameValueDTO nameValueDTO) -> {
            MarketingPreUserDetailDTO marketingPreUserDetailDTO = new MarketingPreUserDetailDTO();
            marketingPreUserDetailDTO.setCell(nameValueDTO.getMidMd5());
            marketingPreUserDetailDTO.setCustNum(nameValueDTO.getCidMd5());
            JSONObject reserveField1 = new JSONObject();
            reserveField1.put("registerTime", nameValueDTO.getRegisterDatetime());
            if (nameValueDTO.getExtendFields() != null && !nameValueDTO.getExtendFields().isEmpty()) {
                reserveField1.putAll(nameValueDTO.getExtendFields());
            }
            marketingPreUserDetailDTO.setReserveField1(reserveField1.toJSONString());
            dataItems.add(marketingPreUserDetailDTO);
        });
        return dataItems;
    }
}
