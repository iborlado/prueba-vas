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

@Service
public class VasService implements IVasService{

	@Autowired
	RestTemplate restTemplate;
	
	@Autowired
	Metrics metrics;
	
	@Autowired
	Kpis kpis;
	
	private int contadorMissingField;
	private int contadorLLamadasConError;
	private int contadorLlamadas;
	private int contadorLlamadasOK;
	private int contadorLlamadasKO;

	private int contadorMensajesConError;
	private int contadorMensajes;
	private int contadorMensajesSinContenido;
	
	private Map<String,Integer> resultWordRanking;
	private List<String> rankingWords;
	
	//KPIS
	private Map<String,Long> ficherosProcesados;
	private Map<String, Map<String,Long>> complexMap;
	private long nRows;
	private long nCalls;
	private long nMsgs;
	private long differentOrigin;
	private long differentDestination;
	private long durationProcess;
	
	
	@Override
	public String getJsonFromFile(String date) {
		int numLineasErroneas = 0;
		
		//obtenemos un string con el fichero
		String contentFile = getContentFileFromUrl(date);
		//obtenemos cada línea del fichero
		String[] jsonList = contentFile.split("\n");
		
		String response = "[";
		for (int i=0;i<jsonList.length;i++){
			try{
				String linea = jsonList[i];
				System.out.print(linea);
				boolean valida = Util.isJSONValid(linea);
				if (valida){
					response+=linea+",";
				}
				else{
					numLineasErroneas++;
				}
			}catch (Exception e) {
				System.out.println("Error en elemento " +i +"------" +e.toString());
				numLineasErroneas++;
			}
		}
		if (numLineasErroneas>0){
			response=null;
		}
		else{
			response = response.substring(0, response.length()-1) + "]";
		}
		
		return response;
	}
	

	public void calculateMetrics(String fecha) {
		Long inicioProceso = new Date().getTime();
		int numLineasErroneas = 0;
		inicializarContadores();
		inicializarAuxKpis();
		establecerRankingWords();
		
	
		//obtenemos un string con el fichero
		String contentFile = getContentFileFromUrl(fecha);
		//obtenemos cada línea del fichero
		String[] lista = formatLines(contentFile);
		
		String response = "";
		nRows = lista.length;
		for (String linea : lista){
			try{
				System.out.print(linea);
				boolean valida = Util.isJSONValid(linea);
				if (valida){
					response+=linea+",";
					String tipoMensaje = tipodeMensaje(linea);
					System.out.println("tipo de mensaje = "	+ tipoMensaje);
					if (tipoMensaje != null){
						boolean todoscamposgenericos = comprobarGenerico(linea);
						if (tipoMensaje.equals("CALL")){
							nCalls++;
							comprobarTipoLlamada(linea, todoscamposgenericos);
						}
						else if (tipoMensaje.equals("MSG")){
							nMsgs++;
							comprobarTipoMensaje(linea, todoscamposgenericos);
						}
					}

				}
				else{
					numLineasErroneas++;
				}
			}catch (Exception e) {
				System.out.println("Error en elemento " +linea +"------" +e.toString());
				numLineasErroneas++;
			}
			
		}
		if (numLineasErroneas>0){
			response=null;
		}
		else{
			response = response.substring(0, response.length()-1);
		}
		
		metrics.setnRowsMissingFields(contadorMissingField);
		metrics.setnMessagesBlankContent(contadorMensajesSinContenido);
		metrics.setnRowsFieldsErrors(contadorLLamadasConError+contadorMensajesConError+numLineasErroneas);
		metrics.setnCallsByCountry(null);
		
		Map<String,Integer> relationship = new HashMap<>();
		relationship.put("OK", contadorLlamadasOK);
		relationship.put("KO", contadorLlamadasKO);
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
//		System.out.println(""+);
		System.out.println(metrics.getRelationshipOkKoCalls().get("OK")+"OK / "+metrics.getRelationshipOkKoCalls().get("KO")+"KO");


		//return metrics;
		Long finProceso = new Date().getTime();
		System.out.println("Proceso "+fecha+ "--> "+ (finProceso-inicioProceso)+" ms");

		ficherosProcesados.put("MCP_"+fecha+".json", finProceso-inicioProceso);
		durationProcess = finProceso-inicioProceso;
		
		
	}
	
	
	public void calculateKpis(String fechas){
		System.out.println(fechas);
		inicializarpkis();
		String [] fileNames = extractFileNames(fechas);
		//repeat to all files
		for (String name: fileNames){
		calculateMetrics(name);
		complexMap = fillComplexMap(complexMap, name, nRows, nCalls, nMsgs, differentOrigin, differentDestination, durationProcess);
		}
		
		//calcular totales y devolver objeto kpis
		fillKpis(complexMap);
		
	}
	
	
	private void fillKpis(Map<String, Map<String,Long>> complexMap){
		System.out.println("Ficheros procesados = "+complexMap.size());
		inicializarAuxKpis();
		for(Map<String, Long> mapita: complexMap.values()){
		  nRows += 	mapita.get("nRows");
		  nCalls += mapita.get("nCalls");
		  nMsgs += mapita.get("nMsgs");
		  differentOrigin += mapita.get("differentOrigin");
		  differentDestination += mapita.get("differentDestination");
		}
		kpis.setnProcessedFiles(complexMap.size());
		kpis.setnRows(nRows);
		kpis.setnCalls(nCalls);
		kpis.setnMessages(nMsgs);
		kpis.setnDifferentOrigin(differentOrigin);
		kpis.setnDifferentDestination(differentDestination);
		ficherosProcesados  = Util.sortMapByKey(ficherosProcesados);
		kpis.setDurationJsonProcess(ficherosProcesados);
	}
	
