package com.br.marketing.service.file.filetodb;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.dto.TxtToDbDTO;
import com.br.marketing.mapper.LocalFileMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

/**
 * AbstractFileToDbAssembler
 *
 * @author xiang.li
 * @date 2024/01/22
 */
@Service
@Slf4j
public abstract class AbstractFileToDbAssembler{

    @Resource
    LocalFileMapper localFileMapper;

    public Result operateDateToDb(TxtToDbDTO toDbDTO) {
        try {
            log.info("operateDateToDb-start, "+ JSONObject.toJSONString(toDbDTO));
            // Step1 choose a sql template
            String tempContent = chooseSqlTemp("");
            log.info("tempContent: "+ tempContent);

            // Step2 assemble params for template
            Result<Map<String, Object>> tempParamsResult = assembleTempParams("", toDbDTO);
            if(!ResultCode.SUCCESS.getValue().equals(tempParamsResult.getCode()) || tempParamsResult.getData()==null){
                return new Result().setCode(ResultCode.FAIL.getValue()).setMessage("");
            }
            Map<String, Object> tempParamsMap = tempParamsResult.getData();
            log.info("tempParamsMap: "+ JSONObject.toJSONString(tempParamsMap));

            // Step3 fill template and insert to db
            Map<String, String> placeHolders = (Map<String, String>) tempParamsMap.get("placeHolders");
            insertToDb(tempContent, placeHolders);

            JSONObject resMsg = new JSONObject();
            resMsg.put("successNum", tempParamsMap.get("successNum"));
            resMsg.put("errorNum", tempParamsMap.get("errorNum"));
            log.info("operateDateToDb-end, "+ JSONObject.toJSONString(resMsg));
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setMessage(JSON.toJSONString(resMsg));
        }catch (Exception e){
            log.error(e.getMessage(),e);
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage(e.getMessage());
        }
    }

    public abstract String chooseSqlTemp(String tempType);

    public abstract Result<Map<String, Object>> assembleTempParams(String tempType, TxtToDbDTO toDbDTO);

    public void insertToDb(String tempContent, Map<String, String> placeHolders){
        if(placeHolders==null || placeHolders.size()<1){
            return;
        }
        String sqlContent = tempContent;
        for (Map.Entry<String, String> entry :placeHolders.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sqlContent = sqlContent.replace("#{"+key+"}", value);
        }
        if(StringUtils.isNotBlank(sqlContent)) {
            localFileMapper.insertFileData(sqlContent);
        }
    }
}
