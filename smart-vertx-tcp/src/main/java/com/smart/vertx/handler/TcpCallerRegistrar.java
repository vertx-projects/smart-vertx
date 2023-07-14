package com.smart.vertx.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.GenericTypeResolver;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import java.lang.annotation.Annotation;

/**
 * @author peng.bo
 * @date 2022/5/19 13:28
 * @desc
 */
@Slf4j
public abstract class TcpCallerRegistrar<T extends Annotation> implements ImportBeanDefinitionRegistrar {


    protected abstract void selectImports(boolean server, BeanDefinitionRegistry registry);

    protected String getEnumName() {
        return "server";
    }

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata,
                                        @NonNull BeanDefinitionRegistry registry) {
        Class<T> annotationClass = (Class<T>) GenericTypeResolver.resolveTypeArgument(this.getClass(), TcpCallerRegistrar.class);
        Assert.state(annotationClass != null, "Unresolvable type argument for MessageCallerSelector");
        AnnotationAttributes attributes = AnnotationAttributes.fromMap(metadata.getAnnotationAttributes(annotationClass.getName(), true));

        if (attributes == null) {
            log.error("get attributes is null");
        } else {
            boolean server = attributes.getBoolean(getEnumName());
            selectImports(server, registry);
        }
    }
}
