package com.br.marketing.api.customer.alien;

import com.br.marketing.api.customer.adapter.TransferDataAdaptee;
import com.br.marketing.api.customer.handler.CustomerDataHandler;
import com.br.marketing.api.customer.handler.CustomerHandlerEnum;
import com.br.marketing.common.constants.MarketingErrorInfo;
import com.br.marketing.dto.CustomerResponseDTO;
import com.br.marketing.dto.alien.AlienResponseDTO;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * 转化数据陌生客户处理
 *
 * @author Guo Zeqiang
 * @dateTime 2023-10-20 14:53
 */
@Service
public class AlienTransferDataServiceImpl implements CustomerDataHandler {

    @Override
    public CustomerHandlerEnum customer() {
        return CustomerHandlerEnum.T_ALIEN_DEFAULT;
    }

    @Override
    public TransferDataAdaptee parseObject(String jsonData) {
        return null;
    }

    @Override
    public CustomerResponseDTO verifyFields(TransferDataAdaptee adaptee) {
        AlienResponseDTO alienResponseDTO = new AlienResponseDTO();
        alienResponseDTO.success();
        return new CustomerResponseDTO(alienResponseDTO
                , CustomerResponseDTO.StatusEnum.VALID, alienResponseDTO.getCode());
    }

    @Override
    public int countBizDataNumber(TransferDataAdaptee adaptee, String jsonStr) {
        return countBizDataNumber(jsonStr);
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
