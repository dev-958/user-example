package com.builder.userexample;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class UserExampleApplication {

    public static void main(final String[] args) {
        new SpringApplicationBuilder(UserExampleApplication.class).web(WebApplicationType.SERVLET)
                .run(args);
    }
}
