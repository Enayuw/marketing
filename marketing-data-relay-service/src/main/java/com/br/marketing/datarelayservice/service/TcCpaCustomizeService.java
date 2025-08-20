package com.br.marketing.datarelayservice.service;


import com.br.marketing.dto.tc.TcRequestDTO;
import com.br.marketing.dto.tc.TcResponseDTO;

/**
 * 同程易融接口
 */
public interface TcCpaCustomizeService {

    TcResponseDTO marketDataPush(TcRequestDTO tcRequestDTO, String apiCode);

    TcResponseDTO marketRevoke(TcRequestDTO tcRequestDTO, String apiCode);

    TcResponseDTO transformNotify(TcRequestDTO tcRequestDTO, String apiCode);

    TcResponseDTO sampleDataPush(TcRequestDTO tcRequestDTO, String apiCode);

    TcResponseDTO marketFailDataPush(TcRequestDTO tcRequestDTO, String apiCode);
}
