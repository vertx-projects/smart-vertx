package com.smart.vertx.servicediscovery;

import ch.qos.logback.core.util.Loader;
import com.smart.vertx.annotation.WebClient;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.lang.NonNull;

import java.io.IOException;
import java.util.Locale;
import java.util.Objects;

/**
 * @author peng.bo
 * @date 2022/5/31 12:39
 * @desc
 */
@ComponentScan("com.smart.vertx.servicediscovery")
public class DiscoveryHandler implements ResourceLoaderAware {
    ResourceLoader resourceLoader;

    @Bean
    public BeanDefinitionRegistryPostProcessor vertxDefinitionRegistryPostProcessor() {
        return new BeanDefinitionRegistryPostProcessor() {
            @Override
            public void postProcessBeanDefinitionRegistry(@NonNull BeanDefinitionRegistry registry) throws BeansException {
                ResourcePatternResolver resolver = ResourcePatternUtils.getResourcePatternResolver(resourceLoader);
                MetadataReaderFactory metaReader = new CachingMetadataReaderFactory();
                try {
                    Resource[] resources = resolver.getResources("classpath*:".concat("com/smart/**/*.class"));
                    for (Resource resource : resources) {
                        MetadataReader reader = metaReader.getMetadataReader(resource);
                        String className = reader.getClassMetadata().getClassName();
                        Class<?> cls = Loader.loadClass(className);
                        WebClient webClient = cls.getAnnotation(WebClient.class);
                        if (Objects.nonNull(webClient)) {
                            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(cls);
                            GenericBeanDefinition definition = (GenericBeanDefinition) builder.getRawBeanDefinition();
                            definition.getPropertyValues().add("superclass", definition.getBeanClassName());
                            definition.setBeanClass(WebClientProxyFactory.class);
                            definition.setAutowireMode(GenericBeanDefinition.AUTOWIRE_BY_TYPE);
                            String beanName = cls.getSimpleName().toLowerCase(Locale.ROOT) + "_webClient";
                            registry.registerBeanDefinition(beanName, definition);
                        }
                    }
                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void postProcessBeanFactory(@NonNull ConfigurableListableBeanFactory beanFactory) throws BeansException {

            }
        };
    }

    @Override
    public void setResourceLoader(@NonNull ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }
}
