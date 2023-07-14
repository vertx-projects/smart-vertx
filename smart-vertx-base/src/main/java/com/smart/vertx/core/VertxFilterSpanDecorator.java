package com.smart.vertx.core;

import com.google.common.collect.Maps;
import io.opentracing.Span;
import io.opentracing.tag.Tags;
import io.vertx.rxjava3.core.http.HttpServerRequest;
import io.vertx.rxjava3.core.http.HttpServerResponse;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.Objects;

/**
 * @author pengbo
 * @version V1.0
 * @Package com.smart.vertx.handler
 * @date 2022/6/21 17:56
 */
public interface VertxFilterSpanDecorator {
    VertxFilterSpanDecorator STANDARD_TAGS = new VertxFilterSpanDecorator() {
        @Override
        public void onRequest(HttpServerRequest request, Span span, String type) {
            Tags.COMPONENT.set(span, "vertx-".concat(type));
            Tags.HTTP_METHOD.set(span, request.method().name());
            Tags.HTTP_URL.set(span, request.path());
        }

        @Override
        public void onResponse(HttpServerRequest request, HttpServerResponse response, Span span) {
            if (Objects.nonNull(span)) {
                Tags.HTTP_STATUS.set(span, response.getStatusCode());
                span.finish();
            }
        }

        @Override
        public void onError(HttpServerRequest request, HttpServerResponse response, Throwable exception, Span span) {
            if (Objects.nonNull(span)) {
                Tags.ERROR.set(span, Boolean.TRUE);
                span.log(this.logsForException(exception));
                if (response.getStatusCode() == 200) {
                    Tags.HTTP_STATUS.set(span, 500);
                }
            }
        }

        private Map<String, String> logsForException(Throwable throwable) {
            Map<String, String> errorLog = Maps.newHashMapWithExpectedSize(3);
            errorLog.put("event", Tags.ERROR.getKey());
            String message = throwable.getCause() != null ? throwable.getCause().getMessage() : throwable.getMessage();
            if (message != null) {
                errorLog.put("message", message);
            }

            StringWriter sw = new StringWriter();
            throwable.printStackTrace(new PrintWriter(sw));
            errorLog.put("stack", sw.toString());
            return errorLog;
        }
    };

    void onRequest(HttpServerRequest request, Span span, String type);

    void onResponse(HttpServerRequest request, HttpServerResponse response, Span span);

    void onError(HttpServerRequest request, HttpServerResponse response, Throwable exception, Span span);
}
