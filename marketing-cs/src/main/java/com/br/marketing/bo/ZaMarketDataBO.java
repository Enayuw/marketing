package com.br.marketing.bo;

import com.br.marketing.client.zhongan.input.ZaMarketDataDTO;
import com.br.marketing.entity.ZhonganRosterLockingData;

import java.util.List;
import lombok.Data;

/**
 * 重试功能封装属性
 *
 * @author Guo Zeqiang
 * @dateTime 2022/11/16 17:47
 */
@Data
public class ZaMarketDataBO {
    private ZaMarketDataDTO dataDTO;
    private String apiCode;
    private String tag;
    private List<ZhonganRosterLockingData> list;
    private List<Long> ids;
    private List<Long> smsIds;

    public ZaMarketDataBO(ZaMarketDataDTO dataDTO, String apiCode, String tag) {
        this.dataDTO = dataDTO;
        this.apiCode = apiCode;
        this.tag = tag;
    }


    public ZaMarketDataBO(ZaMarketDataDTO dataDTO, String apiCode, String tag, List<Long> ids) {
        this.dataDTO = dataDTO;
        this.apiCode = apiCode;
        this.tag = tag;
        this.ids = ids;
    }

    public ZaMarketDataBO(ZaMarketDataDTO dataDTO, String apiCode, String tag, List<Long> ids, List<Long> smsIds) {
        this.dataDTO = dataDTO;
        this.apiCode = apiCode;
        this.tag = tag;
        this.ids = ids;
        this.smsIds = smsIds;
    }

    public ZaMarketDataBO() {
    }
}
