package com.br.marketing.service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.br.marketing.dto.ResponseCustomDTO;
import com.br.marketing.dto.gume.GuMeTransferJsonDTO;
import com.br.marketing.dto.gume.ResponseGuMeDTO;
import com.br.marketing.service.IPushGuMeDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 国美业务
 *
 * @author Guo Zeqiang
 * @dateTime 2023-10-16 17:06
 */
@Service
@Slf4j
public class PushGuMeDataServiceImpl implements IPushGuMeDataService {


    @Override
    public ResponseCustomDTO saveTransferData(String apiCode, String jsonData) {
        ResponseGuMeDTO responseGuMeDTO = new ResponseGuMeDTO();
        responseGuMeDTO.success();
        GuMeTransferJsonDTO jsonDTO;
        try {
            jsonDTO = JSONObject.parseObject(jsonData, new TypeReference<GuMeTransferJsonDTO>() {
            }.getType());
        } catch (Exception e) {
            responseGuMeDTO.failed("json解析失败");
            log.error(e.getMessage(), e);
            return responseGuMeDTO;
        }
        return null;
    }

    /**
     * 2023-10-16 18:08
     * 转化接口参数合法检查
     */
    private boolean transferApiParamRightfulCheck() {
        return true;
    }
}
