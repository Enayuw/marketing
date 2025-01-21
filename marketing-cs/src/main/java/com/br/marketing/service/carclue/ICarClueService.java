package com.br.marketing.service.carclue;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.dto.HxClueCallBackReqDTO;

public interface ICarClueService {

    Result callBackClue(HxClueCallBackReqDTO reqDTO);
}
