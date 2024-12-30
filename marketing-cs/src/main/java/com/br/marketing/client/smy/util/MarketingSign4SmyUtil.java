package com.br.marketing.client.smy.util;

import cn.hutool.core.bean.BeanUtil;
import com.br.common.log.AlertLog;
import com.br.marketing.client.smy.input.SmyCommReqDto;
import com.br.marketing.client.smy.output.SmyCommRespDto;
import com.br.marketing.common.enums.AlarmSendCodeEnum;
import com.br.marketing.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.pulsar.shade.org.apache.avro.data.Json;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @Description 萨摩耶 client
 * @Author bin.li1
 * @CreateTime 2024-12-19
 */
@Slf4j
public class MarketingSign4SmyUtil {
    private static final String PATTERN = "-";
    /**
     * 签名，加密萨摩耶请求参数
     * @param PARTNERS_PRIVATE_KEY 请求方签名私钥
     * @param SMY_PUBLIC_KEY 萨摩耶加密公钥
     * @param smyCommReqDto
     * @throws Exception
     */
    public static boolean signSmyRequest(String PARTNERS_PRIVATE_KEY, String SMY_PUBLIC_KEY, SmyCommReqDto smyCommReqDto) {
        Assert.isTrue(StringUtils.isNotEmpty(PARTNERS_PRIVATE_KEY),"请求方签名私钥不能为空");
        Assert.isTrue(StringUtils.isNotEmpty(SMY_PUBLIC_KEY),"萨摩耶加密公钥不能为空");
        Assert.isTrue(StringUtils.isNotEmpty(smyCommReqDto.getBizContent()),"bizContent not allowed null");
        Assert.isTrue(StringUtils.isNotEmpty(smyCommReqDto.getMerchantNo()),"merchantNo not allowed null");
        Assert.isTrue(StringUtils.isNotEmpty(smyCommReqDto.getVersion()),"version not allowed null");
        if(StringUtils.isEmpty(smyCommReqDto.getTimestamp())){
            smyCommReqDto.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        }
        if(StringUtils.isEmpty(smyCommReqDto.getReqSeqNumber())){
            smyCommReqDto.setReqSeqNumber(UUID.randomUUID().toString().replaceAll(PATTERN, ""));
        }
        String secretKey = smyCommReqDto.getSecretKey();
        if(StringUtils.isEmpty(secretKey)){
            secretKey = UUID.randomUUID().toString().replaceAll(PATTERN, "").substring(0, 16);
        }
        try {
            //加密并转base64
            smyCommReqDto.setBizContent(AESUtil.strEncodBase64(secretKey,smyCommReqDto.getBizContent()));
            //rsa 公钥加密 secretKey
            smyCommReqDto.setSecretKey(RSASignUtil.encryptByPublicKey(secretKey, SMY_PUBLIC_KEY));
            //把当前对象转为Map<String,String>
            Map<String,String> contentMap = BeanUtil.beanToMap(smyCommReqDto).entrySet().stream().
                    filter(entry -> StringUtils.isNotEmpty(entry.getValue())).
                    collect(Collectors.toMap(Map.Entry::getKey, entry -> (String) entry.getValue()));
            //sign 不参与加密
            contentMap.remove("sign");
            String tobeSigned = RSASignUtil.getString(contentMap);
            String tobeSignedMd5 = DigestUtils.md5Hex(tobeSigned);
            smyCommReqDto.setSign(RSASignUtil.encryptByPrivateKey(tobeSignedMd5, PARTNERS_PRIVATE_KEY));
        } catch (Exception e) {
            log.error("萨摩耶黑名单推送请求签名异常：{}", Json.toString(smyCommReqDto));
            return false;
        }
        return true;
    }

    /**
     * 验签萨摩耶响应数据
     * @param PARTNERS_PRIVATE_KEY
     * @param SMY_PUBLIC_KEY
     * @param commRespDto
     * @return
     * @throws Exception
     */
    public static boolean verifySignSmyResponse(String PARTNERS_PRIVATE_KEY, String SMY_PUBLIC_KEY, SmyCommRespDto commRespDto)  {
        Assert.isTrue(StringUtils.isNotEmpty(PARTNERS_PRIVATE_KEY),"请求方签名私钥不能为空");
        Assert.isTrue(StringUtils.isNotEmpty(SMY_PUBLIC_KEY),"萨摩耶加密公钥不能为空");
        //把当前对象转为Map<String,String>
        Map<String,String> resultMap = BeanUtil.beanToMap(commRespDto).entrySet().stream().
                filter(entry -> StringUtils.isNotEmpty(entry.getValue())).
                collect(Collectors.toMap(Map.Entry::getKey, entry -> (String) entry.getValue()));
        resultMap.remove("sign");
        String signFromChannel = commRespDto.getSign();
        String toSigned = RSASignUtil.getString(resultMap);
        String verifyMd5 = DigestUtils.md5Hex(toSigned);
        String decryptMd5 = null;
        try{
             decryptMd5 = RSASignUtil.decryptByPublicKey(signFromChannel, SMY_PUBLIC_KEY);
            //对比MD5值,验证签名是否成功
            if (!decryptMd5.equals(verifyMd5)) {
                log.warn(AlertLog.buildErrorMessage(AlarmSendCodeEnum.SMY_SERVICEERROR.getCode(),
                        String.format("萨摩耶黑名单推送响应结果验签失败,数据md5值不一致-verifyMd5:%s;decryptMd5:%s", verifyMd5,decryptMd5)));
                return false;
            }
            String key = RSASignUtil.decryptByPrivateKey(commRespDto.getSecretKey(), PARTNERS_PRIVATE_KEY);
            String decode = AESUtil.base64StrDecode(key, commRespDto.getBizContent());
            commRespDto.setSecretKey(key);
            commRespDto.setBizContent(decode);
        }catch (Exception e){
            String message = AlertLog.buildErrorMessage(AlarmSendCodeEnum.SMY_SERVICEERROR.getCode(),
                    String.format("萨摩耶黑名单推送响应结果验签异常-verifyMd5:%s;decryptMd5:%s;secretKey:%s;bizContent:%s;异常:%s",
                            verifyMd5,decryptMd5,commRespDto.getSecretKey(),commRespDto.getBizContent(),e.getMessage()));
            log.error(message,e);
            return false;
        }
        return true;
    }
}
