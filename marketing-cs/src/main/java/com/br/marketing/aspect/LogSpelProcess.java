package com.br.marketing.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.expression.EvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 对日志进行SPEL处理
 *
 * @author kongbx
 * @date 2024/4/17
 */
@Slf4j
@Component
public class LogSpelProcess {

    private static final Pattern PATTERN_ATTRIBUTE = Pattern.compile("\\{(.*?)}");

    private final OperateLogExpressionEvaluator cachedExpressionEvaluator = new OperateLogExpressionEvaluator();

    /**
     * key 为字段未SPEL解析属性, value为已解析的属性，如果解析失败依旧是未解析属性
     * <p>
     * 这里巧妙在 spelValues.put(tpl, tpl);后期如果解析失败那获取的是未解析成功或者不需要解析的属性
     *
     * @param templates 需要解析的属性
     * @param joinPoint 切面
     * @return 已经解析过的map
     */
    public HashMap<String, String> processBeforeExec(List<String> templates, ProceedingJoinPoint joinPoint) {
        // 获取切点方法上的注解
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        Object[] args = joinPoint.getArgs();
        Object target = joinPoint.getTarget();
        Class<?> targetClass = AopUtils.getTargetClass(target);
        HashMap<String, String> map = new HashMap<>();
        AnnotatedElementKey elementKey = new AnnotatedElementKey(method, targetClass);
        EvaluationContext evaluationContext = cachedExpressionEvaluator.createEvaluationContext(targetClass, method, args);
        for (String template : templates) {
            map.put(template, template);
            // 定义规则：如果存在{,就代表需要处理属性
            String str = map.get(template);
            if (str.contains("{")) {
                Matcher matcher = PATTERN_ATTRIBUTE.matcher(str);
                StringBuffer bufferStr = new StringBuffer();
                while (matcher.find()) {
                    String paramName = matcher.group(1);
                    Object value = cachedExpressionEvaluator.parseExpression(paramName, elementKey, evaluationContext);
                    matcher.appendReplacement(bufferStr, value.toString());
                }
                matcher.appendTail(bufferStr);
                map.put(template, bufferStr.toString());
            }
        }
        return map;
    }

}
