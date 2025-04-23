package com.br.marketing.client.tag;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.br.cloud.web.MethodType;
import com.br.cloud.web.PrometheusTimeMethod;
import com.br.common.log.AlertLog;
import com.br.marketing.client.net.ApiCallerUtil;
import com.br.marketing.client.tag.dto.AntaiosResourceDTO;
import com.br.marketing.client.tag.vo.AntaiosResourceVo;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.net.ThirdApiResultTransfer;
import com.br.marketing.mapper.datasource.log.InterfaceLogMapper;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @ClassName AntaiosResourceClient
 * @Description 同步自研标签库
 * @Author kongbx
 * @Date 2025/3/19 14:39
 */
@Slf4j
@Service
public class AntaiosResourceClient {

    @Value("${api.antaios.pushUserUrl:00}")
    private String tagUrl;

    @Resource
    MarketingCommonConfig marketingCommonConfig;

    @Autowired
    RestTemplate restTemplate;

    @Qualifier("logDbpool")
    @Autowired
    public ThreadPoolExecutor logDbpool;

    @Autowired
    InterfaceLogMapper interfaceLogMapper;

    @PrometheusTimeMethod(buckets = {0.02d, 0.05d, 0.2d, 0.5d, 1d}, methodType = MethodType.REMOTE)
    public AntaiosResourceVo getTagLibrary(AntaiosResourceDTO dto) {

        HashMap<String, Object> mock = marketingCommonConfig.getAntaiosResourceMock();
        if (mock.get("switch") == Boolean.TRUE) {
            return mockData(mock);
        }

        AntaiosResourceVo result = new AntaiosResourceVo();
        try{
            ThirdApiResultTransfer thirdApiResult = new ApiCallerUtil(restTemplate,interfaceLogMapper,logDbpool)
                    .setUrl(tagUrl)
                    .setContentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .setRequestParam(dto).postTransferStr();
            if(!Integer.valueOf(200).equals(thirdApiResult.getHttpCode())){
                throw new RuntimeException("外呼标签：".concat(String.valueOf(thirdApiResult.getHttpCode())));
            }
            result = JSON.parseObject(thirdApiResult.getResult()
                    , new TypeReference<AntaiosResourceVo>() {}.getType());
            return result;
        }catch (Exception ex){
            log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.PUSHING_CUSTOMERERROR.getCode(), ex.getMessage()), ex);
            result.setCode("9999");
            result.setMessage(ex.getMessage());
        }
        return result;
    }

    private AntaiosResourceVo mockData(HashMap<String, Object> mock) {
        AntaiosResourceVo result = new AntaiosResourceVo();
        if("000000".equals(mock.get("code").toString())){
            result.setCode("000000");
            result.setMessage("请求成功！");
            result.setData("iPhone提示音挂机末句,iPhone提示音挂机首句,客户要求发短信");
        }else {
            result.setCode(mock.get("code").toString());
            result.setMessage("请求失败！");
        }
        return result;
    }

}
