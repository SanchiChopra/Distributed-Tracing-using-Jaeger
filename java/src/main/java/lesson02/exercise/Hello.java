package lesson02.exercise;

import com.google.common.collect.ImmutableMap;

import io.jaegertracing.internal.JaegerTracer;
import io.opentracing.Span;
import io.opentracing.Tracer;
import lib.Tracing;

public class Hello {

    private final Tracer tracer;

    private Hello(Tracer tracer) {
        this.tracer = tracer;
    }

    private void sayHello(String helloTo) {
        Span span = tracer.buildSpan("say-hello").start();
        span.setTag("hello-to", helloTo);

        String helloStr = formatString(span, helloTo);

        span.log(ImmutableMap.of("event", "string-format", "value", helloStr));
        printHello(span, helloStr);
        span.log(ImmutableMap.of("event", "println"));

        span.finish();
    }

    private  String formatString(Span rootSpan, String helloTo) {
        Span span = tracer.buildSpan("formatString").asChildOf(rootSpan).start();
        try {
            String helloStr = String.format("Hello, %s!", helloTo);
            span.log(ImmutableMap.of("event", "string-format", "value", helloStr));
            return helloStr;
        } finally {
            span.finish();
        }
    }

    private void printHello(Span rootSpan, String helloStr) {
        Span span = tracer.buildSpan("printHello").asChildOf(rootSpan).start();
        try {
            System.out.println(helloStr);
            span.log(ImmutableMap.of("event", "println"));
        } finally {
            span.finish();
        }
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            throw new IllegalArgumentException("Expecting one argument");
        }

        String helloTo = args[0];
        try (JaegerTracer tracer = Tracing.init("hello-world")) {
            new Hello(tracer).sayHello(helloTo);
        }
    }
}