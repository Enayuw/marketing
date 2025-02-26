package com.br.marketing.entity.mark;

import com.br.marketing.common.utils.StringUtils;
import com.br.marketing.es.bean.MarketingCondition;

public class MarketingConditionVariant extends MarketingCondition {

    /**
     * 将String转为Double
     * @return
     */
    public Object doubleConvert() {
        if(StringUtils.isNumeric(this.getStrValue())){
            return Double.parseDouble(this.getStrValue());
        }
        return this.getStrValue();
    }
}
