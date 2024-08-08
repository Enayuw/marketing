package com.br.marketing.api.customer.transfer.adapter;

import com.br.marketing.dto.TransferDataDTO;
import com.br.marketing.dto.TransferDataItemDTO;

import java.io.Serializable;

/**
 * 适配者
 *
 * @author Guo Zeqiang
 * @dateTime 2023-10-23 16:28
 */
public abstract class TransferDataAdaptee implements Serializable {

    private static final long serialVersionUID = 405278457150492618L;

    private String apiCode;
    private String jsonData;

    protected abstract TransferDataDTO<TransferDataItemDTO> adapteeRequest(String apiCode
            , TransferDataDTO<TransferDataItemDTO> transferDataDTO);

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode;
    }

    public String getJsonData() {
        return jsonData;
    }

    public void setJsonData(String jsonData) {
        this.jsonData = jsonData;
    }
}
