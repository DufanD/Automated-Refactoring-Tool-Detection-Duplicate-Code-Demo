package com.finalproject.automated.refactoring.tool;

import com.finalproject.automated.refactoring.tool.demo.service.Demo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Arrays;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(DemoApplication.class, args);
        applicationContext.getBean(Demo.class).demo(Arrays.asList(args));
        applicationContext.close();
    }
}
