
package com.github.managetech;

import org.springframework.boot.env.PropertiesPropertySourceLoader;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.util.List;


public class TestYamlInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        Resource resource = applicationContext.getResource("classpath:application.yml");
        YamlPropertySourceLoader sourceLoader = new YamlPropertySourceLoader();

        List<PropertySource<?>> yamlProperties;
        try {
            yamlProperties = sourceLoader.load("test:[" + "classpath:application.yml" + "]", resource);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (!ObjectUtils.isEmpty(yamlProperties)) {
            applicationContext.getEnvironment().getPropertySources().addFirst(yamlProperties.get(0));
        }

    }


}
