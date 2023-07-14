package com.smart.vertx;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import java.util.Objects;

import static com.smart.vertx.constant.CommonConstant.*;

/**
 * @author peng.bo
 * @date 2022/6/10 10:04
 * @desc
 */
@Slf4j
public class LockYamlProcessor implements EnvironmentPostProcessor {
    private final YamlPropertySourceLoader loader = new YamlPropertySourceLoader();

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        MutablePropertySources mutablePropertySources = environment.getPropertySources();
        String[] profiles = environment.getActiveProfiles();
        String fileName = CONFIG_BOOTSTRAP;
        if (ArrayUtils.isNotEmpty(profiles)) {
            fileName = String.format(CONFIG_PROFILE, profiles[0]);
        }
        PropertySource<?> propertySource = loadYaml(new FileSystemResource(fileName), profiles);
        if (Objects.nonNull(propertySource)) {
            mutablePropertySources.addFirst(propertySource);
        }
    }

    private PropertySource<?> loadYaml(Resource path, String[] profiles) {
        if (!path.exists()) {
            log.warn("######load {} file is empty ignore this config######,", path);
            return null;
        }
        try {
            if (ArrayUtils.isNotEmpty(profiles)) {
                return this.loader.load(String.format(PROFILE, profiles[0]), path).get(0);
            }
            return this.loader.load(BOOTSTRAP, path).get(0);
        } catch (Exception e) {
            throw new RuntimeException("load file error," + e);
        }
    }
}
