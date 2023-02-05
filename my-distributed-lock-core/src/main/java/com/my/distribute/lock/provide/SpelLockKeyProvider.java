package com.my.distribute.lock.provide;

import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.*;

@Component
public class SpelLockKeyProvider {

    private ExpressionParser parser;
    private ParameterNameDiscoverer nameDiscoverer;

    public SpelLockKeyProvider() {
        this.parser = new SpelExpressionParser();
        this.nameDiscoverer = new DefaultParameterNameDiscoverer();
    }

    public static void main(String[] args) {
        Set<String> lockKeys = new HashSet<>();
        lockKeys.add("a");
        lockKeys.add("b");
        ExpressionParser parser = new SpelExpressionParser();
        EvaluationContext context = new StandardEvaluationContext();
        context.setVariable("lockKeys",lockKeys);
        Object value = parser.parseExpression("#lockKeys")
                .getValue(context);
        System.out.println(value);


    }
    public List<String> get(String keyDefinition, Method method, Object[] parameterValues) {
        if (keyDefinition != null && !keyDefinition.isEmpty()) {
            EvaluationContext context = getContext(method, parameterValues);

            Object value = parser.parseExpression(keyDefinition)
                    .getValue(context);
            if (value instanceof Collection<?>) {
                List<String> keys = new ArrayList<>();
                for (Object object : (Collection) value) {
                    keys.add(Objects.toString(object, null));
                }

                return keys;
            } else {
                return Collections.singletonList(value.toString());
            }
        }

        return null;
    }

    private EvaluationContext getContext(Method method, Object[] parameterValues) {
        return new MethodBasedEvaluationContext(null, method, parameterValues, nameDiscoverer);
    }
}
