package com.github.managetech;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;

@ContextConfiguration(initializers = TestYamlInitializer.class)
@Configuration
@ComponentScan(value = {"com.github.managetech"})
public abstract class BaseTest extends AbstractTestNGSpringContextTests {

}