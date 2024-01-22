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

@Service
@Slf4j
public abstract class AbstractFileToDbAssembler{

    @Resource
    LocalFileMapper localFileMapper;

    public Result operateDateToDb(TxtToDbDTO toDbDTO) {
        try {
            String tempContent = chooseSqlTemp("");
            Map<String, Object> tempParamsMap = assembleTempParams("", toDbDTO);
            Map<String, String> placeHolders = (Map<String, String>) tempParamsMap.get("placeHolders");
            insertToDb(tempContent, placeHolders);

            JSONObject resMsg = new JSONObject();
            resMsg.put("successNum", tempParamsMap.get("successNum"));
            resMsg.put("errorNum", tempParamsMap.get("errorNum"));
            return new Result().setCode(ResultCode.SUCCESS.getValue()).setMessage(JSON.toJSONString(resMsg));
        }catch (Exception ex){
            log.error(ex.getMessage(),ex);
            return new Result().setCode(ResultCode.FAIL.getValue()).setMessage(ex.getMessage());
        }
    }

    public abstract String chooseSqlTemp(String tempType);

    public abstract Map<String, Object> assembleTempParams(String tempType, TxtToDbDTO toDbDTO);

    public void insertToDb(String tempContent, Map<String, String> placeHolders){
        if(placeHolders==null || placeHolders.size()<1){
            return;
        }
        String sqlContent = tempContent;
        for (String key :placeHolders.keySet()) {
            sqlContent = sqlContent.replace("#{"+key+"}", placeHolders.get(key));
        }
        if(StringUtils.isNotBlank(sqlContent)) {
            localFileMapper.insertFileData(sqlContent);
        }
    }
}
