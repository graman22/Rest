package com.crossover.trial.weather;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;

import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.crossover.trial.config.AppConfig;
import com.crossover.trial.weather.builder.AirportLoader;
import com.crossover.trial.weather.model.AirportData;
import com.crossover.trial.weather.model.AtmosphericInformation;
import com.crossover.trial.weather.model.DataPoint;
import com.crossover.trial.weather.model.DataPointType;
import com.crossover.trial.weather.restservice.RestWeatherCollectorEndpoint;
import com.crossover.trial.weather.restservice.RestWeatherQueryEndpoint;
import com.crossover.trial.weather.restservice.WeatherCollectorEndpoint;
import com.crossover.trial.weather.restservice.WeatherQueryEndpoint;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class WeatherEndpointTest {

	private WeatherQueryEndpoint _query;

	private WeatherCollectorEndpoint _update;

	private Gson _gson = new Gson();

	private DataPoint _dp;

	@Before
	public void setUp() throws Exception {
		//RestWeatherQueryEndpoint.init();
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
		AirportLoader loader = context.getBean(AirportLoader.class);
		loader.loadData();
		
		_query = (WeatherQueryEndpoint)context.getBean(RestWeatherQueryEndpoint.class);
		_update = (WeatherCollectorEndpoint)context.getBean(RestWeatherCollectorEndpoint.class);
		
		_dp = new DataPoint.Builder().withCount(10).withFirst(10).withMedian(20).withLast(30).withMean(22).build();
		_update.updateWeather("BOS", DataPointType.WIND, _gson.toJson(_dp));
		_query.weather("BOS", "0").getEntity();
	}

	@Test
	public void testPing() throws Exception {
		String ping = _query.ping();
		JsonElement pingResult = new JsonParser().parse(ping);
		assertEquals(1, pingResult.getAsJsonObject().get("datasize").getAsInt());
		assertEquals(10, pingResult.getAsJsonObject().get("iata_freq").getAsJsonObject().entrySet().size());
	}
	
	@Test
	public void testCollectPing() throws Exception {
		Response ping = _update.ping();
		assertEquals(ping.getEntity(),"ready");
	}

	@Test
	public void testGet() throws Exception {
		List<AtmosphericInformation> ais = (List<AtmosphericInformation>) _query.weather("BOS", "0").getEntity();
		assertEquals(ais.get(0).getWind(), _dp);
	}

	@Test
	public void testGetNearby() throws Exception {
		// check datasize response
		_update.updateWeather("JFK", DataPointType.WIND, _gson.toJson(_dp));
		_dp.setMean(40);
		_update.updateWeather("EWR", DataPointType.WIND, _gson.toJson(_dp));
		_dp.setMean(30);
		_update.updateWeather("LGA", DataPointType.WIND, _gson.toJson(_dp));

		List<AtmosphericInformation> ais = (List<AtmosphericInformation>) _query.weather("JFK", "200").getEntity();
		assertEquals(3, ais.size());
	}

	@Test
	public void testUpdate() throws Exception {

		DataPoint windDp = new DataPoint.Builder().withCount(10).withFirst(10).withMedian(20).withLast(30).withMean(22)
				.build();
		_update.updateWeather("BOS", DataPointType.WIND, _gson.toJson(windDp));
		_query.weather("BOS", "0").getEntity();

		String ping = _query.ping();
		JsonElement pingResult = new JsonParser().parse(ping);
		assertEquals(1, pingResult.getAsJsonObject().get("datasize").getAsInt());

		DataPoint cloudCoverDp = new DataPoint.Builder().withCount(4).withFirst(10).withMedian(60).withLast(100)
				.withMean(50).build();
		_update.updateWeather("BOS", DataPointType.CLOUDCOVER, _gson.toJson(cloudCoverDp));

		List<AtmosphericInformation> ais = (List<AtmosphericInformation>) _query.weather("BOS", "0").getEntity();
		assertEquals(ais.get(0).getWind(), windDp);
		assertEquals(ais.get(0).getCloudCover(), cloudCoverDp);
	}
	
	@Test
	public void testGetAirports() throws Exception {
		List<AirportData> airports = (List<AirportData>)_update.getAirports().getEntity();
		assertNotNull(airports);
		assertEquals(10,airports.size());
	}
	
	@Test
	public void testGetAirportByIataCode() throws Exception {
		AirportData airportdata = (AirportData)_update.getAirport("BOS").getEntity();
		assertNotNull(airportdata);
		//assertEquals(10,airports.size());
	}
	
	@Test
	public void testdeleteAirport(){
		AirportData airportdata = (AirportData)_update.getAirport("BOS").getEntity();
		assertNotNull("Airport data BOS available",airportdata);
		
		Response response = _update.deleteAirport("BOS");
		assertEquals("Status of deletion",response.getStatus(),Response.Status.OK.getStatusCode());
		
		airportdata = (AirportData)_update.getAirport("BOS").getEntity();
		assertNull("Airport data BOS should not be available as it is deleted",airportdata);		
	}
	
	@Test
	public void testAddAirport(){			
		Response response = _update.addAirport("TEST", "0.0", "0.0");
		assertEquals("Status of Addition",response.getStatus(),Response.Status.OK.getStatusCode());

		AirportData airportdata = (AirportData)_update.getAirport("TEST").getEntity();
		assertNotNull("Airport data TEST which is just inserted is available",airportdata);
		}

}