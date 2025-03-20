package com.br.marketing.client.carclue.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

@Data
public class HxClueCommitDTO {
    @JSONField(name = "channel_id")
    private String channelId;

    @JSONField(name = "phone")
    private String phone;

    @JSONField(name = "member")
    private String member;

    @JSONField(name = "province")
    private String province;

    @JSONField(name = "city")
    private String city;

    @JSONField(name = "city_id")
    private Integer cityId;

    @JSONField(name = "brand")
    private String brand;

    @JSONField(name = "series")
    private String series;

    @JSONField(name = "series_id")
    private int seriesId;

    @JSONField(name = "push_task")
    private String pushTask;

    @JSONField(name = "sound_url")
    private String soundUrl;

    @JSONField(name = "buy_time")
    private String buyTime;

    @JSONField(name = "assign_id")
    private String assignId;

    @JSONField(name = "sign")
    private String sign;

}
