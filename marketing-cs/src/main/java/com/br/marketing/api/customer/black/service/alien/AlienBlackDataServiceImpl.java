package com.br.marketing.api.customer.black.service.alien;

import com.br.common.encryption.Md5Utils;
import com.br.common.log.AlertLog;
import com.br.marketing.api.customer.black.adapter.BaseBlackDataAdaptee;
import com.br.marketing.api.customer.black.handler.CustomerBlackDataHandler;
import com.br.marketing.api.customer.black.handler.CustomerBlackHandlerEnum;
import com.br.marketing.api.customer.black.service.alien.dto.AlienBlackResponseDTO;
import com.br.marketing.common.constants.MarketingErrorInfo;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.dto.CustomerResponseDTO;
import com.br.marketing.dto.MarketingPreUserDTO;
import com.br.marketing.entity.MarketingCustomer;
import com.br.marketing.mapper.MarketingCustomerMapper;
import java.util.List;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * 外星人上传数据处理
 *
 * @author Guo Zeqiang
 * @date 2024/08/07
 */
@Service
@Slf4j
public class AlienBlackDataServiceImpl implements CustomerBlackDataHandler {

    @Resource
    private MarketingCustomerMapper marketingCustomerService;

    /**
     * 解密jsonData
     *
     * @param apiCode  apiCode
     * @param jsonData jsonData
     * @return {@link String }
     * @author senyang.zheng
     * @date 2024/09/11
     */
    @Override
    public String decryptJsonData(String apiCode, String jsonData) {
        return jsonData;
    }

    @Override
    public CustomerBlackHandlerEnum customer() {
        return CustomerBlackHandlerEnum.B_ALIEN_DEFAULT;
    }

    /**
     * 2023-10-18 16:45 反序列化客户定制数据
     *
     * @param jsonData json 字符串
     * @return 转化适配者
     */
    @Override
    public BaseBlackDataAdaptee<MarketingPreUserDTO> parseObject(String jsonData) {
        return new BaseBlackDataAdaptee<MarketingPreUserDTO>() {
            private static final long serialVersionUID = 8794287668420049112L;

            @Override
            protected MarketingPreUserDTO adapteeRequest(String apiCode, String jsonData) {
                return null;
            }
        };
    }

    @Override
    public CustomerResponseDTO verifyFields(BaseBlackDataAdaptee adaptee) {
        AlienBlackResponseDTO alienBlackResponseDTO = new AlienBlackResponseDTO();
        alienBlackResponseDTO.success();
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
                "定制化上传接口接收到“" + shortName + "”编号“" + adaptee.getApiCode().concat("”的数据\n请及时与该“").concat(name).concat("”沟通确认^_^"), "通用定制化上传接口未知请求通知");
        log.warn(msg);
        return new CustomerResponseDTO(alienBlackResponseDTO, CustomerResponseDTO.StatusEnum.VALID, alienBlackResponseDTO.getCode());
    }

    /**
     * 获取requestId
     *
     * @param apiCode apiCode
     * @param adaptee 适配器
     * @return {@link String }
     * @author senyang.zheng
     * @date 2024/08/07
     */
    @Override
    public String getRequestId(String apiCode, BaseBlackDataAdaptee adaptee) {
        return apiCode.concat("_br_").concat(Md5Utils.cell32(RandomStringUtils.randomAlphabetic(32).concat("&") + System.nanoTime()));
    }

    @Override
    public int countBizDataNumber(BaseBlackDataAdaptee adaptee) {
        return countBizDataNumber(adaptee.getJsonData());
    }


    @Override
    public CustomerResponseDTO jsonErrorResponse(Exception e) {
        AlienBlackResponseDTO alienBlackResponseDTO = new AlienBlackResponseDTO();
        alienBlackResponseDTO.failed(MarketingErrorInfo.JSON_DATA_ERROR);
        return new CustomerResponseDTO(alienBlackResponseDTO, CustomerResponseDTO.StatusEnum.INVALID, alienBlackResponseDTO.getCode());
    }

    @Override
    public CustomerResponseDTO bizErrorResponse(Exception e) {
        return fallbackResponse(e);
    }

    @Override
    public CustomerResponseDTO fallbackResponse(Exception e) {
        AlienBlackResponseDTO alienBlackResponseDTO = new AlienBlackResponseDTO();
        alienBlackResponseDTO.failed(MarketingErrorInfo.UNKNOWN_ERROR);
        return new CustomerResponseDTO(alienBlackResponseDTO, CustomerResponseDTO.StatusEnum.INVALID, alienBlackResponseDTO.getCode());
    }

    /**
     * 入库异常默认成功响应
     *
     * @return {@link CustomerResponseDTO }
     * @author senyang.zheng
     * @date 2024/10/30
     */
    @Override
    public CustomerResponseDTO defaultSuccessResponse() {
        AlienBlackResponseDTO alienBlackResponseDTO = new AlienBlackResponseDTO();
        alienBlackResponseDTO.success();
        return new CustomerResponseDTO(alienBlackResponseDTO, CustomerResponseDTO.StatusEnum.INVALID, alienBlackResponseDTO.getCode());
    }

}
