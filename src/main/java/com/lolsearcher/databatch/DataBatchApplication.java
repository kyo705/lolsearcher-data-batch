package com.lolsearcher.databatch;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableBatchProcessing
@SpringBootApplication
public class DataBatchApplication {

	public static void main(String[] args) {
		SpringApplication.run(DataBatchApplication.class, args);
	}

}
