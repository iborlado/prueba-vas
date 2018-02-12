package com.iborlado.boot.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.iborlado.boot.dto.Kpis;
import com.iborlado.boot.dto.Metrics;
import com.iborlado.boot.services.IVasService;


@RestController
@RequestMapping("/prueba-vas")
public class VasRestController {
	
	@Autowired
	IVasService iVasService;
	
	@Autowired
	Metrics metrics;
	
	@Autowired
	Kpis kpis;
	

	@GetMapping(path = "/{date}", produces=MediaType.APPLICATION_JSON_VALUE )
	public ResponseEntity<String> getJson(@PathVariable("date") String date) {
		//extract json file
		String jsonFile = iVasService.getJsonFromFile(date);
		HttpStatus status = jsonFile != null ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
		return new ResponseEntity<String>(jsonFile, status);
	}
	
	
	@GetMapping(path = "/metrics/{date}", produces=MediaType.APPLICATION_JSON_VALUE )
	public Metrics metrics(@PathVariable("date") String date) {
		metrics = iVasService.calculateMetrics(date);	
		
		return metrics;
	}
	
	
	@GetMapping(path = "/kpis/{dates}", produces=MediaType.APPLICATION_JSON_VALUE )
	public Kpis kpis(@PathVariable("dates") String dates) {
		kpis = iVasService.calculateKpis(dates);
		
		return kpis;
	}
	
	
}
