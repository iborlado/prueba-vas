package com.iborlado.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.client.RestTemplate;

import com.iborlado.boot.dto.Kpis;
import com.iborlado.boot.dto.Metrics;
import com.iborlado.boot.services.VasService1;

@SpringBootApplication
public class PruebaVasApplication {

	public static void main(String[] args) {
		SpringApplication.run(PruebaVasApplication.class, args);
	}
	
	@Bean 
	public RestTemplate restTemplate(){
		return new RestTemplate(); 
	}
	
	@Bean
	public Metrics metrics(){
		return new Metrics();
	}
	
	@Bean
	public Kpis kpis(){
		return new Kpis();
	}
	
//	@Bean
//	public VasService vasService(){
//		return new VasService();
//	}
//
	
}
