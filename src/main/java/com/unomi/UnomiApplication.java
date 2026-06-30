package com.unomi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class UnomiApplication {

	public static void main(String[] args) {
		SpringApplication.run(UnomiApplication.class, args);
	}

}
