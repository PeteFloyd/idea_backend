package com.learn.demo.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootTest(classes = {WebConfig.class, FileStorageConfig.class})
class WebConfigTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void webConfigBeanLoads() {
        WebConfig webConfig = applicationContext.getBean(WebConfig.class);
        assertNotNull(webConfig);
        assertTrue(webConfig instanceof WebMvcConfigurer);
    }
}
