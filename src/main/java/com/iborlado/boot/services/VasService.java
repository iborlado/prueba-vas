package com.iborlado.boot.services;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.iborlado.boot.dto.Call;
import com.iborlado.boot.dto.Kpis;
import com.iborlado.boot.dto.MessageType;
import com.iborlado.boot.dto.Metrics;
import com.iborlado.boot.dto.Msg;
import com.iborlado.boot.utils.Util;
import com.iborlado.boot.utils.UtilBusiness;

@Service
public class VasService implements IVasService{

	@Autowired
	RestTemplate restTemplate;
	
	@Autowired
	Metrics metrics;
	
	@Autowired
	Kpis kpis;
	
	//CTES, pasar a properties
	private static final String RESOURCE_URL = "https://raw.githubusercontent.com/vas-test/test1/master/logs/MCP_";
	private static final String PREFIX_FILE = "MCP_";
	private static final String SUFFIX_FILE = ".json";
	private static final String NUMBER_ROWS = "nRows";
	private static final String NUMBER_CALLS = "nCalls";
	private static final String NUMBER_MSGS = "nMsgs";
	private static final String DIFFERENT_ORIGINS = "differentOrigin";
	private static final String DIFFERENT_DESTINATIONS = "differentDestination";
	private static final String PROCESS_DURATION = "durationProcess";
	private static final String CALL = "CALL";
	private static final String CALL_OK = "OK";
	private static final String CALL_KO = "KO";
	private static final String MSG = "MSG";
	private static final String MSG_DELIVERED = "DELIVERED";
	private static final String MSG_SEEN = "SEEN";
	private static final String[] RANKING_WORDS = {"ARE", "YOU", "FINE", "HELLO", "NOT"};
	
	//METRICS
	private int contadorMissingField;
	private int contadorLLamadasConError;
	private int contadorLlamadasOK;
	private int contadorLlamadasKO;
	private int contadorMensajesConError;
	private int contadorMensajes;
	private int contadorMensajesSinContenido;
	private Map<String,Integer> resultWordRanking;
	private int counterWrongLines;
	
	//KPIS
	private Map<String, Long> ficherosProcesados;
	private Map<String, Map<String,Long>> auxKpisMap;
	private long nRows;
	private long nCalls;
	private long nMsgs;
	private long differentOrigin;
	private long differentDestination;
	private long durationProcess;
	
	
	@Override
	public String getJsonFromFile(String date) {
		int nWrongLines = 0;
		
		//obtenemos un string con el fichero
		String contentFile = getContentFileFromUrl(date);
		//obtenemos cada línea del fichero
		String[] jsonList = UtilBusiness.formatLines(contentFile);
		
		String response = "[";
		for (String jsonLine : jsonList){
			try{
				System.out.print(jsonLine);
				boolean validLine = Util.isJSONValid(jsonLine);
				if (validLine){
					response+=jsonLine+",";
				}
				else{
					nWrongLines++;
				}
			}catch (Exception e) {
				nWrongLines++;
			}
		}
		if (nWrongLines>0){
			response=null;
		}
		else{
			response = response.substring(0, response.length()-1) + "]";
		}
		
		return response;
	}
	
	
	@Override
	public Metrics calculateMetrics(String date) {
		processFile(date);
		fillMetricValues();
		
		return metrics;
	}
	
	
	@Override
	public Kpis calculateKpis(String dates){
		inicializarpkis();
		String [] fileNames = UtilBusiness.extractFileNames(dates);
		//repeat to all files
		for (String name: fileNames){
			Long inicioProceso = new Date().getTime();
			processFile(name);
			//obtener duracion del proceso
			durationProcess = UtilBusiness.calcularTimeofProcces(inicioProceso, name);
			ficherosProcesados.put(PREFIX_FILE+name+SUFFIX_FILE, durationProcess);
			//guardar valores a asignar mas tarde al objeto kpis
			auxKpisMap = addKpisToMap(auxKpisMap, name, nRows, nCalls, nMsgs, differentOrigin, differentDestination, durationProcess);
		}
		//calcular totales y devolver objeto kpis
		fillKpiValues(auxKpisMap);
		return kpis;
	}
	

	private String processFile(String date){
		int wrongLines = 0;
		inicializarContadores();
		inicializarAuxKpis();
		establecerRankingWords();
		
		//obtenemos un string con el fichero
		String contentFile = getContentFileFromUrl(date);
		//obtenemos cada línea del fichero
		String[] lista = UtilBusiness.formatLines(contentFile);
		
		String response = "";
		nRows = lista.length;
		for (String linea : lista){
			try{
				System.out.print(linea);
				boolean validJson = Util.isJSONValid(linea);
				if (validJson){
					response+=linea+",";
					if (checkGenericFields(linea)){
						String tipoMensaje = UtilBusiness.tipodeMensaje(linea);
						if (tipoMensaje.equals(CALL)){
							nCalls++;
							checkCallFields(linea);
						}
						else if (tipoMensaje.equals(MSG)){
							nMsgs++;
							checkMsgFields(linea);
						}
						else {
							wrongLines++;
						}
					}
				}
				else{
					wrongLines++;
				}
			}catch (Exception e) {
				System.out.println("Error en elemento " +linea +"------" +e.toString());
				wrongLines++;
			}
		}
		if (wrongLines>0){
			response=null;
		}
		else{
			response = response.substring(0, response.length()-1);
		}
		counterWrongLines = wrongLines;
		return response;
	}
	
