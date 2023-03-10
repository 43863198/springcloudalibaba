package com.my.distribute.lock.provide;

import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class LockKeyProvider {
    private SpelLockKeyProvider spelLockKeyProvider;
    private LockKeyComponentsProvider lockKeyComponentsProvider;

    public LockKeyProvider(SpelLockKeyProvider spelLockKeyProvider, LockKeyComponentsProvider lockKeyComponentsProvider) {
        this.spelLockKeyProvider = spelLockKeyProvider;
        this.lockKeyComponentsProvider = lockKeyComponentsProvider;
    }

    public List<String> get(String keyDefinition, Method method, Object[] parameterValues) {
        List<String> keys = spelLockKeyProvider.get(keyDefinition, method, parameterValues);
        List<String> components = lockKeyComponentsProvider.get(method.getParameters(), parameterValues);

        return Stream.of(keys, components)
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .filter(string -> !string.isEmpty())
                .collect(Collectors.toList());
    }
}
