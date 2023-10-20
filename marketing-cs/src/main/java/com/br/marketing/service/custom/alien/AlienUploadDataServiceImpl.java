package com.br.marketing.service.custom.alien;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.dto.ResponseCustomDTO;
import com.br.marketing.service.custom.handler.CustomCodeEnum;
import com.br.marketing.service.custom.handler.CustomDataHandler;

/**
 * 上传数据陌生客户处理
 *
 * @author Guo Zeqiang
 * @dateTime 2023-10-20 14:53
 */
public class AlienUploadDataServiceImpl implements CustomDataHandler {
    @Override
    public CustomCodeEnum custom() {
        return CustomCodeEnum.U_ALIEN_DEFAULT;
    }

    @Override
    public ResponseCustomDTO receiveCustomDataHandler(String apiCode, String jsonData) {
        return null;
    }

    @Override
    public Result<Boolean> consumerPayData(String msg) {
        return null;
    }
}
