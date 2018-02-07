package com.iborlado.boot.controller;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class RestController {
	@RequestMapping(path = "/{fecha}", method = RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE )
	public @ResponseBody String get(@PathVariable("fecha") String fecha) {
			
		
		return "{\"fecha\":\""+fecha+"\"}";
		
	}
	
	@RequestMapping(path = "/metrics/{fecha}", method = RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE )
	public @ResponseBody String metrics(@PathVariable("fecha") String fecha) {
		
		
		return "{\"fecha\":\""+fecha+"\"}";
		
	}
	
	@RequestMapping(path = "/kpis", method = RequestMethod.GET, produces=MediaType.APPLICATION_JSON_VALUE )
	public @ResponseBody String kpis() {
		
		
		return "{\"nombre\":\"kpi\"}";
		
	}
}
