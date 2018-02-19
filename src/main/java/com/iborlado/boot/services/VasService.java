package com.iborlado.boot.services;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
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
@Primary
public class VasService implements IVasService{

	@Autowired
	RestTemplate restTemplate;
	
	@Autowired
	Metrics metrics;
	
	@Autowired
	Kpis kpis;
	
	//Get constant values from properties file
	@Value("${vas.resource.url}")
	private String resource_url;
	@Value("${vas.prefix.file}")
	private String prefix_file;
	@Value("${vas.suffix.file}")
	private String suffix_file;
	@Value("${vas.number.rows}")
	private String number_rows;
	@Value("${vas.number.calls}")
	private String number_calls;
	@Value("${vas.number.msgs}")
	private String number_msgs;
	@Value("${vas.different.origins}")
	private String different_origins;
	@Value("${vas.different.destinations}")
	private String different_destinations;
	@Value("${vas.process.duration}")
	private String process_duration;
	@Value("${vas.call}")
	private String call;
	@Value("${vas.call.ok}")
	private String call_ok;
	@Value("${vas.call.ko}")
	private String call_ko;
	@Value("${vas.msg}")
	private String msg;
	@Value("${vas.msg.delivered}")
	private String msg_delivered;
	@Value("${vas.msg.seen}")
	private String msg_seen;
	@Value("${vas.max.digits}")
	private int max_digits;

	// Words of ranking test {“ARE, YOU, FINE, HELLO, NOT”}
	private final String[] RANKING_WORDS = {"ARE", "YOU", "FINE", "HELLO", "NOT"};

	//METRICS
	private int counterMissingField;
	private int counterCallsWithError;
	private int counterCallsOK;
	private int counterCallsKO;
	private int counterMessagesWithError;
	private int counterMessages;
	private int counterMessagesWithoutContent;
	private Map<String,Integer> resultWordRanking;
	private int counterWrongLines;
	
