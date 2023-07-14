package com.smart.vertx.selector;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.lang.NonNull;

import java.lang.annotation.Annotation;

/**
 * @author pengbo
 * @version V1.0
 * @Package com.smart.vertx
 * @date 2023/7/13 17:09
 */
public abstract class BaseSelector<T extends Annotation> implements ImportSelector {
    protected abstract String[] selectImports();

    @Override
    public @NonNull String[] selectImports(@NonNull AnnotationMetadata importingClassMetadata) {
        return selectImports();
    }
}
