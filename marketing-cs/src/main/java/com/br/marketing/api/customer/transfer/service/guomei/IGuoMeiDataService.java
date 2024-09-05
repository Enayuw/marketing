package com.br.marketing.api.customer.transfer.service.guomei;

import com.br.marketing.entity.GuoMeiTransferData;

/**
 * 国美数据入库
 *
 * @author Guo Zeqiang
 * @dateTime 2023/10/16 16:24
 */
public interface IGuoMeiDataService {


    /**
     * 国美（客户订制）转化数据入库
     *
     * @param guoMeiTransferData 转化数据
     * @return ResponseGuMeDTO
     * @author Guo Zeqiang
     */
    int saveTransferDataHandler(GuoMeiTransferData guoMeiTransferData);


}