	//KPIS
	private Map<String, Long> processedFiles;
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
		String contentFile = getContentFileFromUrl(date);
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
		initializeKpis();
		String [] fileNames = UtilBusiness.extractFileNames(dates);
		//repeat to all files
		for (String name: fileNames){
			Long startProcess = new Date().getTime();
			processFile(name);
			durationProcess = UtilBusiness.calculateTimeofProcces(startProcess, name);
			processedFiles.put(prefix_file+name+suffix_file, durationProcess);
			//save values to assign later to the "kpis" object 
			auxKpisMap = addKpisToMap(auxKpisMap, name, nRows, nCalls, nMsgs, differentOrigin, differentDestination, durationProcess);
		}
		//calculate total and return "kpis" object
		fillKpiValues(auxKpisMap);
		return kpis;
	}
	

	private String processFile(String date){
		int wrongLines = 0;
		initializeCounters();
		initializeAuxKpis();
		setupRankingWords();
		
		String contentFile = getContentFileFromUrl(date);
		String[] list = UtilBusiness.formatLines(contentFile);
		
		String response = "";
		nRows = list.length;
		for (String line : list){
			try{
				System.out.print(line);
				boolean validJson = Util.isJSONValid(line);
				if (validJson){
					response+=line+",";
					if (checkGenericFields(line)){
						String typeOfMessage = UtilBusiness.getTypeOfMessage(line);
						if (typeOfMessage.equals(call)){
							nCalls++;
							checkCallFields(line);
						}
						else if (typeOfMessage.equals(msg)){
							nMsgs++;
							checkMsgFields(line);
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
		metrics.setnRowsMissingFields(counterMissingField);
		metrics.setnMessagesBlankContent(counterMessagesWithoutContent);
		metrics.setnRowsFieldsErrors(counterCallsWithError+counterMessagesWithError+counterWrongLines);
		metrics.setnCallsByCountry(null);
		
		Map<String,Integer> relationship = new HashMap<>();
		relationship.put(call_ok, counterCallsOK);
		relationship.put(call_ko, counterCallsKO);
		metrics.setRelationshipOkKoCalls(relationship);
		
		metrics.setAverageCallByCountry(null);
		
		resultWordRanking = Util.sortMapByValue(resultWordRanking);
		metrics.setWordOcurrenceRanking(resultWordRanking);
		
	}
	
	
	private void fillKpiValues(Map<String, Map<String,Long>> kpisTotalMap){
		initializeAuxKpis();
		for(Map<String, Long> kpisFileMap: kpisTotalMap.values()){
		  nRows += 	kpisFileMap.get(number_rows);
		  nCalls += kpisFileMap.get(number_calls);
		  nMsgs += kpisFileMap.get(number_msgs);
		  differentOrigin += kpisFileMap.get(different_origins);
		  differentDestination += kpisFileMap.get(different_destinations);
		}
		kpis.setnProcessedFiles(kpisTotalMap.size());
		kpis.setnRows(nRows);
		kpis.setnCalls(nCalls);
		kpis.setnMessages(nMsgs);
		kpis.setnDifferentOrigin(differentOrigin);
		kpis.setnDifferentDestination(differentDestination);
		processedFiles  = Util.sortMapByKey(processedFiles);
		kpis.setDurationJsonProcess(processedFiles);
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
			counterMissingField++;
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
				  if (value.equals(call_ok)){
					  counterCallsOK++;
				  }
				  else if(value.equals(call_ko)){
					  counterCallsKO++;
				  }
				  else {
					  counterCallsWithError++;
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
			counterMissingField++;
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
					  counterMessagesWithoutContent++;
				  }
				  else {
					  addWordtoRanking(value);
				  }
				  counterFields++;
			  } 
			  else if (key.equals(Msg.message_status.toString())){
				  existMsgStatusField = true;
				  value = entry.getValue().asText();
				  if (value.equals(msg_delivered) || value.equals(msg_seen)){
					  counterMessages++;
				  }
				  else {
					  counterMessagesWithError++;
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
			counterMissingField++;
		}
		
	}
	
	private void addWordtoRanking(String messageContent){
		for (String word: RANKING_WORDS){
			int contador = resultWordRanking.get(word);
			resultWordRanking.put(word,contador +StringUtils.countOccurrencesOf(messageContent, word));
		}
	}
	
	
	private String getContentFileFromUrl(String date){
		String url = resource_url;	
		String extension = suffix_file;
		String transactionUrl = url+date+extension;
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(transactionUrl);
		//get file from URI
		String contentFile = restTemplate.getForObject(builder.toUriString(), String.class);
		
		return contentFile;
	}
	

	
	private Map<String, Map<String,Long>> addKpisToMap (Map<String, Map<String,Long>> kpisMap, String file, long nRows, long nCalls,
			long nMsgs, long differentOrigin, long differentDestination, long durationProcess){
		
		Map <String,Long> values  = new HashMap<>();
		values.put(number_rows, nRows);
		values.put(number_calls, nCalls);
		values.put(number_msgs, nMsgs);
		values.put(different_origins, differentOrigin);
		values.put(different_destinations,differentDestination);
		values.put(process_duration, durationProcess);
		kpisMap.put(file, values);
		
		return kpisMap;
	}
	
	
	/**
	 * GSM - DCS 1800
	 * @param msisdn
	 */
	private void getCountryCode(String msisdn){
		String countryCode;
		String nationalDestinationCode;
		String subscriberNumber;
		Map<String,String> fourDigitsCountryCodes = new HashMap<>();
	}
	
	
	/************************************INITIALIZATION*********************************/
	
	private void initializeCounters(){
		counterCallsWithError = 0;
		counterCallsOK = 0;
		counterCallsKO = 0;
		counterMessages = 0;
		counterMessagesWithError = 0;
		counterMessagesWithoutContent = 0;
		counterMissingField = 0;
		counterWrongLines = 0;
	}
	
	private void setupRankingWords(){ 
		resultWordRanking = new HashMap<>();
		for (String word: RANKING_WORDS){
			resultWordRanking.put(word, 0);
		}
	}
	
	private void initializeKpis(){
		processedFiles = new HashMap<>();
		auxKpisMap = new HashMap<>();
	}
	
	private void initializeAuxKpis(){
		nRows = 0;
		nCalls = 0;
		nMsgs = 0;
		differentOrigin = 0;
		differentDestination = 0;
		durationProcess = 0;
	}
	
	
	/********************************************OTHERS********************************************/
	/*Other way get properties
	 * @Autowired
	private Environment env;
	private void getProperties(){
		RESOURCE_URL = env.getProperty("vas.resource.url");
		PREFIX_FILE =  env.getProperty("vas.prefix.file");
		SUFFIX_FILE = env.getProperty("vas.suffix.file");
		NUMBER_ROWS = env.getProperty("vas.number.rows");
		NUMBER_CALLS = env.getProperty("vas.number.calls");
		NUMBER_MSGS = env.getProperty("vas.number.msgs");
		DIFFERENT_ORIGINS = env.getProperty("vas.different.origins");
		DIFFERENT_DESTINATIONS = env.getProperty("vas.different.destinations");
		PROCESS_DURATION = env.getProperty("vas.process.duration");
		CALL = env.getProperty("vas.call");
		CALL_OK = env.getProperty("vas.call.ok");
		CALL_KO = env.getProperty("vas.call.ko");
		MSG = env.getProperty("vas.msg");
		MSG_DELIVERED = env.getProperty("vas.msg.delivered");
		MSG_SEEN = env.getProperty("vas.msg.seen");
		MAX_DIGITS = Integer.valueOf(env.getProperty("vas.max.digits"));
		WORD1 = env.getProperty("vas.ranking.words.word1");
		WORD2 = env.getProperty("vas.ranking.words.word2");
		WORD3 = env.getProperty("vas.ranking.words.word3");
		WORD4 = env.getProperty("vas.ranking.words.word4");
		WORD5 = env.getProperty("vas.ranking.words.word5");
	}*/
	

	
}
