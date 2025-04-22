package com.br.marketing.datarelayservice.service;


import com.br.marketing.dto.tc.TcRequestDTO;
import com.br.marketing.dto.tc.TcResponseDTO;

/**
 * 同程易融接口
 */
public interface TcCustomizeService {

    TcResponseDTO marketDataPush(TcRequestDTO tcRequestDTO);

    TcResponseDTO marketRevoke(TcRequestDTO tcRequestDTO);

    TcResponseDTO transformNotify(TcRequestDTO tcRequestDTO);
}
