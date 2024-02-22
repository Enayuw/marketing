package com.br.marketing.util;

import com.br.common.encryption.Sha256Util;
import com.br.common.util.BrCipherMaker;
import com.br.marketing.common.commondto.Result;
import com.br.marketing.common.commondto.ResultCode;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.common.validators.user.UserValidator;
import com.br.marketing.enums.ThreeKeyEncryptEnum;
import com.br.marketing.enums.ThreeKeyTypeEnum;
import com.br.marketing.rpcclient.RpcClientProxy;
import com.br.marketing.rpcclient.rpcclientImpl.DecodeGrpcClient;
import org.springframework.util.DigestUtils;

public class EncAndDecUtil {


    /**
     * 支持MD5和sha256解密转log
     * @param content 密文
     * @param dataType 数据类型
     * @param isCheck  非md5，非sha256 的文本是否校验 true校验，false不校验
     * @return
     */
    public static Result<String> digestToLog(String content, ThreeKeyTypeEnum dataType,Boolean isCheck) {
        if (StringUtils.isBlank(content) || dataType == null) {
            throw new NullPointerException("content或者dataType为null");
        }
        if (DecodeGrpcClient.isMd5(content)) {
            return digestToLog(content, dataType, ThreeKeyEncryptEnum.md5);
        } else if (64 == content.length()) {
            return digestToLog(content, dataType, ThreeKeyEncryptEnum.sha256);
        }

        Boolean valid = Boolean.TRUE;
        if(isCheck){
            UserValidator userValidator = new UserValidator(0);
            if (ThreeKeyTypeEnum.ID.equals(dataType)) {
                valid = userValidator.validateId(content);
            } else if (ThreeKeyTypeEnum.NAME.equals(dataType)) {
                valid = userValidator.validateName(content);
            } else if (ThreeKeyTypeEnum.CELL.equals(dataType)) {
                valid = userValidator.validatePhone(content);
            }
        }
        if(valid){
            return new Result<>().setCode(ResultCode.SUCCESS.getValue()).setDate(BrCipherMaker.getInstance().encode(content));
        }else{
            return new Result<>().setCode(ResultCode.FAIL.getValue()).setMessage("校验不通过");
        }
    }

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
