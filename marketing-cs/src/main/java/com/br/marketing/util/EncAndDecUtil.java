package com.br.marketing.util;

import com.br.common.encryption.Sha256Util;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.enums.ThreeKeyEncryptEnum;
import com.br.marketing.enums.ThreeKeyTypeEnum;
import com.br.marketing.rpcclient.RpcClientProxy;
import org.springframework.util.DigestUtils;

public class EncAndDecUtil {


    public static Result<String> digestToLog(String content, ThreeKeyTypeEnum dataType, ThreeKeyEncryptEnum encType) {
        if (StringUtils.isBlank(content) || dataType == null || encType == null) {
            throw new NullPointerException("content或者dataType或者encType为null");
        }
        String decode = RpcClientProxy.decode(content, dataType.getValue(), encType.getValue(), "");
        if (StringUtils.isBlank(decode)) {
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage("摘要算法解密失败");
        }
        return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(BrCipherMaker.getInstance().encode(decode));
    }


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
