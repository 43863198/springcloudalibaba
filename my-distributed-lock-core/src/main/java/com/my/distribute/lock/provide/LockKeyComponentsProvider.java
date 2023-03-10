package com.my.distribute.lock.provide;

import com.my.distribute.lock.annotation.LockKey;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Parameter;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class LockKeyComponentsProvider {

    private ExpressionParser parser;

    public LockKeyComponentsProvider() {
        this.parser = new SpelExpressionParser();
    }

    public List<String> get(Parameter[] parameters, Object[] parameterValues) {
        return IntStream.range(0, parameters.length)
                .filter(i -> parameters[i].getAnnotation(LockKey.class) != null)
                .mapToObj(i -> getValue(parameters[i], parameterValues[i]))
                .collect(Collectors.toList());
    }

    private String getValue(Parameter parameter, Object parameterValue) {
        LockKey keyAnnotation = parameter.getAnnotation(LockKey.class);

        if (keyAnnotation.value().isEmpty()) {
            return parameterValue.toString();
        } else {
            StandardEvaluationContext context = new StandardEvaluationContext(parameterValue);
            return parser.parseExpression(keyAnnotation.value()).getValue(context).toString();
        }
    }
}
