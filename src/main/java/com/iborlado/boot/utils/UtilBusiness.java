package com.iborlado.boot.utils;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.JsonNode;
import com.iborlado.boot.dto.MessageType;

public class UtilBusiness {

	public UtilBusiness() {
	}
	
	public static String[] formatLines (String contentFile){
		//obtenemos cada línea del fichero
		String[] list = contentFile.split("\n");
				
		return list;
	}

	public static String[] extractFileNames (String kpisParams){
		String [] fileNames = kpisParams.split("-");
		return fileNames;
	}
	
	public static Long calcularTimeofProcces (Long inicioProceso, String fichero){
		Long duration = 0l;
		Long finProceso = new Date().getTime();
		duration = finProceso-inicioProceso;
		System.out.println("Proceso "+fichero+ "--> "+ duration +" ms");

		return duration;
	}
	
	public static String tipodeMensaje(String jsonInString){
		Iterator<Entry<String, JsonNode>> campos = Util.getFields(jsonInString);
		String type = null;
		while (campos.hasNext()){
			  Map.Entry<String, JsonNode> entry = (Map.Entry<String, JsonNode>) campos.next();		
			  if (entry.getKey().equals(MessageType.message_type.toString())){
				  type = entry.getValue().asText();
				  break;
			  }
		}
		
		return type;
	}
}
