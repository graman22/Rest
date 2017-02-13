package com.crossover.trial.weather.restservice;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.crossover.trial.weather.model.AirportData;
import com.crossover.trial.weather.model.DataPoint;
import com.crossover.trial.weather.model.DataPointType;
import com.crossover.trial.weather.service.AirportWeatherInfoService;
import com.crossover.trial.weather.service.AirportWeatherServiceException;
import com.google.gson.Gson;

/**
 * A REST implementation of the WeatherCollector API. Accessible only to airport
 * weather collection sites via secure VPN.
 *
 * @author code test administrator
 */

@Path("/collect")
@Service
public class RestWeatherCollectorEndpoint implements WeatherCollectorEndpoint {
	
	final static Logger logger = Logger.getLogger(RestWeatherCollectorEndpoint.class);

	/** shared gson json to object factory */
	public final static Gson gson = new Gson();

	@Autowired
	AirportWeatherInfoService airportWeatherService;

	@Override
	public Response ping() {
		logger.info("In Ping of Collector End point");
		return Response.status(Response.Status.OK).entity("ready").build();
	}

	@Override
	public Response updateWeather(@PathParam("iata") String iataCode, @PathParam("pointType") DataPointType pointType,
			String datapointJson) {
		
		logger.info("In updateWeather for " + iataCode);
		try {
			airportWeatherService.addDataPoint(iataCode, pointType, gson.fromJson(datapointJson, DataPoint.class));
		} catch (AirportWeatherServiceException e) {
			logger.error(e.getMessage(), e.getCause());
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		}
		return Response.status(Response.Status.OK).build();
	}

	@Override
	public Response getAirports() {
		logger.info("Before reteriving All Airports Info");
		List<AirportData> airpoList = new ArrayList<AirportData>(airportWeatherService.findAllAirports().values());
		return Response.status(Response.Status.OK).entity(airpoList).build();
	}

	@Override
	public Response getAirport(@PathParam("iata") String iata) {
		logger.info("Before reteriving Airport Info for "+ iata);
		AirportData ad = airportWeatherService.findAirport(iata);
		return Response.status(Response.Status.OK).entity(ad).build();
	}

	@Override
	public Response addAirport(@PathParam("iata") String iata, @PathParam("lat") String latString,
			@PathParam("long") String longString) {
		logger.info("Before Adding Airport Info for "+ iata);
		boolean isSucess = airportWeatherService.insertNewAirport(iata, Double.valueOf(latString),
				Double.valueOf(longString));

		Status status = null;
		if (isSucess) {
			status = Response.Status.OK;
		} else {
			status = Response.Status.BAD_REQUEST;
			logger.error("Adding Airport Info for "+ iata+  " is Not Sucessfull");
		}		
		return Response.status(status).entity("Airport data Already Exists").build();
	}

	@Override
	public Response deleteAirport(@PathParam("iata") String iata) {

		logger.info("Deleteing Airport Info for "+ iata);

		boolean isSucess = airportWeatherService.deleteAirport(iata);

		Status status = null;
		if (isSucess) {
			status = Response.Status.OK;
		} else {
			status = Response.Status.NOT_FOUND;
			logger.error("Deleting Airport Info for "+ iata+  " is Not Sucessfull");
		}
		return Response.status(status).build();
	}

	@Override
	public Response exit() {
		System.exit(0);
		return Response.noContent().build();
	}

}
