package com.unomi.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import de.codecentric.boot.admin.server.config.EnableAdminServer;

@EnableAdminServer
@SpringBootApplication
public class UnomiSpringBootAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(UnomiSpringBootAdminApplication.class, args);
    }
}
