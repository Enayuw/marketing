package com.br.marketing.api.validator;

import com.alibaba.fastjson.JSONObject;
import com.br.marketing.common.constants.strategy.ModuleSwitch;
import com.br.marketing.common.utils.StringUtils;
import com.google.common.base.Function;
import lombok.extern.slf4j.Slf4j;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Iterator;

/**
 * LeastOneAnnotation 校验器
 */
@Slf4j
public class LeastOneValidator implements ConstraintValidator<LeastOneAnnotation, String> {
    @Override
    public void initialize(LeastOneAnnotation constraintAnnotation) {
        constraintAnnotation.message();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        log.info("从参数中获取的为---{}", value);
        if (StringUtils.isEmpty(value)) {
            return true;
        }
        return new Function<String, Boolean>() {
            @Override
            public Boolean apply(String value) {
                JSONObject jsonObject = JSONObject.parseObject(value);
                if (ModuleSwitch.OPEN.getCode().equals(jsonObject.getInteger("status"))) {
                    JSONObject json = jsonObject.fluentRemove("status");
                    log.info("参数数组---{}---",json);
                    Iterator<String> iterator = json.keySet().iterator();
                    if(iterator.hasNext()){
                        return json.getJSONArray(iterator.next()).size() !=0;
                    }
                }
                return Boolean.TRUE;
            }
        }.apply(value);
    }
}
