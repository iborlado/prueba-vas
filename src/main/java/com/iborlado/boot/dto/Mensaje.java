package com.iborlado.boot.dto;

public class Mensaje {
	
	private String message_type;
	private String timestamp;
	private String origin;
	private String destination;
	private Long duration;
	private String status_code;
	private String status_description;
	public String getMessage_type() {
		return message_type;
	}
	public void setMessage_type(String message_type) {
		this.message_type = message_type;
	}
	public String getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}
	public String getOrigin() {
		return origin;
	}
	public void setOrigin(String origin) {
		this.origin = origin;
	}
	public String getDestination() {
		return destination;
	}
	public void setDestination(String destination) {
		this.destination = destination;
	}
	public Long getDuration() {
		return duration;
	}
	public void setDuration(Long duration) {
		this.duration = duration;
	}
	public String getStatus_code() {
		return status_code;
	}
	public void setStatus_code(String status_code) {
		this.status_code = status_code;
	}
	public String getStatus_description() {
		return status_description;
	}
	public void setStatus_description(String status_description) {
		this.status_description = status_description;
	}
	public Mensaje(String message_type, String timestamp, String origin, String destination, Long duration,
			String status_code, String status_description) {
		super();
		this.message_type = message_type;
		this.timestamp = timestamp;
		this.origin = origin;
		this.destination = destination;
		this.duration = duration;
		this.status_code = status_code;
		this.status_description = status_description;
	}
	public Mensaje() {
		super();
	}
	
	
}
