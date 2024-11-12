package com.br.marketing.dto.qifu;

import com.br.marketing.entity.MarketingSyncUser;
import lombok.Data;

import java.util.List;

@Data
public class QiFuCuWanJianBatQryUserRealParamsDto {

    private String apiCode;
    private String taskId;
    private List<MarketingSyncUser> partition;
}