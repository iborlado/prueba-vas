package com.iborlado.boot.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.iborlado.boot.dto.Kpis;
import com.iborlado.boot.dto.Mensaje;
import com.iborlado.boot.dto.Metrics;
import com.iborlado.boot.services.VasService;
import com.iborlado.boot.utils.Util;


@RestController
@RequestMapping("/prueba-vas")
public class VasRestController {
	
	@Autowired
	VasService vasService;
	
	@Autowired
	Metrics metrics;
	
	@Autowired
	Kpis kpis;

	@GetMapping(path = "/{date}", produces=MediaType.APPLICATION_JSON_VALUE )
	public ResponseEntity<String> get(@PathVariable("date") String date) {
		//obtenemos fichero json
		String fichero = vasService.getJsonFromFile(date);
		HttpStatus status = fichero != null ? HttpStatus.OK : HttpStatus.BAD_REQUEST;
		return new ResponseEntity<String>(fichero, status);
	}
	
	
	@GetMapping(path = "/metrics/{date}", produces=MediaType.APPLICATION_JSON_VALUE )
	public Metrics metrics(@PathVariable("date") String date) {
		vasService.calculateMetrics(date);
		
		return metrics;
		
	}
	
	
	@GetMapping(path = "/kpis/{dates}", produces=MediaType.APPLICATION_JSON_VALUE )
	public Kpis kpis(@PathVariable("dates") String dates) {
		vasService.calculateKpis(dates);
		
		return kpis;
	}
	
	
	@PostMapping(path = "/prueba", consumes=MediaType.APPLICATION_JSON_VALUE )
	public String post(@RequestBody List<Mensaje> mensaje) {
			System.out.println("Prueba");
			
		return "";
	}
	
	
	
	
}
