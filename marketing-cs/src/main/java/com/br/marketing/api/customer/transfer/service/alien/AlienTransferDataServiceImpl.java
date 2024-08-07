package com.br.marketing.api.customer.transfer.service.alien;

import com.br.common.log.AlertLog;
import com.br.marketing.api.customer.transfer.adapter.TransferDataAdaptee;
import com.br.marketing.api.customer.transfer.handler.CustomerDataHandler;
import com.br.marketing.api.customer.transfer.handler.CustomerHandlerEnum;
import com.br.marketing.api.customer.transfer.service.alien.dto.AlienResponseDTO;
import com.br.marketing.common.constants.MarketingErrorInfo;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.dto.CustomerResponseDTO;
import com.br.marketing.dto.TransferDataDTO;
import com.br.marketing.dto.TransferDataItemDTO;
import com.br.marketing.entity.MarketingCustomer;
import com.br.marketing.mapper.MarketingCustomerMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

/**
 * 转化数据陌生客户处理
 *
 * @author Guo Zeqiang
 * @dateTime 2023-10-20 14:53
 */
@Service
@Slf4j
public class AlienTransferDataServiceImpl implements CustomerDataHandler {

    @Resource
    private MarketingCustomerMapper marketingCustomerService;

    @Override
    public CustomerHandlerEnum customer() {
        return CustomerHandlerEnum.T_ALIEN_DEFAULT;
    }

    @Override
    public TransferDataAdaptee parseObject(String jsonData) {
        return new TransferDataAdaptee() {
            private static final long serialVersionUID = -3462531314929100259L;

            @Override
            protected TransferDataDTO<TransferDataItemDTO> adapteeRequest(String apiCode
                    , TransferDataDTO<TransferDataItemDTO> transferDataDTO) {
                return null;
            }
        };
    }

    @Override
    public CustomerResponseDTO verifyFields(TransferDataAdaptee adaptee) {
        AlienResponseDTO alienResponseDTO = new AlienResponseDTO();
        alienResponseDTO.success();
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
        String msg = AlertLog.buildWarnMessage(AlarmSendCodeEnum.EXCEPTION_USUAL_NOTICE.getCode()
                , "定制化转化接口接收到“" + shortName + "”编号“" + adaptee.getApiCode()
                        .concat("”的数据\n请及时与该“").concat(name).concat("”沟通确认^_^")
                , "通用定制化转化接口未知请求通知");
        log.warn(msg);
        return new CustomerResponseDTO(alienResponseDTO
                , CustomerResponseDTO.StatusEnum.VALID, alienResponseDTO.getCode());
    }

    @Override
    public int countBizDataNumber(TransferDataAdaptee adaptee) {
        return countBizDataNumber(adaptee.getJsonData());
    }

    @Override
    public Set<String> getBizAllFields(String jsonStr) {
        return null;
    }

    @Override
    public CustomerResponseDTO jsonErrorResponse(Exception e) {
        AlienResponseDTO alienResponseDTO = new AlienResponseDTO();
        alienResponseDTO.failed(MarketingErrorInfo.JSON_DATA_ERROR);
        return new CustomerResponseDTO(alienResponseDTO
                , CustomerResponseDTO.StatusEnum.INVALID, alienResponseDTO.getCode());
    }

    @Override
    public CustomerResponseDTO bizErrorResponse(Exception e) {
        return fallbackResponse(e);
    }

    @Override
    public CustomerResponseDTO fallbackResponse(Exception e) {
        AlienResponseDTO alienResponseDTO = new AlienResponseDTO();
        alienResponseDTO.failed(MarketingErrorInfo.UNKNOWN_ERROR);
        return new CustomerResponseDTO(alienResponseDTO
                , CustomerResponseDTO.StatusEnum.INVALID, alienResponseDTO.getCode());
    }
}
