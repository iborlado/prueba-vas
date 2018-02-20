package com.iborlado.boot.utils;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iborlado.boot.services.VasService;

public class Util {

	//Logging
	private static final Logger logger = LoggerFactory.getLogger(Util.class);

	/**
	 * 
	 * @param jsonInString
	 * @return
	 */
	public static boolean isJSONValid(String jsonInString ) {
		boolean valid = true;
	    try {
	       final ObjectMapper mapper = new ObjectMapper();
	       mapper.readTree(jsonInString); 
	    } catch (IOException e) {
	       valid = false;
	    }
	    return valid;
	  }
	
	/**
	 * 
	 * @param jsonInString
	 * @return
	 */
	public static Iterator<String> getFieldNames(String jsonInString){
		final ObjectMapper mapper = new ObjectMapper();
		Iterator<String> fieldNames = null;
	       try {
	    	   
			fieldNames = mapper.readTree(jsonInString).fieldNames();
			
		} catch (JsonProcessingException e) {
			logger.error(e.getMessage());
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		return fieldNames;
	}
	
	/**
	 * 
	 * @param jsonInString
	 * @return
	 */
	public static Iterator<Entry<String, JsonNode>> getFields(String jsonInString){
		final ObjectMapper mapper = new ObjectMapper();
		Iterator<Entry<String, JsonNode>> fields = null;
	    try {
	    	   
			fields = mapper.readTree(jsonInString).fields();
			
		} catch (JsonProcessingException e) {
			logger.error(e.getMessage());
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		return fields;
	}
	
	/**
	 * 
	 * @param mapToSort
	 * @return
	 */
	public static Map<String,Integer> sortMapByValue(Map<String,Integer> mapToSort){
		Map<String,Integer> sortedMap = null;
		try{
			sortedMap = mapToSort.entrySet()
						.stream()
						.sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
						.collect(Collectors.toMap(
								Map.Entry::getKey, 
								Map.Entry::getValue, 
								(e1, e2) -> e1, 
								LinkedHashMap::new
								));
		} catch(Exception e){
			logger.error(e.getMessage());
		}
		return sortedMap;
	}


	/**
	 * 
	 * @param mapToSort
	 * @return
	 */
	public static Map<String,Long> sortMapByKey(Map<String,Long> mapToSort){
		Map<String,Long> sortedMap = null;
		try{
			sortedMap = mapToSort.entrySet()
						.stream()
						.sorted(Map.Entry.comparingByKey())
						.collect(Collectors.toMap(
								Map.Entry::getKey, 
								Map.Entry::getValue, 
								(e1, e2) -> e1, 
								LinkedHashMap::new
								));
		} catch(Exception e){
			logger.error(e.getMessage());;
		}
		return sortedMap;
	}


}
