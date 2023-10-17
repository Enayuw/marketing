package com.br.marketing.service.Impl.guomei;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.br.arch.geo.pulsar.ProductPulsarClientManager;
import com.br.arch.geo.pulsar.ProductPulsarProducer;
import com.br.common.encryption.Md5Utils;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.constants.PulsarTopic;
import com.br.marketing.dto.ResponseCustomDTO;
import com.br.marketing.dto.gume.GuMeTransferJsonDTO;
import com.br.marketing.dto.gume.ResponseGuMeDTO;
import com.br.marketing.entity.GuoMeiTransferData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.pulsar.client.api.PulsarClientException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.Date;
import java.util.Locale;

/**
 * 国美业务
 *
 * @author Guo Zeqiang
 * @dateTime 2023-10-16 17:06
 */
@Service
@Slf4j
public class PushGuMeDataServiceImpl implements IPushGuMeDataService {

    @Resource
    private IGuoMeiDataService guoMeiDataService;

    @Override
    public ResponseCustomDTO saveTransferData(String apiCode, String jsonData) {
        ResponseGuMeDTO responseGuMeDTO = new ResponseGuMeDTO();
        GuMeTransferJsonDTO jsonDTO;
        GuoMeiTransferData guoMeiTransferData = new GuoMeiTransferData();
        guoMeiTransferData.setCreateDate(LocalDate.now().toString());
        guoMeiTransferData.setCreateTime(new Date());
        guoMeiTransferData.setUpdateTime(guoMeiTransferData.getCreateTime());
        guoMeiTransferData.setJsonData(jsonData);
        try {
            jsonDTO = JSONObject.parseObject(jsonData, new TypeReference<GuMeTransferJsonDTO>() {
            }.getType());
            if (transferApiParamRightfulCheck(jsonDTO, responseGuMeDTO)) {
                responseGuMeDTO.success();
                guoMeiTransferData.setApiCode(apiCode);
                guoMeiTransferData.setChannelcode(jsonDTO.getChannelCode());
                guoMeiTransferData.setRequestid(jsonDTO.getRequestId());
                guoMeiTransferData.setSign(jsonDTO.getSign());
                guoMeiTransferData.setStatus(1);
            } else {
                return responseGuMeDTO;
            }
        } catch (Exception e) {
            responseGuMeDTO.failed("json解析失败");
            guoMeiTransferData.setErrorMsg(responseGuMeDTO.getDesc().concat(":") + e.getMessage());
            log.error(e.getMessage(), e);
        }
        try {
            guoMeiDataService.saveTransferDataHandler(guoMeiTransferData);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            try {
                ProductPulsarProducer producer = ProductPulsarClientManager.newProducer(PulsarTopic.transferGuoMeiTopic);
                String jsonString = JSON.toJSONString(guoMeiTransferData);
                byte[] message = jsonString.getBytes();
                producer.send(message);
                log.warn(String.format("写入Pulsar 主题:%s 数据:%s", PulsarTopic.transferGuoMeiTopic, jsonString));
            } catch (PulsarClientException clientException) {
                responseGuMeDTO.failed();
            }
        }
        return responseGuMeDTO;
    }

    /**
     * 2023-10-16 18:08
     * 转化接口参数合法检查
     */
    private boolean transferApiParamRightfulCheck(GuMeTransferJsonDTO jsonDTO, ResponseGuMeDTO responseGuMeDTO) {
        if (StringUtils.isBlank(jsonDTO.getSign())) {
            responseGuMeDTO.failed(ResponseGuMeDTO.ResultEnum.FAILED_PARAM_NULL, "sign不可为空");
            return false;
        }
        if (StringUtils.isBlank(jsonDTO.getChannelCode())) {
            responseGuMeDTO.failed(ResponseGuMeDTO.ResultEnum.FAILED_PARAM_NULL, "channelCode不可为空");
            return false;
        }
        if (StringUtils.isBlank(jsonDTO.getRequestId())) {
            responseGuMeDTO.failed(ResponseGuMeDTO.ResultEnum.FAILED_PARAM_NULL, "requestId不可为空");
            return false;
        }
        String sign = Md5Utils.cell32(jsonDTO.getRequestId() + jsonDTO.getChannelCode()).toUpperCase(Locale.ROOT);
        if (!jsonDTO.getSign().equals(sign)) {
            responseGuMeDTO.failed(ResponseGuMeDTO.ResultEnum.FAILED_PARAM_NULL, "sign签名不正确");
            return false;
        }
        if (CollectionUtils.isEmpty(jsonDTO.getData())) {
            responseGuMeDTO.failed(ResponseGuMeDTO.ResultEnum.FAILED_PARAM_NULL, "data不可为空");
            return false;
        }
        return true;
    }


    @Override
    public Result<Boolean> consumerTransfer(String msg) {
        Result<Boolean> result = new Result<>();
        GuoMeiTransferData guoMeiTransferData = JSONObject.parseObject(msg, new TypeReference<GuoMeiTransferData>() {
        }.getType());
        try {
            Long b = guoMeiDataService.saveTransferDataHandler(guoMeiTransferData);
            result.setCode(b != null && b > 0 ? ResultCode.SUCCESS.getValue() : ResultCode.FAIL.getValue());
        } catch (Exception e) {
            log.error(e.getMessage());
            result.setCode(ResultCode.FAIL.getValue());
        }
        return result;
    }
}
