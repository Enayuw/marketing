package com.br.marketing.service;

import com.br.marketing.common.commondto.Result;
import com.br.marketing.dto.TxtToDbDTO;

/**
 * @author guangchao.zhang
 * @Classname CallingToDbService
 * @Description 首次拨打入库信息
 * @Date 2022/2/14 6:21 PM
 */
public interface CallingToDbService {

    Result callingToDb(TxtToDbDTO content);
}
