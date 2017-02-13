package com.crossover.trial.weather.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import com.crossover.trial.weather.model.AirportData;
import com.crossover.trial.weather.model.AtmosphericInformation;
import com.crossover.trial.weather.model.DataPoint;
import com.crossover.trial.weather.model.DataPointType;
import com.crossover.trial.weather.restservice.RestWeatherCollectorEndpoint;

@Service
public class AirportWeatherInfoServiceImpl implements AirportWeatherInfoService {

	final static Logger logger = Logger.getLogger(AirportWeatherInfoServiceImpl.class);

	/** all known airports */
	private Map<String, AirportData> airportDatas = new ConcurrentHashMap<String, AirportData>();

	/**
	 * atmospheric information for each airport, idx corresponds with
	 * airportData
	 */
	private Map<String, AtmosphericInformation> atmosphericInformations = new ConcurrentHashMap<String, AtmosphericInformation>();

	public AirportWeatherInfoServiceImpl() {
	}

	public boolean insertNewAirport(String iataCode, double lat, double longitude) {
		AirportData airportData = constructAirport(iataCode, lat, longitude);
		AirportData previous = airportDatas.putIfAbsent(iataCode, airportData);

		if (previous == null) {
			AtmosphericInformation ai = new AtmosphericInformation();
			atmosphericInformations.putIfAbsent(iataCode, ai);
			return true;
		} else {
			return false;
		}
	}

	public Map<String, AirportData> findAllAirports() {
		return airportDatas;
	}

	public AirportData findAirport(String iataCode) {
		AirportData airportData = airportDatas.get(iataCode);
		return airportData;
	}

	public boolean deleteAirport(String iataCode) {
		AirportData airportData = airportDatas.get(iataCode);

		if (airportData != null) {
			airportDatas.remove(iataCode);
			return true;
		} else {
			return false;
		}
	}

	public Map<String, AtmosphericInformation> getAtmosphereInfoOfAllAirports() {
		return atmosphericInformations;
	}

	public AtmosphericInformation findAtmosphericInformation(String iataCode) {
		return atmosphericInformations.get(iataCode);
	}

	private AirportData constructAirport(String iataCode, double latitude, double longitude) {
		AirportData ad = new AirportData();
		ad.setIata(iataCode);
		ad.setLatitude(latitude);
		ad.setLatitude(longitude);
		return ad;
	}

	public void addDataPoint(String iataCode, DataPointType pointType, DataPoint dp)
			throws AirportWeatherServiceException {
		AtmosphericInformation ai = findAtmosphericInformation(iataCode);
		updateAtmosphericInformation(ai, pointType.name(), dp);
	}

	/**
	 * update atmospheric information with the given data point for the given
	 * point type
	 *
	 * @param ai
	 *            the atmospheric information object to update
	 * @param pointType
	 *            the data point type as a string
	 * @param dp
	 *            the actual data point
	 */
	public void updateAtmosphericInformation(AtmosphericInformation ai, String pointType, DataPoint dp)
			throws AirportWeatherServiceException {
		final DataPointType dptype = DataPointType.valueOf(pointType.toUpperCase());

		if (pointType.equalsIgnoreCase(DataPointType.WIND.name())) {
			if (dp.getMean() >= 0) {
				ai.setWind(dp);
				ai.setLastUpdateTime(System.currentTimeMillis());
				return;
			}
		}

		if (pointType.equalsIgnoreCase(DataPointType.TEMPERATURE.name())) {
			if (dp.getMean() >= -50 && dp.getMean() < 100) {
				ai.setTemperature(dp);
				ai.setLastUpdateTime(System.currentTimeMillis());
				return;
			}
		}

		if (pointType.equalsIgnoreCase(DataPointType.HUMIDTY.name())) {
			if (dp.getMean() >= 0 && dp.getMean() < 100) {
				ai.setHumidity(dp);
				ai.setLastUpdateTime(System.currentTimeMillis());
				return;
			}
		}

		if (pointType.equalsIgnoreCase(DataPointType.PRESSURE.name())) {
			if (dp.getMean() >= 650 && dp.getMean() < 800) {
				ai.setPressure(dp);
				ai.setLastUpdateTime(System.currentTimeMillis());
				return;
			}
		}

		if (pointType.equalsIgnoreCase(DataPointType.CLOUDCOVER.name())) {
			if (dp.getMean() >= 0 && dp.getMean() < 100) {
				ai.setCloudCover(dp);
				ai.setLastUpdateTime(System.currentTimeMillis());
				return;
			}
		}

		if (pointType.equalsIgnoreCase(DataPointType.PRECIPITATION.name())) {
			if (dp.getMean() >= 0 && dp.getMean() < 100) {
				ai.setPrecipitation(dp);
				ai.setLastUpdateTime(System.currentTimeMillis());
				return;
			}
		}

		logger.error("couldn't update atmospheric data");
		throw new AirportWeatherServiceException("couldn't update atmospheric data");
	}

	public void reset() {
		airportDatas.clear();
		atmosphericInformations.clear();
	}

}
