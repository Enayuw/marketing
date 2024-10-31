package com.br.marketing.service.Impl.qifu;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.client.qifu.*;
import com.br.marketing.common.annoation.RetryMethod;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.qifu.QiFuCuWanJianBatQryUserRealParamsDto;
import com.br.marketing.entity.MarketingSyncUser;
import com.br.marketing.mapper.MarketingSyncUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;

/**
 * QiFuCuWanJianBatQryUserRealTransService
 *
 * @Author lixiang
 * @Date 2024-10-19
 */
@Service
@Slf4j
public class QiFuCuWanJianBatQryUserRealTransService {

    private final static String TITLE = "【360促完件用户信息批量查询】";

    @Resource
    private QiFuClients qiFuClients;

    @Resource
    private MarketingSyncUserMapper marketingSyncUserMapper;

    @RetryMethod(retryNowNum = 3, isOrNoDbRetry = true)
    public Result actionPartition(QiFuCuWanJianBatQryUserRealParamsDto paramsDto, Integer retry) {
        Result result = new Result().failure();

        String apiCode = paramsDto.getApiCode();
        String taskId = paramsDto.getTaskId();
        List<MarketingSyncUser> partition = paramsDto.getPartition();

        if(CollectionUtils.isEmpty(partition)){
            return result.success();
        }

        log.warn(TITLE + "actionPartition start, apiCode: {}, taskId: {}", apiCode, taskId);
        long start = System.currentTimeMillis();

        List<RealDataesReq> realDataes = new ArrayList<>();
        Map<String, Long> custNumToIdMap = new HashMap<>();
        for (MarketingSyncUser marketingSyncUser : partition) {
            RealDataesReq realDataesReq = new RealDataesReq();
            realDataesReq.setUniqueReqNo(marketingSyncUser.getCustNum());
            realDataesReq.setMobileMd5(marketingSyncUser.getCellMd5());
            realDataes.add(realDataesReq);
            Long id = marketingSyncUser.getId();
            custNumToIdMap.put(marketingSyncUser.getCustNum(), id);
        }

        QrySleepUserRealMessageReq qrySleepUserRealMessageReq = new QrySleepUserRealMessageReq();
        String uuid = UUID.randomUUID().toString();
        qrySleepUserRealMessageReq.setRequestNo(uuid);
        qrySleepUserRealMessageReq.setBatchNo(taskId);
        qrySleepUserRealMessageReq.setInitiatingType("noArt");
        qrySleepUserRealMessageReq.setPartner("bairong");
        qrySleepUserRealMessageReq.setRealDataes(realDataes);
        Result<ResponseData<QrySleepUserRealMessageResp>> dataResult = qiFuClients.qryUserRealMessageUrl(qrySleepUserRealMessageReq);
        log.warn(TITLE + "返回结果, dataResult: {}", JSONObject.toJSONString(dataResult));

        if (ResultCode.INTERNAL_SERVER_ERROR.getValue().equals(dataResult.getCode())) {
            return result.setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
        }

        if (ResultCode.FAIL.getValue().equals(dataResult.getCode())) {
            return result.setCode(ResultCode.INTERNAL_SERVER_ERROR.getValue());
        }

        if (ResultCode.SUCCESS.getValue().equals(dataResult.getCode())) {
            ResponseData<QrySleepUserRealMessageResp> data = dataResult.getData();
            ResultDataObj<QrySleepUserRealMessageResp> dataObj = data.getData();
            if(dataObj == null){
                return result.success();
            }
            QrySleepUserRealMessageResp qrySleepUserRealMessageResp = dataObj.getT();
            if(qrySleepUserRealMessageResp == null){
                return result.success();
            }
            List<QryUserRealMessage> realDetails = qrySleepUserRealMessageResp.getRealDetails();
            if(CollectionUtils.isEmpty(realDataes)){
                return result.success();
            }
            log.warn(TITLE + "返回结果, realDetails: {}", JSONObject.toJSONString(realDetails));

            // update
            for (QryUserRealMessage qryUserRealMessage : realDetails) {
                updateInfoList(apiCode, qryUserRealMessage, custNumToIdMap);
            }
        }
        long end = System.currentTimeMillis();
        log.warn(TITLE + "actionPartition end, cost: {}, apiCode: {}, taskId: {}", end-start, apiCode, taskId);
        return result.success();
    }

    private void updateInfoList(String apiCode, QryUserRealMessage qryUserRealMessage, Map<String, Long> custNumToIdMap){
        String custNum = qryUserRealMessage.getUniqueReqNo();
        if(StringUtils.isEmpty(custNum)){
            return;
        }
        Object userMessageRes = qryUserRealMessage.getUserMessageRes();
        if (userMessageRes == null) {
            return;
        }
        JSONObject userMessageJo = JSONObject.parseObject(userMessageRes.toString());
        if (userMessageJo == null) {
            return;
        }

        List<Map<String, String>> extendList = assembleExtendList(userMessageJo);
        // update
        Long id = custNumToIdMap.get(custNum);
        marketingSyncUserMapper.updateExtend(apiCode, extendList, id);
    }

    private List<Map<String, String>> assembleExtendList(JSONObject userMessageJo){
        String name = userMessageJo.getString("name");
        if(StringUtils.isEmpty(name)){
            name = "";
        }
        String sex = userMessageJo.getString("sex");
        if(StringUtils.isEmpty(sex)){
            sex = "";
        }
        String gender;
        switch (sex) {
            case "F":
                gender = "0";
                break;
            case "M":
                gender = "1";
                break;
            default:
                gender = "";
        }

        // extendList
        List<Map<String, String>> extendList = new ArrayList<>();
        Map<String, String> nameMap = new HashMap<>();
        nameMap.put("key", "cusName");
        nameMap.put("value", name);
        Map<String, String> sexMap = new HashMap<>();
        sexMap.put("key", "gender");
        sexMap.put("value", gender);

        extendList.add(nameMap);
        extendList.add(sexMap);
        return extendList;
    }


}
