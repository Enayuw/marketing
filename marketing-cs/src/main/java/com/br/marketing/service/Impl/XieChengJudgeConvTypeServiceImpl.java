package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.entity.XieChengJudgeConvTypeValue;
import com.br.marketing.mapper.MarketingTransferSyncUserMapper;
import com.br.marketing.service.ValidityPeriodDataService;
import com.br.marketing.service.XieChengJudgeConvTypeService;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import cn.hutool.core.lang.Pair;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * 描述：： 根据有效期框定数据范围实现
 * <p>
 * ------------------------------------
 * @program: marketing
 * @ClassName XieChengJudgeConvTypeServiceImpl
 * @author: chenh
 * @create: 2023-09-22 21:24
 * @Version 1.0
 * --------------------------------------
 **/
@Service
@Slf4j
public class XieChengJudgeConvTypeServiceImpl implements XieChengJudgeConvTypeService {

    @Resource
    private MarketingTransferSyncUserMapper marketingTransferSyncUserMapper;
    @Resource
    private ValidityPeriodDataService validityPeriodDataService;
    @Autowired
    MarketingCommonConfig marketingCommonConfig;
    @Resource
    private TableCreateServiceImpl tableCreateService;

    @Override
    public List<XieChengJudgeConvTypeValue> getJudgeConvType(String apiCode, String custNum) {
        JSONObject condition = getCondition(apiCode);
        // 查询有效期使用的apiCode
        String mainApiCode = condition.getString("mainApiCode");
        // 查询转化数据convType使用的apiCode
        JSONArray convTypeApiCodes = condition.getJSONArray("convTypeApiCodes");
        String tcid = tableCreateService.getTcId(apiCode);

        // 获取有效期范围
        Pair<String, String> validityRange =
                validityPeriodDataService.getMarketingTransferDataWithValidityRange(mainApiCode);
        if (validityRange == null) {
            log.error("携程所有配置在有效期配置表中的上传数据均已失效！");
            return null;
        }

        String startDate = validityRange.getKey();
        String endDate = validityRange.getValue();

        Set<String> transferSet = new HashSet<>();
        transferSet.add(custNum);
        List<XieChengJudgeConvTypeValue> xieChengJudgeConvType = marketingTransferSyncUserMapper.getXieChengJudgeConvType(tcid, convTypeApiCodes,
                startDate, endDate, transferSet);
        return xieChengJudgeConvType;
    }

    @Override
    public JSONObject getCondition(String apiCode) {
        HashMap<String, JSONObject> xieChengCallPushCondition = marketingCommonConfig.getXieChengCallPushCondition();
        if (xieChengCallPushCondition == null) {
            xieChengCallPushCondition = new HashMap<>();
            xieChengCallPushCondition.put("3710058", getJo("1", Arrays.asList("3710058", "3710078"), "3710058"));
            xieChengCallPushCondition.put("3710078", getJo("1", Arrays.asList("3710058", "3710078"), "3710058"));
            xieChengCallPushCondition.put("3710090", getJo("2", Arrays.asList("3710090", "3710091"), "3710090"));
            xieChengCallPushCondition.put("3710091", getJo("2", Arrays.asList("3710090", "3710091"), "3710090"));
        }
        JSONObject condition = xieChengCallPushCondition.get(apiCode);
        return condition;
    }

    private JSONObject getJo(String condition, List<String> soleCellApiCodes, String mainApiCode) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("condition", condition);
        jsonObject.put("isBlackApiCodes", soleCellApiCodes);
        jsonObject.put("convTypeApiCodes", soleCellApiCodes);
        jsonObject.put("soleCellApiCodes", soleCellApiCodes);
        jsonObject.put("mainApiCode", mainApiCode);
        return jsonObject;
    }
}
