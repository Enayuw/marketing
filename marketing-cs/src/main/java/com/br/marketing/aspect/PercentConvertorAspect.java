package com.br.marketing.aspect;

import com.br.marketing.common.annoation.DecimalFieldConvertor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Aspect
@Component
public class PercentConvertorAspect {

    @Around("@within(com.br.marketing.common.annoation.PercentConvertor)")
    public Object convert(ProceedingJoinPoint jp) throws Throwable {
        List dtos = (List<Class>) jp.proceed();
        dtos.forEach((Object dto) ->{
            Class<?> aClass = dto.getClass();
            Field[] fields = aClass.getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];
                DecimalFieldConvertor convertor = field.getAnnotation(DecimalFieldConvertor.class);
                if (null == convertor) {
                    continue;
                }
                int scale = convertor.scale();
                RoundingMode roundingMode = convertor.roundingMode();
                boolean isPercent = convertor.isPercent();
                field.setAccessible(true);
                Class<?> type = field.getType();
                if (type == BigDecimal.class) {
                    try {
                        BigDecimal value = (BigDecimal)field.get(dto);
                        if (null == value) {
                            value = BigDecimal.ZERO;
                        }
                        if (isPercent) {
                            value = value.multiply(new BigDecimal(100)).setScale(scale, roundingMode);
                        }else{
                            value = value.setScale(scale, roundingMode);
                        }
                        field.set(dto, value);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
        return dtos;
    }
}