	private void fillMetricValues(){
		metrics.setnRowsMissingFields(contadorMissingField);
		metrics.setnMessagesBlankContent(contadorMensajesSinContenido);
		metrics.setnRowsFieldsErrors(contadorLLamadasConError+contadorMensajesConError+counterWrongLines);
		metrics.setnCallsByCountry(null);
		
		Map<String,Integer> relationship = new HashMap<>();
		relationship.put(CALL_OK, contadorLlamadasOK);
		relationship.put(CALL_KO, contadorLlamadasKO);
		metrics.setRelationshipOkKoCalls(relationship);
		
		metrics.setAverageCallByCountry(null);
		
		resultWordRanking = Util.sortMapByValue(resultWordRanking);
		metrics.setWordOcurrenceRanking(resultWordRanking);
		
		System.out.println("--->CONTADORES");
		System.out.println("contadorLLamadasConError = "+contadorLLamadasConError);
		System.out.println("contadorLlamadasKO = "+contadorLlamadasKO);
		System.out.println("contadorLlamadasOK = "+contadorLlamadasOK);
		System.out.println("contadorMensajes"+contadorMensajes);
		System.out.println("contadorMensajesConError = "+contadorMensajesConError);
		System.out.println("contadorMensajesSinContenido = "+contadorMensajesSinContenido);
		System.out.println(metrics.getRelationshipOkKoCalls().get(CALL_OK)+ CALL_OK +" / "+metrics.getRelationshipOkKoCalls().get(CALL_KO)+CALL_KO);
	}
	
	
	private void fillKpiValues(Map<String, Map<String,Long>> kpisTotalMap){
		System.out.println("Ficheros procesados = "+kpisTotalMap.size());
		inicializarAuxKpis();
		for(Map<String, Long> kpisFileMap: kpisTotalMap.values()){
		  nRows += 	kpisFileMap.get(NUMBER_ROWS);
		  nCalls += kpisFileMap.get(NUMBER_CALLS);
		  nMsgs += kpisFileMap.get(NUMBER_MSGS);
		  differentOrigin += kpisFileMap.get(DIFFERENT_ORIGINS);
		  differentDestination += kpisFileMap.get(DIFFERENT_DESTINATIONS);
		}
		kpis.setnProcessedFiles(kpisTotalMap.size());
		kpis.setnRows(nRows);
		kpis.setnCalls(nCalls);
		kpis.setnMessages(nMsgs);
		kpis.setnDifferentOrigin(differentOrigin);
		kpis.setnDifferentDestination(differentDestination);
		ficherosProcesados  = Util.sortMapByKey(ficherosProcesados);
		kpis.setDurationJsonProcess(ficherosProcesados);
	}
	
	private boolean checkGenericFields(String jsonLine){
		int countFields = 0;
		boolean genericFieldsOk = false;
		boolean existMessageTypeField = false;
		boolean existTimestampField = false;
		boolean existOriginField = false;
		boolean existDestinationField = false;
		Iterator<Entry<String, JsonNode>> fields = Util.getFields(jsonLine);
		
		while (fields.hasNext()){
			  Map.Entry<String, JsonNode> entry = (Map.Entry<String, JsonNode>) fields.next();
			  String key = entry.getKey();
			  if (key.equals(MessageType.message_type.toString())){
				  existMessageTypeField = true;
				  countFields++;
			  }else if (key.equals(MessageType.timestamp.toString())){
				  existTimestampField = true;
				  countFields++;
			  }else if (key.equals(MessageType.origin.toString())){
				  existOriginField = true;
				  countFields++;
			  }else if (key.equals(MessageType.destination.toString())){
				  existDestinationField = true;
				  countFields++;
			  }
		}
		
		//Ensure the existence of mandatory fields
		if (existMessageTypeField && existTimestampField && existOriginField && existDestinationField){
			//Avoid repeated fields
			if(countFields == MessageType.values().length){
				genericFieldsOk = true;
			} 
			else {
				counterWrongLines++;
			}
		}
		else {
			contadorMissingField++;
		}
		return genericFieldsOk;
	}
	
