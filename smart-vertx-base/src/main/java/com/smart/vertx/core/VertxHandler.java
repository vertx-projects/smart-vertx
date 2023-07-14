package com.smart.vertx.core;

import com.smart.vertx.VertxProperties;
import io.jaegertracing.internal.propagation.BinaryCodec;
import io.jaegertracing.internal.samplers.ConstSampler;
import io.micrometer.core.instrument.Clock;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.prometheus.client.CollectorRegistry;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.VertxOptions;
import io.vertx.micrometer.MicrometerMetricsOptions;
import io.vertx.micrometer.VertxPrometheusOptions;
import io.vertx.tracing.opentracing.OpenTracingOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.lang.NonNull;

import java.util.concurrent.TimeUnit;


/**
 * @author pengbo
 * @version V1.0
 * @Package com.smart.vertx
 * @date 2022/6/29 11:39
 */
@Slf4j
@ComponentScan("com.smart.vertx.core")
public class VertxHandler implements ApplicationContextAware {

    @Bean
    public CollectorRegistry collectorRegistry(Environment environment) {
        return CollectorRegistry.defaultRegistry;
    }

    @Bean
    @ConditionalOnProperty(prefix = "vertx", name = "metrics", havingValue = "true")
    public MicrometerMetricsOptions metrics(CollectorRegistry collectorRegistry) {
        // You could also reuse an existing registry from the Prometheus Java client:
        PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT, collectorRegistry, Clock.SYSTEM);
        return new MicrometerMetricsOptions().setPrometheusOptions(new VertxPrometheusOptions().setPublishQuantiles(true).setEnabled(true)).setEnabled(true).setJvmMetricsEnabled(true).setMicrometerRegistry(registry);
    }

    @Bean
    @ConditionalOnProperty(prefix = "vertx.opentracing", name = "enable", havingValue = "true")
    public Tracer tracer(VertxProperties vertxProperties, OpentracingProperties opentracing) {
        io.jaegertracing.Configuration config = new io.jaegertracing.Configuration(vertxProperties.getServiceName());
        io.jaegertracing.Configuration.SenderConfiguration sender = new io.jaegertracing.Configuration.SenderConfiguration();
        sender.withAgentHost(opentracing.getHost());
        sender.withAgentPort(opentracing.getPort());
        log.info("opentracing [ sender ] : {}", sender);
        config.withSampler(new io.jaegertracing.Configuration.SamplerConfiguration().withType(ConstSampler.TYPE).withParam(opentracing.getConstSampler()));
        config.withReporter(new io.jaegertracing.Configuration.ReporterConfiguration().withSender(sender).withLogSpans(true).withMaxQueueSize(opentracing.getQueenSize()).withFlushInterval(opentracing.getFlushIntervalMs()));
        config.withCodec(new io.jaegertracing.Configuration.CodecConfiguration().withBinaryCodec(Format.Builtin.BINARY, new BinaryCodec()));
        return config.getTracer();
    }

    @Bean
    @Primary
    @ConditionalOnBean({Tracer.class, MicrometerMetricsOptions.class})
    public VertxOptions vertxOptionsAll(VertxProperties config, Tracer tracer, MicrometerMetricsOptions metrics) {
        buildContext(tracer);
        return getVertxOptions(config).setTracingOptions(new OpenTracingOptions(tracer)).setMetricsOptions(metrics);
    }

    @Bean
    @ConditionalOnBean({Tracer.class})
    public VertxOptions vertxOptionsTracer(VertxProperties config, Tracer tracer) {
        buildContext(tracer);
        return getVertxOptions(config).setTracingOptions(new OpenTracingOptions(tracer));
    }


    @Bean
    @ConditionalOnBean({MicrometerMetricsOptions.class})
    public VertxOptions vertxOptionsOpentracing(VertxProperties config, MicrometerMetricsOptions metrics) {
        return getVertxOptions(config).setMetricsOptions(metrics);
    }

    @Bean
    @ConditionalOnMissingBean
    public VertxOptions vertxOptions(VertxProperties config) {
        return getVertxOptions(config);
    }

    protected VertxOptions getVertxOptions(VertxProperties config) {
        VertxOptions vertxOptions = new VertxOptions();
        vertxOptions.setEventLoopPoolSize(config.getNioThreadCount())
                .setWorkerPoolSize(config.getWorkerThreadCount())
                .setMaxEventLoopExecuteTime(config.getMaxEventLoopExecuteTime())
                .setMaxEventLoopExecuteTimeUnit(TimeUnit.MILLISECONDS);
        return vertxOptions;
    }

    protected void buildContext(Tracer tracer) {
        if (!springContext.containsBean(OpentracingContext.class.getName())) {
            ConfigurableApplicationContext context = (ConfigurableApplicationContext) springContext;
            DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) context.getBeanFactory();
            BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.rootBeanDefinition(OpentracingContext.class);
            beanDefinitionBuilder.addPropertyValue("tracer", tracer);
            //动态注入OpentracingContext
            beanFactory.registerBeanDefinition(OpentracingContext.class.getName(), beanDefinitionBuilder.getBeanDefinition());
        }
    }

    @Bean
    @ConditionalOnMissingBean
    public DeploymentOptions deploymentOptions(VertxProperties config) {
        DeploymentOptions depOptions = new DeploymentOptions();
        depOptions.setInstances(config.getVerticleCount());
        return depOptions;
    }


    private ApplicationContext springContext;

    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        this.springContext = applicationContext;
    }
}
