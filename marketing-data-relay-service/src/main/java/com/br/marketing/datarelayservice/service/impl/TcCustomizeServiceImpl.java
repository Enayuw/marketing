package com.br.marketing.datarelayservice.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.datarelayservice.service.TcCustomizeService;
import com.br.marketing.dto.tc.*;
import com.br.marketing.speedconfig.MarketingCommonConfig;
import com.br.marketing.util.tc.RSAUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.function.Consumer;

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

    /**
     * @description 数据推送
     * @param tcRequestDTO
     * @return com.br.marketing.dto.tc.TcResponseCommonDTO
     * @author hedongshuo
     * @date 2025/4/15 15:24
     **/
    @Override
    public TcResponseDTO marketDataPush(TcRequestDTO tcRequestDTO) {
        return process(tcRequestDTO, TcDataPushDto.class, this::handleMarketDataPush);
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
        return process(tcRequestDTO, TcRevokeDto.class, this::handleMarketRevoke);
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
        return process(tcRequestDTO, TcTransformNotifyDto.class, this::handleTransformNotify);
    }

    /**
     * @description 数据推送业务处理
     * @param dataPushDto
     * @return void
     * @author hedongshuo
     * @date 2025/4/16 15:37
     **/
    private void handleMarketDataPush(TcDataPushDto dataPushDto) {
        //todo
        System.out.println("正义必胜！");
    }

    /**
     * @description 撤销营销业务处理
     * @param revokeDto
     * @return void
     * @author hedongshuo
     * @date 2025/4/16 15:37
     **/
    private void handleMarketRevoke(TcRevokeDto revokeDto) {
        //todo
    }

    /**
     * @description 转化通知业务处理
     * @param transformNotifyDto
     * @return void
     * @author hedongshuo
     * @date 2025/4/16 15:38
     **/
    private void handleTransformNotify(TcTransformNotifyDto transformNotifyDto) {
        //todo
    }


    /**
     * 业务前置校验
     * @param tcRequestDTO
     * @param clazz
     * @param businessHandler
     * @return
     * @param <T>
     */
    private <T extends TcDataDto> TcResponseDTO process(TcRequestDTO tcRequestDTO, Class<T> clazz, Consumer<T> businessHandler) {
        TcResponseDTO resdto = new TcResponseDTO();
        JSONObject tcyrServerConfig = marketingCommonConfig.getTcyrServerConfig();
        //同程公钥验签
        String tcPublicKey = tcyrServerConfig.getString("tcPublicKey");
        //百融私钥加签
        String brPrivateKey = tcyrServerConfig.getString("brPrivateKey");
        try {
            //1.公共必填项校验
            if (StringUtils.isNotEmpty(tcRequestDTO.validate())) {
                return resdto.outterParamsFail(brPrivateKey, tcRequestDTO.validate());
            }
            //2.验签
            if (!RSAUtil.SignVf(tcRequestDTO, tcPublicKey)) {
                return resdto.signFail(brPrivateKey);
            }
            TcDataDto tcDataDto;
            //3.data层必填项校验
            tcDataDto = objectMapper.readValue(tcRequestDTO.getData(), clazz);
            if (StringUtils.isNotEmpty(tcDataDto.validate())) {
                return resdto.innerParamsFail(brPrivateKey, tcDataDto.validate());
            }
            //4.业务处理
            businessHandler.accept((T) tcDataDto);
        } catch (Exception e) {
            resdto.systemFail(brPrivateKey);
        }
        return resdto.success(brPrivateKey);
    }

}
