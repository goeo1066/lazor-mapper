package com.github.goeo1066.lazormapper;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.instrument.Instrumentation;

public class TimerAgent {
    private static final ElementMatcher<? super String> BYTE_BUDDY_LOGGING_FILTER;

    static {
        String filterClass = System.getProperty("bytebuddy.debug.instrumentation.for.class");
        BYTE_BUDDY_LOGGING_FILTER = filterClass != null ? s -> s.contains(filterClass) : s -> false;
    }

//    private static void mode1(Instrumentation instrumentation) {
//        new AgentBuilder.Default()
//                .type(ElementMatchers.nameStartsWith("BP"))
//                .transform((builder, type, classLoader, module) ->
//                        builder.method(ElementMatchers.any())
//                                .intercept(MethodDelegation.to(TimingInterceptor.class))
//                ).with(new AgentBuilder.Listener.Filtering(
//                        BYTE_BUDDY_LOGGING_FILTER,
//                        AgentBuilder.Listener.StreamWriting.toSystemOut()
//                )).installOn(instrumentation);
//    }

    private static void mode2(Instrumentation instrumentation) {

        new AgentBuilder.Default()
                .disableClassFormatChanges()
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                .type(ElementMatchers.any())
                //.type(ElementMatchers.nameMatches(".*doProcess.*"))
                //.type(ElementMatchers.hasSuperType(ElementMatchers.named("com.example.bytebuddylab.app.AbstractBusinessClass")))
                .transform((builder, type, classLoader, module, protectionDomain) ->
                        builder.visit(Advice.to(TimerAdvice.class).on(ElementMatchers.isMethod().and(ElementMatchers.nameMatches(".*doProcess|.*waitALittle.*"))))
                ).installOn(instrumentation);

    }

    public static void premain(String arguments,
                               Instrumentation instrumentation) {
        System.out.println(" ******* Agent starts V 4.0");
        //mode1(instrumentation);
        mode2(instrumentation);
    }
}