	private void checkCallFields(String jsonLine){
		int counterFields = 0;
		boolean existDurationField = false;
		boolean existStatusCodeField = false;
		boolean existStatusDescriptionField = false;
		String value = null;
		Iterator<Entry<String, JsonNode>> fields = Util.getFields(jsonLine);
				
		while (fields.hasNext()){
			  Map.Entry<String, JsonNode> entry = (Map.Entry<String, JsonNode>) fields.next();	
			  String key = entry.getKey();

			  if (key.equals(Call.duration.toString())){
				  existDurationField = true;
				  counterFields++;
			  }
			  else if (key.equals(Call.status_code.toString())){
				  existStatusCodeField = true;
				  value = entry.getValue().asText();
				  if (value.equals(CALL_OK)){
					  contadorLlamadasOK++;
				  }
				  else if(value.equals(CALL_KO)){
					  contadorLlamadasKO++;
				  }
				  else {
					  contadorLLamadasConError++;
				  }
				  counterFields++;
			  }
			  else if (key.equals(Call.status_description.toString())){
				  existStatusDescriptionField = true;
				  counterFields++;
			  }
		}
		
		//Ensure the existence of mandatory fields
		if (existDurationField && existStatusCodeField && existStatusDescriptionField){
			//Avoid repeated fields
			if(counterFields != Call.values().length){
				counterWrongLines++;
			} 
		}
		else {
			contadorMissingField++;
		}
	
	}
	
	private void checkMsgFields(String jsonLine){
		String value = null;
		String key = null;
		int counterFields = 0;
		boolean existMsgContentField = false;
		boolean existMsgStatusField = false;
		Iterator<Entry<String, JsonNode>> fields = Util.getFields(jsonLine);
		
		while (fields.hasNext()){
			  Map.Entry<String, JsonNode> entry = (Map.Entry<String, JsonNode>) fields.next();	
			  key = entry.getKey();
			  if (key.equals(Msg.message_content.toString())){
				  existMsgContentField = true;
				  value = entry.getValue().asText();
				  if(value==null || value.isEmpty() ){
					  contadorMensajesSinContenido++;
				  }
				  else {
					  addWordtoRanking(value);
				  }
				  counterFields++;
			  } 
			  else if (key.equals(Msg.message_status.toString())){
				  existMsgStatusField = true;
				  value = entry.getValue().asText();
				  if (value.equals(MSG_DELIVERED) || value.equals(MSG_SEEN)){
					  contadorMensajes++;
				  }
				  else {
					  contadorMensajesConError++;
				  }
				  counterFields++;
			  }
			  
		}
		
		//Ensure the existence of mandatory fields
		if (existMsgContentField && existMsgStatusField){
			//Avoid repeated fields
			if(counterFields != Msg.values().length){
				counterWrongLines++;
			} 
		}
		else {
			contadorMissingField++;
		}
		
	}
	
		
	private void addWordtoRanking(String messageContent){
		for (String word: RANKING_WORDS){
			int contador = resultWordRanking.get(word);
			resultWordRanking.put(word,contador +StringUtils.countOccurrencesOf(messageContent, word));
			System.out.println(word+" = "+resultWordRanking.get(word));
		}
	}
	
	
	private String getContentFileFromUrl(String date){
		String url = RESOURCE_URL;	
		String extension = SUFFIX_FILE;
		String transactionUrl = url+date+extension;
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(transactionUrl);
		//obtenemos un string con el fichero
		String contentFile = restTemplate.getForObject(builder.toUriString(), String.class);
		
		return contentFile;
	}
	

	
	private Map<String, Map<String,Long>> addKpisToMap (Map<String, Map<String,Long>> kpisMap, String file, long nRows, long nCalls,
			long nMsgs, long differentOrigin, long differentDestination, long durationProcess){
		
		Map <String,Long> values  = new HashMap<>();
		values.put(NUMBER_ROWS, nRows);
		values.put(NUMBER_CALLS, nCalls);
		values.put(NUMBER_MSGS, nMsgs);
		values.put(DIFFERENT_ORIGINS, differentOrigin);
		values.put(DIFFERENT_DESTINATIONS,differentDestination);
		values.put(PROCESS_DURATION, durationProcess);
		kpisMap.put(file, values);
		
		return kpisMap;
	}
	
	
	/************************************INITIALIZATION*********************************/
	
	private void inicializarContadores(){
		contadorLLamadasConError = 0;
		contadorLlamadasOK = 0;
		contadorLlamadasKO = 0;
		contadorMensajes = 0;
		contadorMensajesConError = 0;
		contadorMensajesSinContenido = 0;
		contadorMissingField = 0;
		counterWrongLines = 0;
	}
	
	private void establecerRankingWords(){ 
		resultWordRanking = new HashMap<>();
		for (String word: RANKING_WORDS){
			resultWordRanking.put(word, 0);
		}
	}
	
	private void inicializarpkis(){
		ficherosProcesados = new HashMap<>();
		auxKpisMap = new HashMap<>();
	}
	
	private void inicializarAuxKpis(){
		nRows = 0;
		nCalls = 0;
		nMsgs = 0;
		differentOrigin = 0;
		differentDestination = 0;
		durationProcess = 0;
	}
	
	
	
	
	

}
