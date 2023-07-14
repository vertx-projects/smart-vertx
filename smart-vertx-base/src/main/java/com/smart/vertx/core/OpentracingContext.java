package com.smart.vertx.core;

import io.opentracing.Span;
import io.opentracing.Tracer;
import io.opentracing.tag.Tags;
import io.vertx.rxjava3.core.http.HttpServerRequest;
import io.vertx.rxjava3.ext.web.RoutingContext;
import io.vertx.tracing.opentracing.OpenTracingUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;


/**
 * @author pengbo
 * @version V1.0
 * @Package com.smart.vertx.handler
 * @date 2022/6/21 17:20
 */
@Slf4j
@Data
public class OpentracingContext {
    private Tracer tracer;

    public synchronized void request(RoutingContext s, HttpServerRequest request) {
        final Tracer.SpanBuilder spanBuilder = this.tracer
                .buildSpan(request.method().name())
                .withTag(Tags.SPAN_KIND.getKey(), Tags.SPAN_KIND_SERVER);
        Span span = spanBuilder.start();
        VertxFilterSpanDecorator.STANDARD_TAGS.onRequest(request, span, "web");
        //span交给vert.x管理(激活、赋值)
        OpenTracingUtil.setSpan(span);
    }
}
