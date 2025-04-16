package com.br.marketing.datarelayservice.service.impl;

import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.datarelayservice.service.TcCustomizeService;
import com.br.marketing.dto.tc.*;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.tc.RSAUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.Map;

/**
 * @description: 同程易融实现
 * @author hedongshuo
 * @date 2025/4/15 15:04
 **/
@Service
@Slf4j
public class TcCustomizeServiceImpl implements TcCustomizeService {

    @Resource
    private MarketingCommonConfig marketingCommonConfig;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final String tcPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAs9He491hp3zCI70rKQe79mXJglifGas59YLzHvfW6ljp73u1d36U2UnWVTIOwfL4iQ1La1Kg2P2UlrFKI1tDd++G842JRjWWJVpmKomXymWeOAASuvnZMuTA4Z2k6WIoZYcMYiqfD+PGsI8jMYKlsb+3VoUhIFRb4Q5CEK/V2LUH9dRC9rNTem6pGn9brxgEQG+sg8/b4eLaK86MPm5n3tB3ox9UMsOQt3s2vlhJoRXQRRHh+BoZC8Kd8EOpqKUA6cjz186zSSKO82qm2ifrPi049qvBAlfIR53cEh62otH6IJGnQDBfbYEw9lMB2QLbytCvCFtUS9BPJBSGpn5VlQIDAQAB";

    private static final String brPrivateKey = "MIIEvwIBADANBgkqhkiG9w0BAQEFAASCBKkwggSlAgEAAoIBAQDRhuOUZ4BDizyz6o247ZlTONZJWOrYXl/bc6Fsxw975S79VWy6D8BsDBrZxlHmX6TLoO3r22P7536ewcNaNDSRBJGAKP6RbGjzMPYYn0QQcugwS2cJDN712VJWCzU6gbl9kh0fJ64TNQXG8Dj951MDN1WfOpWzsGUcYXhCC+JtyVQHwAgw7fIyNLfY6k0bxL4y+USsFXvLNF/Ymz0Tk1mKgb3aH6cgLfBPtTpo5HT7IIUg1ojPWZDzqnnPn6Kf5uZJSUMdMIcPJSf4LeEKJULcTKokIO+SQf7p3sxenD4/Kseq0cW/JGI4ExTTee5yU/NNuDqnCH/R//23HNWJXqnjAgMBAAECggEBALVomoYVDsJsPURw4f+pB8U8QpwCtmJbY5c3iB4MQ2W7ynFgkuCNXsatvFmtEZ/qU15SmWxJ6Uli5whBLwhcht7AG9HgrHfwavWUAJ7U1jN/qXSW9ECruicV3+nLjmx3gMgtx7T/wAG1OKlDt7Rtojv4ntNb/90x7nkiEuLsM0FlKzt0XJR2eJiUp2St+xnRfUyskT4YJLLaPQKtEX5vKLnNPp82jxCJaT0yxHZ9GkGw8zClDnQPenNnU3N7w7d6IIgS9H+zsbXT7K/DxhWMMWaiBq9ZSWJY6HkYIKY8hk1M5qLakbLCzS3UmV1kPsXF9jzKEInZMhCxs+7FeIcvw6ECgYEA8mb7LcF4wA5FhZ0G0x/wTY5Xe7yfPQjaNEedk0Yfb5zbAU33mIGmW/2Zy75CktimgH7oJQovzaKUdhDjkQx89fQs7gnTuMJfxtUM1t0WqNb+i2j0HKPhe3/qauwedMq7HE6n+OXeG6DyinRUYqN/r0OOIHV1dMthzYggHWJygFkCgYEA3UfNEsXsXu1iBI+eW+jzUAzaBwouwk4sS92beuI2Y0BSqh6uNIoLaSjX9STkF8rrCjqTxdbgTU5aWm2gtGCq4SLl+TA8eBD+SX0SO7Ls6dP8hDNyX0pLyHprXz5wjmzZ6PRDel6p5voDslhMu8syz9WTR+pDs5vPaFC6XXukFJsCgYAGpYY7ofNyqLGFUWHvhg+rwLxrWyeun8CD8HbEpAaWo+Fpbr7cQqnSGekOqh/fMOuX2GL7KQVYiR2zAxGKV6JRiCl3OXPBvCquJAdfGN7XMFX4cp9G7cNwBHjkB6dqImjxBMMcUwk9DrO30irCLaOBpcOO5kmbMzxyS0o+JAXpuQKBgQClBcYXaQm1ZZ1Cv4SfEKRcH6l9tIdYmwoH7hXk180tyauceVL7lbOa0j3z8XY1lDwjHbpUwcH3hSZ25+kAfFMToX4Wj3WZKTsqvR6a7P4oB9L7GI8EJ5lKwplOp3czkFQWmgu0t+JHgk69c4KOTqTvQF0dNcUVrm5IYmQKEYo1XwKBgQDiK9l7mYqpg4xSDYGZ8M853aisjtbcy2hMM/sedo+9tw40MoNGCPvQEiD4OgTooINIAp1SB3FPlWZ8PxjLSMKIIywGzeCyH3VuC/LQwskWdxSFmt5vSVZh7HkHYw0HW5Jgskv7AA1S1GKGi0BujPo3CdHa9wl66r8pb5LUv92TuQ==";

