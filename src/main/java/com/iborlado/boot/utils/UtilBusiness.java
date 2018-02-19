package com.iborlado.boot.utils;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.JsonNode;
import com.iborlado.boot.dto.MessageType;

public class UtilBusiness {

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
}
