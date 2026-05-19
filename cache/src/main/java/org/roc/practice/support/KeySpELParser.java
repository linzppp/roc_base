package org.roc.practice.support;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class KeySpELParser {
    private final SpelExpressionParser parser = new SpelExpressionParser();
    private final ParameterNameDiscoverer paramNameDiscoverer = new DefaultParameterNameDiscoverer();
    private final ConcurrentHashMap<String, Expression> expressionCache = new ConcurrentHashMap<>();

    public String parse(String expression, Method method, Object[] args, Object target) {
        if (!StringUtils.hasText(expression)) {
            throw new IllegalStateException("SpEL expression must not be empty");
        }

        EvaluationContext context = buildContext(method, args, target);
        Expression expr = expressionCache.computeIfAbsent(expression, parser::parseExpression);

        Object value;
        try {
            value = expr.getValue(context);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "SpEL evaluation failed: expression=[" + expression + "], method=" + method, e);
        }

        if (value == null) {
            throw new IllegalStateException(
                    "SpEL evaluated to null: expression=[" + expression + "], method=" + method);
        }
        return String.valueOf(value);
    }

    private EvaluationContext buildContext(Method method, Object[] args, Object target) {
        StandardEvaluationContext ctx = new StandardEvaluationContext(target);

        // Spring官方约定实现, 注入 #method, #target
        ctx.setVariable("method", method);
        ctx.setVariable("target", target);

        String[] paramNames = paramNameDiscoverer.getParameterNames(method);
        if (paramNames == null) {
            // 降级：编译期未带 -parameters，用 p0/p1/...
            log.warn("Parameter names unavailable for method [{}], falling back to p0/p1/.... "
                    + "Add -parameters to compiler args to fix.", method);
            for (int i = 0; i < args.length; i++) {
                ctx.setVariable("p" + i, args[i]);
            }
        } else {
            for (int i = 0; i < paramNames.length; i++) {
                ctx.setVariable(paramNames[i], args[i]);
            }
        }
        return ctx;
    }
}
