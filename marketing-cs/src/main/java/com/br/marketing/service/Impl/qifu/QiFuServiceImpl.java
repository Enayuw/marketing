package com.br.marketing.service.Impl.qifu;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.marketingapi.input.UploadDataDTO;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.BrExecutors;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.MarketingPreUserDTO;
import com.br.marketing.dto.MarketingPreUserDetailDTO;
import com.br.marketing.dto.qifu.UpLoadCleanDTO;
import com.br.marketing.entity.DrsCustomizeUploadData;
import com.br.marketing.entity.Log360ai;
import com.br.marketing.entity.Log360aiExample;
import com.br.marketing.mapper.DrsCustomizeUploadDataMapper;
import com.br.marketing.mapper.Log360aiMapper;
import com.br.marketing.service.Impl.qifu.valobj.QiFuCleanStatusEnum;
import com.br.marketing.service.PushInfoService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

@Service
@Slf4j
public class QiFuServiceImpl implements IQiFuService {

    @Resource
    DrsCustomizeUploadDataMapper drsCustomizeUploadDataMapper;

    @Resource
    MarketingCommonConfig marketingCommonConfig;

    @Resource
    Log360aiMapper log360aiMapper;

    @Resource
    PushInfoService pushInfoService;

    @Override
    public void aiCleanProcess() {

        JSONObject qifuAiCleanConfig = marketingCommonConfig.getQifuAiCleanConfig();
        String tcId = getValueOfJson(qifuAiCleanConfig, "tCid", "");
        List<String> apiCodes = Arrays.asList(getValueOfJson(qifuAiCleanConfig, "cleanApiCode", "3700226").split(","));
        String dataTimeMark = getValueOfJson(qifuAiCleanConfig, "dataTime", "-1");
        LocalDate now = LocalDate.now();
        String nowDay = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String yesterDay = now.minusDays(1L).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        List<String> receiveDates = new ArrayList<>();
        if ("-1".equals(dataTimeMark)) {
            receiveDates.add(yesterDay);
            receiveDates.add(nowDay);
        } else if ("1".equals(dataTimeMark)) {
            receiveDates.add(nowDay);
        } else {
            receiveDates.add(dataTimeMark);
        }
        Integer pageSize = Integer.valueOf(getValueOfJson(qifuAiCleanConfig, "pageSize", "10"));
        Integer threadNum = Integer.valueOf(getValueOfJson(qifuAiCleanConfig, "threadNum", "1"));
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(threadNum, threadNum, "qiAiClean", 200);
        Boolean actionMark = Boolean.TRUE;
        while (actionMark) {

            Boolean b = dynamicAction(threadPool);
            if (b) {
                actionMark = Boolean.FALSE;
                continue;
            }
            List<DrsCustomizeUploadData> dataOfNeedClean = drsCustomizeUploadDataMapper.getDataOfNeedClean(tcId, apiCodes, receiveDates, pageSize);
            if (dataOfNeedClean.size() <= 0) {
                actionMark = Boolean.FALSE;
                continue;
            }
            ArrayList<Long> ids = new ArrayList<>();
            StringBuilder insertLogSql = new StringBuilder();
            insertLogSql.append("insert into b_log_360ai ");
            insertLogSql.append("(data_id,status) ");
            insertLogSql.append("values ");
            for (DrsCustomizeUploadData drsCustomizeUploadData : dataOfNeedClean) {
                insertLogSql.append(String.format("(%d,%d),", drsCustomizeUploadData.getId(), QiFuCleanStatusEnum.RUNNING.getValue()));
                ids.add(drsCustomizeUploadData.getId());
            }
            String insertLog = insertLogSql.toString().substring(0, insertLogSql.toString().length() - 1);
            log360aiMapper.batchSaveLog(insertLog);
            drsCustomizeUploadDataMapper.updateSyncStatusByIds(tcId, ids, 1);
            threadPool.submit(() -> {
                try {
                    pushProcess(dataOfNeedClean);
                } catch (Exception ex) {
                    log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.QIFU_SERVICEERROR.getCode(), ex.getMessage()), ex);
                }
            });

        }
        shutdownThreadPool(threadPool);
    }

    void pushProcess(List<DrsCustomizeUploadData> dataOfNeedClean) {
        for (DrsCustomizeUploadData drsCustomizeUploadData : dataOfNeedClean) {
            // 生成推送对象
            Result<MarketingPreUserDTO> result = buildPushDto(drsCustomizeUploadData);
            if (!ResultCode.SUCCESS.getValue().equals(result.getCode())) {
                Log360aiExample example = new Log360aiExample();
                example.createCriteria().andDataIdEqualTo(drsCustomizeUploadData.getId());
                Log360ai log360ai = new Log360ai();
                log360ai.setStatus(QiFuCleanStatusEnum.FAILDATAACTION.getValue());
                log360ai.setErrorMsg(result.getMessage());
                log360aiMapper.updateByExampleSelective(log360ai, example);
                continue;
            }
            // 推送
            UpLoadCleanDTO upLoadCleanDTO = new UpLoadCleanDTO();
            upLoadCleanDTO.setDataId(drsCustomizeUploadData.getId());
            upLoadCleanDTO.setApiCode(drsCustomizeUploadData.getApiCode());
            upLoadCleanDTO.setJsonData(JSON.toJSONString(result.getData()));
            pushInfoService.pushUploadOfCleanRetry(upLoadCleanDTO, null);
        }
    }

    private Result<MarketingPreUserDTO> buildPushDto(DrsCustomizeUploadData drsCustomizeUploadData) {
        Result<MarketingPreUserDTO> res = new Result<>();
        StringBuilder errorMsg = new StringBuilder();
        StringBuilder warnMsg = new StringBuilder();
        MarketingPreUserDTO marketingPreUserDTO = new MarketingPreUserDTO();
        ArrayList<MarketingPreUserDetailDTO> list = new ArrayList<>();
        marketingPreUserDTO.setDataItems(list);
        String requestJsonData = drsCustomizeUploadData.getRequestJsonData();
        JSONObject jsonObject = JSONObject.parseObject(requestJsonData);
        String taskId = "";
        String requestId = "";
        JSONObject extendKey = new JSONObject();
        String batch = drsCustomizeUploadData.getReceiveDate().replaceAll("-", "").concat("_").concat(drsCustomizeUploadData.getApiCode());
        extendKey.put("operateType", "3");
        extendKey.put("batchName", batch);
        extendKey.put("batchNumber", batch);

        try {
            //region 遍历一级字段
            outerLoop:
            for (String s : jsonObject.keySet()) {
                switch (s) {
                    case "batchNo":
                        taskId = jsonObject.getString(s);
                        if (ObjectUtils.isEmpty(taskId)) {
                            errorMsg.append("batchNo为空");
                            continue outerLoop;
                        }
                        break;
                    case "flowNo":
                        String flowNo = jsonObject.getString(s);
                        if (ObjectUtils.isEmpty(flowNo)) {
                            errorMsg.append("flowNo为空");
                            continue outerLoop;
                        }
                        requestId = String.format("%s_%s", drsCustomizeUploadData.getId(), flowNo);
                        extendKey.put(s, jsonObject.getString(s));
                        break;
                    case "dataList":
                        break;
                    case "templateNo":
                        String templateStr = jsonObject.getString(s);
                        if (ObjectUtils.isEmpty(templateStr)) {
                            errorMsg.append("templateNo为空");
                            continue outerLoop;
                        }
                        String userType = "";
                        String strategyCode = "";
                        if (templateStr.length() > 12) {
                            userType = templateStr.substring(0, templateStr.length() - 12);
                            strategyCode = templateStr.substring(templateStr.length() - 12);
                        } else {
                            userType = templateStr;
                            errorMsg.append("templateNo长度小于12");
                            continue outerLoop;
                        }
                        boolean flag = marketingCommonConfig.getQifuAiCleanStrategyCodeFlag();
                        if (flag) {
                            extendKey.put("strategyCode", "");
                            extendKey.put("strategyName", "");
                        } else {
                            extendKey.put("strategyCode", strategyCode);
                            extendKey.put("strategyName", strategyCode);
                        }
                        extendKey.put("userType", userType);
                        break;
                    case "operateScene":
                        extendKey.put("customName", jsonObject.getString(s));
                        extendKey.put("customNameType", jsonObject.getString(s));
                        break;
                    default:
                        extendKey.put(s, jsonObject.getString(s));
                        break;
                }
            }
            //endregion

            if (StringUtils.isNotBlank(errorMsg.toString())) {
                log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.QIFU_SERVICEERROR.getCode()
                        , "奇富360ai清洗数据异常[" + drsCustomizeUploadData.getId() + "]" + errorMsg.toString()));
                return res.setCode(ResultCode.FAIL.getValue()).setMessage(errorMsg.toString());
            }

            marketingPreUserDTO.setTaskId(taskId);
            marketingPreUserDTO.setRequestId(requestId);

            //region 遍历二级字段
            JSONArray dataList = jsonObject.getJSONArray("dataList");
            if (!ObjectUtils.isEmpty(dataList)) {
                for (Object o : dataList) {
                    JSONObject reserField1 = new JSONObject();
                    extendKey.keySet().forEach(t -> reserField1.put(t, extendKey.get(t)));
                    JSONObject o1 = (JSONObject) o;
                    MarketingPreUserDetailDTO marketingPreUserDetailDTO = buildListDto(o1, reserField1, warnMsg);
                    list.add(marketingPreUserDetailDTO);
                }
                if (StringUtils.isNotBlank(warnMsg.toString())) {
                    log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.QIFU_SERVICEERROR.getCode()
                            , "奇富360ai清洗数据异常字段告警[" + drsCustomizeUploadData.getId() + "]" + warnMsg.toString()));
                }
            }
            //endregion

            return res.setCode(ResultCode.SUCCESS.getValue()).setDate(marketingPreUserDTO).setMessage(warnMsg.toString());
        } catch (Exception ex) {
            return res.setCode(ResultCode.FAIL.getValue()).setMessage(ex.getMessage());
        }
    }


    private MarketingPreUserDetailDTO buildListDto(JSONObject o1, JSONObject reserField1, StringBuilder warnMsg) {
        MarketingPreUserDetailDTO marketingPreUserDetailDTO = new MarketingPreUserDetailDTO();
        for (String s : o1.keySet()) {
            switch (s) {
                case "serialNo":
                    marketingPreUserDetailDTO.setCustNum(o1.getString(s));
                    break;
                case "phoneNoMd5":
                    marketingPreUserDetailDTO.setCell(o1.getString(s));
                    break;
                case "surname":
                    reserField1.put("firstName", o1.getString(s));
                    break;
                case "gender":
                    String genderValue = o1.getString(s);
                    if ("F".equals(genderValue)) {
                        reserField1.put(s, "0");
                    } else if ("M".equals(genderValue)) {
                        reserField1.put(s, "1");
                    } else if (!"".equals(genderValue)) {
                        warnMsg.append("异常性别：").append(genderValue);
                    }
                    break;
                default:
                    reserField1.put(s, o1.getString(s));
                    break;
            }
        }
        marketingPreUserDetailDTO.setReserveField1(JSONArray.toJSONString(reserField1));
        return marketingPreUserDetailDTO;
    }

    private String getValueOfJson(JSONObject jo, String key, String defaultValue) {
        if (jo == null || ObjectUtils.isEmpty(jo.getString(key))) {
            return defaultValue;
        }
        return jo.getString(key);
    }

    private void shutdownThreadPool(ThreadPoolExecutor executor) {
        executor.shutdown();
        Boolean b = true;
        while (b) {
            if (executor.isTerminated()) {
                b = false;
            } else {
                try {
                    Thread.sleep(3000L);
                } catch (InterruptedException e) {
                    log.error(e.getMessage(),e);
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private Boolean dynamicAction(ThreadPoolExecutor executor) {
        JSONObject qifuAiCleanConfig = marketingCommonConfig.getQifuAiCleanConfig();
        Boolean isPause = qifuAiCleanConfig.getBoolean("isPause");
        if (isPause == null || isPause) {
            return Boolean.TRUE;
        }
        if (StringUtils.isNotBlank(qifuAiCleanConfig.getString("threadNum"))) {
            Integer threadNum = Integer.valueOf(qifuAiCleanConfig.getString("threadNum"));
            if (executor.getCorePoolSize() != threadNum.intValue()) {
                executor.setCorePoolSize(threadNum);
                executor.setMaximumPoolSize(threadNum);
            }
        }
        return Boolean.FALSE;
    }
}
