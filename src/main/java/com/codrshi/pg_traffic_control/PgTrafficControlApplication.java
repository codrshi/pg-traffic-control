package com.codrshi.pg_traffic_control;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class PgTrafficControlApplication {

	public static void main(String[] args) {
		SpringApplication.run(PgTrafficControlApplication.class, args);
	}

}
