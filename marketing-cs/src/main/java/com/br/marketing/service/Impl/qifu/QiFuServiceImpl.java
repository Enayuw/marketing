package com.br.marketing.service.Impl.qifu;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.common.log.AlertLog;
import com.br.marketing.client.marketingapi.input.UploadDataDTO;
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
import com.br.marketing.service.PushInfoService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
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
        Integer pageSize = Integer.valueOf(getValueOfJson(qifuAiCleanConfig, "pageSize", "10"));
        Integer threadNum = Integer.valueOf(getValueOfJson(qifuAiCleanConfig, "threadNum", "1"));
        ThreadPoolExecutor threadPool = BrExecutors.getThreadPool(threadNum, threadNum, "qiAiClean", 200);
        Boolean actionMark = Boolean.TRUE;
        while (actionMark) {
            List<DrsCustomizeUploadData> dataOfNeedClean = drsCustomizeUploadDataMapper.getDataOfNeedClean(tcId, pageSize);
            if (dataOfNeedClean.size() <= 0) {
                actionMark = Boolean.FALSE;
                continue;
            }
            StringBuilder insertLogSql = new StringBuilder();
            insertLogSql.append("insert into b_log_360ai ");
            insertLogSql.append("(data_id,status) ");
            insertLogSql.append("values ");
            for (DrsCustomizeUploadData drsCustomizeUploadData : dataOfNeedClean) {
                insertLogSql.append("(").append(drsCustomizeUploadData.getId()).append(",1),");
            }
            String insertLog = insertLogSql.toString().substring(0, insertLogSql.toString().length() - 1);
            log360aiMapper.batchSaveLog(insertLog);
            threadPool.submit(() -> {
                try {
                    pushProcess(dataOfNeedClean);
                }catch (Exception ex){
                    log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.QIFU_SERVICEERROR.getCode(),ex.getMessage()),ex);
                }
            });

        }
        shutdownThreadPool(threadPool);
    }

    void pushProcess(List<DrsCustomizeUploadData> dataOfNeedClean) {
        for (DrsCustomizeUploadData drsCustomizeUploadData : dataOfNeedClean) {
            MarketingPreUserDTO marketingPreUserDTO = buildPushDto(drsCustomizeUploadData);
            if(marketingPreUserDTO == null){
                Log360aiExample example = new Log360aiExample();
                example.createCriteria().andDataIdEqualTo(drsCustomizeUploadData.getId());
                Log360ai log360ai = new Log360ai();
                log360ai.setStatus(Byte.valueOf("3"));
                log360aiMapper.updateByExampleSelective(log360ai,example);
                continue;
            }
            UpLoadCleanDTO upLoadCleanDTO = new UpLoadCleanDTO();
            upLoadCleanDTO.setDataId(drsCustomizeUploadData.getId());
            upLoadCleanDTO.setApiCode(drsCustomizeUploadData.getApiCode());
            upLoadCleanDTO.setJsonData(JSON.toJSONString(marketingPreUserDTO));
            pushInfoService.pushUploadOfCleanRetry(upLoadCleanDTO,null);
        }
    }

    private MarketingPreUserDTO buildPushDto(DrsCustomizeUploadData drsCustomizeUploadData) {
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
                        errorMsg.append("batchNo为空");
                        continue outerLoop;
                    }
                    requestId = String.format("%s_%s", drsCustomizeUploadData.getId(), flowNo);
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
                    if (templateStr.length() > 12) {
                        userType = templateStr.substring(templateStr.length() - 13);
                    } else {
                        userType = templateStr;
                        warnMsg.append("templateNo长度小于12");
                    }
                    extendKey.put("userType", userType);
                default:
                    extendKey.put(s, jsonObject.getString(s));
                    break;
            }
        }

        if (StringUtils.isNotBlank(errorMsg.toString())) {
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.QIFU_SERVICEERROR.getCode()
                    , "奇富360ai清洗数据异常[" + drsCustomizeUploadData.getId() + "]" + errorMsg.toString()));
            return null;
        }

        marketingPreUserDTO.setTaskId(taskId);
        marketingPreUserDTO.setRequestId(requestId);

        JSONArray dataList = jsonObject.getJSONArray("dataList");
        if(!ObjectUtils.isEmpty(dataList)) {
            for (Object o : dataList) {
                JSONObject reserField1 = new JSONObject();
                BeanUtils.copyProperties(extendKey, reserField1);
                JSONObject o1 = (JSONObject) o;
                MarketingPreUserDetailDTO marketingPreUserDetailDTO = buildListDto(o1, reserField1, warnMsg);
                list.add(marketingPreUserDetailDTO);
            }
            if (StringUtils.isNotBlank(warnMsg.toString())) {
                log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.QIFU_SERVICEERROR.getCode()
                        , "奇富360ai清洗数据异常字段告警[" + drsCustomizeUploadData.getId() + "]" + warnMsg.toString()));
            }
        }

        return marketingPreUserDTO;
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
                        warnMsg.append("异常性别：" + genderValue);
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
        while (b){
            if(executor.isTerminated()){
                System.out.println("结束");
                b=false;
            }else{
                System.out.println("休息");
                try {
                    Thread.sleep(3000L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
