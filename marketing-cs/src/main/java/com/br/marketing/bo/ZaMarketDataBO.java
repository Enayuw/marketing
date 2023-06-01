package com.br.marketing.bo;

import com.br.marketing.client.zhongan.input.ZaMarketDataDTO;
import com.br.marketing.entity.ZhonganRosterLockingData;

import java.util.List;

/**
 * 重试功能封装属性
 *
 * @author Guo Zeqiang
 * @dateTime 2022/11/16 17:47
 */
public class ZaMarketDataBO {
    private ZaMarketDataDTO dataDTO;
    private String apiCode;
    private String tag;
    private List<ZhonganRosterLockingData> list;
    private List<Long> ids;

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

    public ZaMarketDataBO() {
    }

    public ZaMarketDataDTO getDataDTO() {
        return dataDTO;
    }

    public void setDataDTO(ZaMarketDataDTO dataDTO) {
        this.dataDTO = dataDTO;
    }

    public String getApiCode() {
        return apiCode;
    }

    public void setApiCode(String apiCode) {
        this.apiCode = apiCode;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public List<ZhonganRosterLockingData> getList() {
        return list;
    }

    public void setList(List<ZhonganRosterLockingData> list) {
        this.list = list;
    }

    public List<Long> getIds() {
        return ids;
    }

    public void setIds(List<Long> ids) {
        this.ids = ids;
    }
}
