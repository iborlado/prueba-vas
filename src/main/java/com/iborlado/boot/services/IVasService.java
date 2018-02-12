package com.iborlado.boot.services;

import com.iborlado.boot.dto.Kpis;
import com.iborlado.boot.dto.Metrics;

public interface IVasService {
	public String getJsonFromFile(String date);
	public Metrics calculateMetrics(String date);
	public Kpis calculateKpis(String dates);
}
