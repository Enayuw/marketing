package com.br.marketing.api.customer.upload.service.alien;

import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.br.common.encryption.Md5Utils;
import com.br.common.log.AlertLog;
import com.br.marketing.api.customer.upload.adapter.BaseUploadDataAdaptee;
import com.br.marketing.api.customer.upload.handler.CustomerUploadDataHandler;
import com.br.marketing.api.customer.upload.handler.CustomerUploadHandlerEnum;
import com.br.marketing.api.customer.upload.service.alien.dto.AlienUploadResponseDTO;
import com.br.marketing.common.constants.MarketingErrorInfo;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.dto.CustomerResponseDTO;
import com.br.marketing.dto.MarketingPreUserDTO;
import com.br.marketing.entity.MarketingCustomer;
import com.br.marketing.mapper.MarketingCustomerMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * 转化数据陌生客户处理
 *
 * @author Guo Zeqiang
 * @dateTime 2023-10-20 14:53
 */
@Service
@Slf4j
public class AlienUploadUploadDataServiceImpl implements CustomerUploadDataHandler {

    @Resource
    private MarketingCustomerMapper marketingCustomerService;

    @Override
    public CustomerUploadHandlerEnum customer() {
        return CustomerUploadHandlerEnum.U_ALIEN_DEFAULT;
    }

    @Override
    public BaseUploadDataAdaptee parseObject(String jsonData) {
        return new BaseUploadDataAdaptee() {
            @Override
            protected MarketingPreUserDTO adapteeRequest(String apiCode, MarketingPreUserDTO marketingPreUserDTO) {
                return null;
            }
        };
    }

    @Override
    public CustomerResponseDTO verifyFields(BaseUploadDataAdaptee adaptee) {
        AlienUploadResponseDTO alienUploadResponseDTO = new AlienUploadResponseDTO();
        alienUploadResponseDTO.success();
        List<MarketingCustomer> nameList = null;
        try {
            nameList = marketingCustomerService.getNameByApiCodeList(adaptee.getApiCode());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        String name;
        String shortName;
        if (CollectionUtils.isEmpty(nameList)) {
            name = customer().getName();
            shortName = name;
        } else {
            MarketingCustomer customer = nameList.get(0);
            name = customer.getName();
            shortName = customer.getShortName();
        }
        String msg = AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_USUAL_NOTICE.getCode(),
            "定制化转化接口接收到“" + shortName + "”编号“" + adaptee.getApiCode().concat("”的数据\n请及时与该“").concat(name).concat("”沟通确认^_^"), "通用定制化转化接口未知请求通知");
        log.warn(msg);
        return new CustomerResponseDTO(alienUploadResponseDTO, CustomerResponseDTO.StatusEnum.VALID, alienUploadResponseDTO.getCode());
    }

    /**
     * 获取requestId
     *
     * @param adaptee 适配器
     * @return {@link String }
     * @author senyang.zheng
     * @date 2024/08/07
     */
    @Override
    public String getRequestId(BaseUploadDataAdaptee adaptee) {
        String apiCode = adaptee.getApiCode();
        return apiCode.concat("_br_").concat(Md5Utils.cell32(RandomStringUtils.randomAlphabetic(32).concat("&") + System.nanoTime()));
    }

    @Override
    public int countBizDataNumber(BaseUploadDataAdaptee adaptee) {
        return countBizDataNumber(adaptee.getJsonData());
    }

    @Override
    public Set<String> getBizAllFields(String jsonStr) {
        return null;
    }

    @Override
    public CustomerResponseDTO jsonErrorResponse(Exception e) {
        AlienUploadResponseDTO alienUploadResponseDTO = new AlienUploadResponseDTO();
        alienUploadResponseDTO.failed(MarketingErrorInfo.JSON_DATA_ERROR);
        return new CustomerResponseDTO(alienUploadResponseDTO, CustomerResponseDTO.StatusEnum.INVALID, alienUploadResponseDTO.getCode());
    }

    @Override
    public CustomerResponseDTO bizErrorResponse(Exception e) {
        return fallbackResponse(e);
    }

    @Override
    public CustomerResponseDTO fallbackResponse(Exception e) {
        AlienUploadResponseDTO alienUploadResponseDTO = new AlienUploadResponseDTO();
        alienUploadResponseDTO.failed(MarketingErrorInfo.UNKNOWN_ERROR);
        return new CustomerResponseDTO(alienUploadResponseDTO, CustomerResponseDTO.StatusEnum.INVALID, alienUploadResponseDTO.getCode());
    }
}