    /**
     * @description 数据推送
     * @param tcRequestDTO
     * @return com.br.marketing.dto.tc.TcResponseCommonDTO
     * @author hedongshuo
     * @date 2025/4/15 15:24
     **/
    @Override
    public TcResponseDTO marketDataPush(TcRequestDTO tcRequestDTO) {
        TcResponseDTO resdto = new TcResponseDTO();
        Map<String, String> tcyrServerConfig = marketingCommonConfig.getTcyrServerConfig();
        try {
            //1.公共必填项检验
            if (StringUtils.isNotEmpty(tcRequestDTO.validate())) {
                resdto.outterParamsFail(brPrivateKey, tcRequestDTO.validate());
                return resdto;
            }
            //2.验签
            if (!RSAUtil.SignVf(tcRequestDTO, tcPublicKey)) {
                resdto.signFail(brPrivateKey);
                return resdto;
            }
            TcDataPushDto tcDataPushDto;
            //3.data层必输项检验
            tcDataPushDto = objectMapper.readValue(tcRequestDTO.getData(), TcDataPushDto.class);
            if (StringUtils.isNotEmpty(tcDataPushDto.validate())) {
                resdto.innerParamsFail(brPrivateKey, tcDataPushDto.validate());
                return resdto;
            }
            //4.业务处理 todo
        } catch (Exception e) {
            resdto.systemFail(brPrivateKey);
            return resdto;
        }
        resdto.success(brPrivateKey);
        return resdto;
    }

    /**
     * @description 撤销营销
     * @param tcRequestDTO
     * @return com.br.marketing.dto.tc.TcResponseDTO
     * @author hedongshuo
     * @date 2025/4/16 10:20
     **/
    @Override
    public TcResponseDTO marketRevoke(TcRequestDTO tcRequestDTO) {
        TcResponseDTO resdto = new TcResponseDTO();
        Map<String, String> tcyrServerConfig = marketingCommonConfig.getTcyrServerConfig();
        try {
            //1.公共必填项检验
            if (StringUtils.isNotEmpty(tcRequestDTO.validate())) {
                resdto.outterParamsFail(brPrivateKey, tcRequestDTO.validate());
                return resdto;
            }
            //2.验签
            if (!RSAUtil.SignVf(tcRequestDTO, tcPublicKey)) {
                resdto.signFail(brPrivateKey);
                return resdto;
            }
            TcRevokeDto tcRevokeDto;
            //3.data层必输项检验
            tcRevokeDto = objectMapper.readValue(tcRequestDTO.getData(), TcRevokeDto.class);
            if (StringUtils.isNotEmpty(tcRevokeDto.validate())) {
                resdto.innerParamsFail(brPrivateKey, tcRevokeDto.validate());
                return resdto;
            }
            //4.业务处理 todo
        } catch (Exception e) {
            resdto.systemFail(brPrivateKey);
            return resdto;
        }
        resdto.success(brPrivateKey);
        return resdto;
    }

    /**
     * @description 转化通知
     * @param tcRequestDTO
     * @return com.br.marketing.dto.tc.TcResponseDTO
     * @author hedongshuo
     * @date 2025/4/16 11:30
     **/
    @Override
    public TcResponseDTO transformNotify(TcRequestDTO tcRequestDTO) {
        TcResponseDTO resdto = new TcResponseDTO();
        Map<String, String> tcyrServerConfig = marketingCommonConfig.getTcyrServerConfig();
        try {
            //1.公共必填项检验
            if (StringUtils.isNotEmpty(tcRequestDTO.validate())) {
                resdto.outterParamsFail(brPrivateKey, tcRequestDTO.validate());
                return resdto;
            }
            //2.验签
            if (!RSAUtil.SignVf(tcRequestDTO, tcPublicKey)) {
                resdto.signFail(brPrivateKey);
                return resdto;
            }
            TcTransformNotifyDto tcTransformNotifyDto;
            //3.data层必输项检验
            tcTransformNotifyDto = objectMapper.readValue(tcRequestDTO.getData(), TcTransformNotifyDto.class);
            if (StringUtils.isNotEmpty(tcTransformNotifyDto.validate())) {
                resdto.innerParamsFail(brPrivateKey, tcTransformNotifyDto.validate());
                return resdto;
            }
            //4.业务处理 todo
        } catch (Exception e) {
            resdto.systemFail(brPrivateKey);
            return resdto;
        }
        resdto.success(brPrivateKey);
        return resdto;
    }

}
