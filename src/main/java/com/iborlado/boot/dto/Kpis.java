package com.iborlado.boot.dto;

import java.util.Map;

public class Kpis {

	private int nProcessedFiles;
	private long nRows;
	private long nCalls;
	private long nMessages;
	private Map<String,Long> nDifferentOrigin;
	private Map<String,Long> nDifferentDestination;
	private Map<String,Long> durationJsonProcess;
	
	public Kpis() {
		super();
	}

	public int getnProcessedFiles() {
		return nProcessedFiles;
	}

	public void setnProcessedFiles(int nProcessedFiles) {
		this.nProcessedFiles = nProcessedFiles;
	}

	public long getnRows() {
		return nRows;
	}

	public void setnRows(long nRows) {
		this.nRows = nRows;
	}

	public long getnCalls() {
		return nCalls;
	}

	public void setnCalls(long nCalls) {
		this.nCalls = nCalls;
	}

	public long getnMessages() {
		return nMessages;
	}

	public void setnMessages(long nMessages) {
		this.nMessages = nMessages;
	}

	public Map<String, Long> getnDifferentOrigin() {
		return nDifferentOrigin;
	}

	public void setnDifferentOrigin(Map<String, Long> nDifferentOrigin) {
		this.nDifferentOrigin = nDifferentOrigin;
	}

	public Map<String, Long> getnDifferentDestination() {
		return nDifferentDestination;
	}

	public void setnDifferentDestination(Map<String, Long> nDifferentDestination) {
		this.nDifferentDestination = nDifferentDestination;
	}

	public Map<String, Long> getDurationJsonProcess() {
		return durationJsonProcess;
	}

	public void setDurationJsonProcess(Map<String, Long> durationJsonProcess) {
		this.durationJsonProcess = durationJsonProcess;
	}

	
	
}
