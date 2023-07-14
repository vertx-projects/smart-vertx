package com.smart.vertx.selector;

import com.smart.vertx.annotation.EnableMongo;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.lang.NonNull;

import java.util.Map;

/**
 * @author pengbo
 * @version V1.0
 * @Package com.smart.vertx.selector
 * @date 2023/7/13 17:11
 */
public class MongoSelector extends BaseSelector<EnableMongo> implements EnvironmentAware {
    private StandardEnvironment environment;

    @Override
    public void setEnvironment(@NonNull Environment environment) {
        this.environment = (StandardEnvironment) environment;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected String[] selectImports() {
        MutablePropertySources propertySources = environment.getPropertySources();
        PropertySource<?> applicationPropertySources = propertySources.get("systemProperties");
        assert applicationPropertySources != null;
        Map<String, Object> map = (Map<String, Object>) applicationPropertySources.getSource();
        map.put("spring.autoconfigure.exclude", "org.springframework.boot.autoconfigure.mongo.MongoReactiveAutoConfiguration");
        return new String[0];
    }
}
