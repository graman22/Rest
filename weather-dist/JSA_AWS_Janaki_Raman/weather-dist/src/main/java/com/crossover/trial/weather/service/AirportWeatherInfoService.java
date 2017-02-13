package com.crossover.trial.weather.service;

import java.util.Map;

import com.crossover.trial.weather.model.AirportData;
import com.crossover.trial.weather.model.AtmosphericInformation;
import com.crossover.trial.weather.model.DataPoint;
import com.crossover.trial.weather.model.DataPointType;

public interface AirportWeatherInfoService
{
	public boolean insertNewAirport(String iataCode, double lat, double longitude);

	public Map<String, AirportData> findAllAirports();

	public AirportData findAirport(String iataCode);

	public boolean deleteAirport(String iataCode);
	
	public Map<String,AtmosphericInformation> getAtmosphereInfoOfAllAirports();
	
	public AtmosphericInformation findAtmosphericInformation(String iataCode);	

	public void addDataPoint(String iataCode, DataPointType pointType, DataPoint dp) throws AirportWeatherServiceException;

	public void updateAtmosphericInformation(AtmosphericInformation ai, String pointType, DataPoint dp) throws AirportWeatherServiceException;
	
	public void reset();

}
