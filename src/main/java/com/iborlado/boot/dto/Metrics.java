package com.iborlado.boot.dto;

import java.util.Map;

public class Metrics {
	
	private int nRowsMissingFields;
	private int nMessagesBlankContent;
	private int nRowsFieldsErrors;
	private Map<String,Integer> nCallsByCountry;
	private Map<String,Integer> relationshipOkKoCalls;
	private Map<String,Double> averageCallByCountry;
	private Map<String,Integer> wordOcurrenceRanking;

    public Metrics() {
		super();
	}

	public int getnRowsMissingFields() {
		return nRowsMissingFields;
	}

	public void setnRowsMissingFields(int nRowsMissingFields) {
		this.nRowsMissingFields = nRowsMissingFields;
	}

	public int getnMessagesBlankContent() {
		return nMessagesBlankContent;
	}

	public void setnMessagesBlankContent(int nMessagesBlankContent) {
		this.nMessagesBlankContent = nMessagesBlankContent;
	}

	public int getnRowsFieldsErrors() {
		return nRowsFieldsErrors;
	}

	public void setnRowsFieldsErrors(int nRowsFieldsErrors) {
		this.nRowsFieldsErrors = nRowsFieldsErrors;
	}

	public Map<String,Integer> getnCallsByCountry() {
		return nCallsByCountry;
	}

	public void setnCallsByCountry(Map<String,Integer> nCallsByCountry) {
		this.nCallsByCountry = nCallsByCountry;
	}

	public Map<String, Integer> getRelationshipOkKoCalls() {
		return relationshipOkKoCalls;
	}

	public void setRelationshipOkKoCalls(Map<String, Integer> relationshipOkKoCalls) {
		this.relationshipOkKoCalls = relationshipOkKoCalls;
	}

	public Map<String, Double> getAverageCallByCountry() {
		return averageCallByCountry;
	}

	public void setAverageCallByCountry(Map<String, Double> averageCallByCountry) {
		this.averageCallByCountry = averageCallByCountry;
	}

	public Map<String, Integer> getWordOcurrenceRanking() {
		return wordOcurrenceRanking;
	}

	public void setWordOcurrenceRanking(Map<String, Integer> wordOcurrenceRanking) {
		this.wordOcurrenceRanking = wordOcurrenceRanking;
	}

	
	
	
	
}
