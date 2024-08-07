package com.br.marketing.api.customer.upload.adapter;

import com.br.marketing.dto.MarketingPreUserDTO;
import com.br.marketing.dto.TransferDataDTO;
import com.br.marketing.dto.TransferDataItemDTO;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;


/**
 * 上传数据适配器
 *
 * @author senyang.zheng
 * @date 2024/08/07
 */
@Getter
@Setter
public abstract class UploadDataAdaptee implements Serializable {


    private static final long serialVersionUID = 2295254868340373170L;
    private String apiCode;
    private String jsonData;

    protected abstract MarketingPreUserDTO adapteeRequest(String apiCode, MarketingPreUserDTO marketingPreUserDTO);

}
