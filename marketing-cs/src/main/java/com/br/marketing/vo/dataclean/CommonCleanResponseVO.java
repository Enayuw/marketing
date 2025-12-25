package com.br.marketing.vo.dataclean;

import com.br.marketing.common.constants.auth.CodeEnum;
import lombok.Data;

/**
 * @ClassName CommonCleanResponseVO
 * @Author hang.zhou
 * @Date 2025/12/22
 */
@Data
public class CommonCleanResponseVO {

    String code;

    String message;

    String data;

    public CommonCleanResponseVO(CodeEnum codeEnum, String data) {
        this.code = codeEnum.getCode();
        this.message = codeEnum.getMessage();
        this.data = data;
    }
}
