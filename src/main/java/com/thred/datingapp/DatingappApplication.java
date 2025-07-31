package com.thred.datingapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DatingappApplication {

	public static void main(String[] args) {
		SpringApplication.run(DatingappApplication.class, args);

	}
}
