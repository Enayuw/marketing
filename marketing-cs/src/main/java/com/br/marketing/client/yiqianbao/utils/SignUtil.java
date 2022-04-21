package com.br.marketing.client.yiqianbao.utils;

import com.br.marketing.client.haier.utils.Md5Utils;
import com.br.marketing.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SignUtil {

    public static String getSign(String data ,String salt) {
        try {
            return Md5Utils.stringToMD5(salt
                    + data +salt);
        }catch (Exception e){
            log.error("getSign error",e);
            throw new BusinessException("推送壹钱包获取sign失败");
        }
    }
}
