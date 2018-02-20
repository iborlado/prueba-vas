package com.iborlado.boot.dto;

public class PhoneNumber {

	private String name;
	private String dialCode;
	private String code;
	
	public PhoneNumber() {
		
	}

	public PhoneNumber(String name, String dialCode, String code) {
		super();
		this.name = name;
		this.dialCode = dialCode;
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDialCode() {
		return dialCode;
	}

	public void setDialCode(String dialCode) {
		this.dialCode = dialCode;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	
}
