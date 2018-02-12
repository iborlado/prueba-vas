package com.iborlado.boot.dto;

import java.util.Map;

public class Kpis {

	private int nProcessedFiles;
	private long nRows;
	private long nCalls;
	private long nMessages;
	private long nDifferentOrigin;
	private long nDifferentDestination;
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

	public long getnDifferentOrigin() {
		return nDifferentOrigin;
	}

	public void setnDifferentOrigin(long nDifferentOrigin) {
		this.nDifferentOrigin = nDifferentOrigin;
	}

	public long getnDifferentDestination() {
		return nDifferentDestination;
	}

	public void setnDifferentDestination(long nDifferentDestination) {
		this.nDifferentDestination = nDifferentDestination;
	}

	public Map<String, Long> getDurationJsonProcess() {
		return durationJsonProcess;
	}

	public void setDurationJsonProcess(Map<String, Long> durationJsonProcess) {
		this.durationJsonProcess = durationJsonProcess;
	}

	
	
}
