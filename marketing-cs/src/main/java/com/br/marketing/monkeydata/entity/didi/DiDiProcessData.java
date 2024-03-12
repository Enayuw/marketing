package com.br.marketing.monkeydata.entity.didi;

import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.entity.DidiData;
import com.br.marketing.enums.DiDiAllowMarketingEnum;
import lombok.Data;
import org.springframework.beans.BeanUtils;

@Data
public class DiDiProcessData  extends DidiData {

    public DiDiProcessData(Integer dataStatus,String pushDate,DidiData data){
        BeanUtils.copyProperties(data,this);
        this.dataStatus = dataStatus;
        if(dataStatus ==1){
            this.setStatus(3);
        }else if(dataStatus ==2 && StringUtils.isNotBlank(pushDate)){
            this.setPushDate(pushDate);
            this.setIsMarketing(DiDiAllowMarketingEnum.YES.getValue());
            this.setStatus(4);
        }

    }

    /**
     * 数据状态 1-重复数据；2-有效期数据；3-已准入；4-未准入；5-接口调用失败
     */
    private Integer dataStatus;
}
