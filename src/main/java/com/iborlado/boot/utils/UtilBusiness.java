package com.iborlado.boot.utils;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iborlado.boot.dto.Call;
import com.iborlado.boot.dto.MessageType;

public class UtilBusiness {

	//Logging
	private static final Logger logger = LoggerFactory.getLogger(UtilBusiness.class);

	/**
	 * 
	 * @param contentFile
	 * @return
	 */
	public static String[] formatLines (String contentFile){
		//each json line is separated by line break
		String[] list = contentFile.split("\n");
				
		return list;
	}

	/**
	 * 
	 * @param kpisParams
	 * @return
	 */
	public static String[] extractFileNames (String kpisParams){
		String [] fileNames = kpisParams.split("-");
		return fileNames;
	}
	
	/**
	 * 
	 * @param startProcess
	 * @param file
	 * @return
	 */
	public static Long calculateTimeofProcces (Long startProcess, String file){
		Long duration = 0l;
		Long finishProcess = new Date().getTime();
		duration = finishProcess-startProcess;

		return duration;
	}
	
	/**
	 * 
	 * @param jsonInString
	 * @return
	 */
	public static String getTypeOfMessage(String jsonInString){
		Iterator<Entry<String, JsonNode>> fields = Util.getFields(jsonInString);
		String type = null;
		while (fields.hasNext()){
			  Map.Entry<String, JsonNode> entry = (Map.Entry<String, JsonNode>) fields.next();		
			  if (entry.getKey().equals(MessageType.message_type.toString())){
				  type = entry.getValue().asText();
				  break;
			  }
		}
		
		return type;
	}
	
	/**
	 * 
	 * @param jsonInString
	 * @return
	 */
	public static Double getDurationCall(String jsonInString){
		Double duration = null;
		final ObjectMapper mapper = new ObjectMapper();
		try {
			duration = mapper.readTree(jsonInString).findValue(Call.duration.toString()).asDouble();
			logger.debug("Duración = "+duration);
		} catch (JsonProcessingException e) {
			logger.error(e.getMessage());
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		
		return duration;
	}
	
	/**
	 * 
	 * @param jsonInString
	 * @return
	 */
	public static String getOriginMsisdn(String jsonInString){
		String msisdn = null;
		final ObjectMapper mapper = new ObjectMapper();
		try {
			msisdn = mapper.readTree(jsonInString).findValue(MessageType.origin.toString()).asText();
			logger.debug("Origin missdn = "+msisdn);
		} catch (JsonProcessingException e) {
			logger.error(e.getMessage());
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		return msisdn;
	}
	
	/**
	 * 
	 * @param jsonInString
	 * @return
	 */
	public static String getDestinationMsisdn(String jsonInString){
		String msisdn = null;
		final ObjectMapper mapper = new ObjectMapper();
		try {
			msisdn = mapper.readTree(jsonInString).findValue(MessageType.destination.toString()).asText();
			logger.debug("destination missdn = "+msisdn);
		} catch (JsonProcessingException e) {
			logger.error(e.getMessage());
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		return msisdn;
	}
}
