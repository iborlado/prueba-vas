package com.iborlado.boot.utils;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iborlado.boot.dto.MessageType;
import com.iborlado.boot.dto.Msg;

public class Util {

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
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
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
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
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
			e.printStackTrace();
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
			e.printStackTrace();
		}
		return sortedMap;
	}


}