	private boolean comprobarGenerico(String linea){
		Iterator<Entry<String, JsonNode>> campos = Util.getFields(linea);
		int contadorCampos = 0;
		boolean todoscampos = true;
		while (campos.hasNext()){
			  Map.Entry<String, JsonNode> entry = (Map.Entry<String, JsonNode>) campos.next();
			  String key = entry.getKey();
			  if (key.equals(MessageType.message_type.toString())){
				  contadorCampos++;
			  }else if (key.equals(MessageType.timestamp.toString())){
				  contadorCampos++;
			  }else if (key.equals(MessageType.origin.toString())){
				  contadorCampos++;
			  }else if (key.equals(MessageType.destination.toString())){
				  contadorCampos++;
			  }
		}
		if (contadorCampos < MessageType.values().length){
			contadorMissingField++;
			todoscampos = false;
		}
		return todoscampos;
	}
	
	private void calcularCamposLlamada(String linea, boolean todoscamposgenericos){
		Iterator<Entry<String, JsonNode>> campos = Util.getFields(linea);
		int contadorCampos = 0;
		String value = null;
		while (campos.hasNext()){
			  Map.Entry<String, JsonNode> entry = (Map.Entry<String, JsonNode>) campos.next();	
			  String key = entry.getKey();

			  if (key.equals(Call.duration.toString())){
				  contadorCampos++;
			  }
			  if (key.equals(Call.status_code.toString())){
				  value = entry.getValue().asText();
				  if (value.equals("OK")){
					  contadorLlamadasOK++;
				  }
				  else if(value.equals("KO")){
					  contadorLlamadasKO++;
				  }
				  else {
					  contadorLLamadasConError++;
				  }
				  contadorCampos++;
			  }
			  else if (key.equals(Call.status_description.toString())){
				  contadorCampos++;
			  }
		}
		if ((contadorCampos < Call.values().length) && todoscamposgenericos){
			contadorMissingField++;
		}
	}
	
