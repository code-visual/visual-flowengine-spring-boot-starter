package com.github.eyesli.groovy;

import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.groovy.sandbox.GroovyInterceptor;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@Slf4j
public class GroovyNotSupportInterceptor extends GroovyInterceptor {


    private final List<String> defaultMethodBlacklist = Arrays.asList("getClass", "class", "wait", "notify",
            "notifyAll", "invokeMethod", "finalize", "execute");


     static final Map<Class<?>, String> STATIC_METHOD_BLACK_LIST = Stream.of(
                    new Pair<>(System.class, "exit,gc"),
                    new Pair<>(Class.class, "forName"),
                    new Pair<>(Runtime.class, ""))
            .collect(Collectors.toMap(Pair::getKey, Pair::getValue));


    /**
     * 静态方法拦截
     */
    @Override
    public Object onStaticCall(GroovyInterceptor.Invoker invoker, Class receiver,
                               String method, Object... args) throws Throwable {

        log.info("GroovyInterceptor:静态方法拦截类名->{},方法->{},参数->{}", receiver.getSimpleName(), method, args);
        String staticMethodBlackList = STATIC_METHOD_BLACK_LIST.get(receiver);
        if (staticMethodBlackList != null) {

            if (staticMethodBlackList.isEmpty()) {
                throw new SecurityException("Not support method: " + method);
            }
            String[] split = staticMethodBlackList.split(",");
            boolean allMatch = Arrays.stream(split).anyMatch(method::equalsIgnoreCase);
            if (allMatch) {
                throw new SecurityException("Not support method: " + method);
            }
        }
        return super.onStaticCall(invoker, receiver, method, args);
    }

    /**
     * 普通方法拦截
     */
    @Override
    public Object onMethodCall(GroovyInterceptor.Invoker invoker, Object receiver, String method, Object... args)
            throws Throwable {
        log.info("GroovyInterceptor:普通方法拦截->{},方法->{},参数->{}", receiver.getClass().getSimpleName(), method, args);
        if (defaultMethodBlacklist.contains(method)) {
            // 方法列表黑名单
            throw new SecurityException("Not support method: " + method);
        }
        return super.onMethodCall(invoker, receiver, method, args);
    }

}