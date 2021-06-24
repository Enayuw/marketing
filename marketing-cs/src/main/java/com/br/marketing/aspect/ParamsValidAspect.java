package com.br.marketing.aspect;

import com.br.marketing.common.exception.validators.ParamValidErrorException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.executable.ExecutableValidator;
import java.util.Set;

@Aspect
@Order(-998) // 异常处理之内
@Component
public class ParamsValidAspect {

    private static final Logger log = LoggerFactory.getLogger(ParamsValidAspect.class);

    @Before("execution(public com.br.marketing.common.commondto.Result com.br.marketing.service..*.*(..))")
    public void checkParam(JoinPoint jp) throws Throwable {
        final Object[] args = jp.getArgs();
        if (args.length == 0) {
            return;
        }
        final Signature signature = jp.getSignature();
        final MethodSignature methodSignature = (MethodSignature) signature;
        ExecutableValidator validator = Validation.buildDefaultValidatorFactory().getValidator().forExecutables();
        Set<ConstraintViolation<Object>> validResult = validator.validateParameters(jp.getThis(), methodSignature.getMethod(), args);
        if (!validResult.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (ConstraintViolation<Object> constraintViolation : validResult) {
                sb.append(constraintViolation.getMessage() + ",");
            }
            String result = sb.toString();
            if (result != null && result.length() > 0) {
                result = result.substring(0, result.length() - 1);
            }
            throw new ParamValidErrorException(result);
        }
    }
}