	private void calcularCamposMensaje(String linea, boolean todoscamposgenericos){
		Iterator<Entry<String, JsonNode>> campos = Util.getFields(linea);
		String value = null;
		String key = null;
		int contadorCampos = 0;

		while (campos.hasNext()){
			  Map.Entry<String, JsonNode> entry = (Map.Entry<String, JsonNode>) campos.next();	
			  key = entry.getKey();
			  if (key.equals(Msg.message_content.toString())){
				  value = entry.getValue().asText();
				  if(value==null || value.isEmpty() ){
					  contadorMensajesSinContenido++;
				  }
				  else {
					  getRanking(value);
				  }
				  contadorCampos++;
			  } 
			  else if (key.equals(Msg.message_status.toString())){
				  value = entry.getValue().asText();
				  if (value.equals("DELIVERED") || value.equals("SEEN")){
					  contadorMensajes++;
				  }
				  else {
					  contadorMensajesConError++;
				  }
				  contadorCampos++;
			  }
			  
		}
		if ((contadorCampos < Msg.values().length) && todoscamposgenericos){
			contadorMissingField++;
		}
	}
	
	
	private void comprobarTipoLlamada(String linea, boolean todoscamposgenericos){
		System.out.println("Comprobando llamada");

		//si faltan campos
		//si campos erroneos
		calcularCamposLlamada(linea,todoscamposgenericos);
		//calculo duracion media llamadas
		//llamadas por pais
		//relacion ok/ko
		
	}
	
	private void comprobarTipoMensaje(String linea, boolean todoscamposgenericos){
		System.out.println("Comprobando mensaje");

		//si faltan campos y si campos erroneos
		calcularCamposMensaje(linea, todoscamposgenericos);
		//si contenido mensaje vacio
		//ranking
		
		//getRanking(linea,rankingWords);
	}
	
	
	
	
	
	
	
	
	
	private void getRanking(String messageContent){
		for (String word: rankingWords){
			int contador = resultWordRanking.get(word);
			resultWordRanking.put(word,contador +StringUtils.countOccurrencesOf(messageContent, word));
			System.out.println(word+" = "+resultWordRanking.get(word));
		}
	}
	
	private void inicializarContadores(){
		contadorLLamadasConError = 0;
		contadorLlamadasOK = 0;
		contadorLlamadasKO = 0;
		contadorMensajes = 0;
		contadorMensajesConError = 0;
		contadorMensajesSinContenido = 0;
		contadorMissingField = 0;
	}
	
	private void establecerRankingWords(){ 
		rankingWords = Arrays.asList("ARE", "YOU", "FINE", "HELLO", "NOT");
		resultWordRanking = new HashMap<>();
		for (String word: rankingWords){
			resultWordRanking.put(word, 0);
		}
	}
	
	private void inicializarpkis(){
		ficherosProcesados = new HashMap<>();
		complexMap = new HashMap<>();
	}
	
	private void inicializarAuxKpis(){
		nRows = 0;
		nCalls = 0;
		nMsgs = 0;
		differentOrigin = 0;
		differentDestination = 0;
		durationProcess = 0;
	}
	
	
	private String[] formatLines (String contentFile){
		//obtenemos cada línea del fichero
		String[] lista = contentFile.split("\n");
				
		return lista;
	}

	private String[] extractFileNames (String kpisParam){
		String [] fileNames = kpisParam.split("-");
		return fileNames;
	}
	
	private String getContentFileFromUrl(String date){
		String url = "https://raw.githubusercontent.com/vas-test/test1/master/logs/MCP_";
		String extension = ".json";
		String transactionUrl = url+date+extension;
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(transactionUrl);
		//obtenemos un string con el fichero
		String contentFile = restTemplate.getForObject(builder.toUriString(), String.class);
		
		return contentFile;
	}
	
	private String tipodeMensaje(String jsonInString){
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
	
	private Map<String, Map<String,Long>> fillComplexMap (Map<String, Map<String,Long>> complexMap, String file, long nRows, long nCalls,
			long nMsgs, long differentOrigin, long differentDestination, long durationProcess){
		
		Map <String,Long> values  = new HashMap<>();
		values.put("nRows", nRows);
		values.put("nCalls", nCalls);
		values.put("nMsgs", nMsgs);
		values.put("differentOrigin", differentOrigin);
		values.put("differentDestination",differentDestination);
		values.put("durationProcess", durationProcess);
		complexMap.put(file, values);
		
		return complexMap;
	}

}
