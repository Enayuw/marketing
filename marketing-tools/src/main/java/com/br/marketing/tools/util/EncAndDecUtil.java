package com.br.marketing.tools.util;

import com.br.common.encryption.Sha256Util;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.common.validators.user.UserValidator;
import org.springframework.util.DigestUtils;

public class EncAndDecUtil {
    public static String logTodigest(String content, ThreeKeyEncryptEnum encType) {
        if (StringUtils.isBlank(content) || encType == null) {
            throw new NullPointerException("content或者encType为null");
        }
        String decode = BrCipherMaker.getInstance().decode(content);
        String res = "";
        if (ThreeKeyEncryptEnum.md5.getCode().equals(encType.getCode())) {
            res = DigestUtils.md5DigestAsHex(decode.getBytes());
        } else if (ThreeKeyEncryptEnum.sha256.getCode().equals(encType.getCode())) {
            res = Sha256Util.getSHA256Encrypt(decode);
        }
        return res;
    }
}
