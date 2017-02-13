package com.crossover.trial.weather.builder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import com.crossover.trial.weather.restservice.RestWeatherCollectorEndpoint;
import com.crossover.trial.weather.service.AirportWeatherInfoService;
import com.crossover.trial.weather.service.AirportWeatherServiceException;

/**
 * A simple airport loader which reads a file from disk and sends entries to the
 * webservice
 *
 * TODO: Implement the Airport Loader
 * 
 * @author code test administrator
 */
@Service
public class AirportLoader {

	final static Logger logger = Logger.getLogger(AirportLoader.class);

	@Value("${weather.datafilename}")
	private String FILE_NAME;

	@Autowired
	AirportWeatherInfoService airportWeatherService;

	public AirportLoader() {
	}

	public void upload(InputStream airportDataStream) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(airportDataStream));
		String airportdata = null;
		// cleanup();

		while ((airportdata = reader.readLine()) != null) {
			// as the data contains quotes on iata code airport name
			airportdata = airportdata.replace("\"", "");
			String data[] = airportdata.split(",");
			logger.debug("Inserting For -- iata " + data[4] + " latitude " + data[6] + " longitude " + data[7]);

			String iataCode = data[4];
			double latitude = Double.valueOf(data[6]);
			double longitude = Double.valueOf(data[7]);

			airportWeatherService.insertNewAirport(iataCode, latitude, longitude);
		}
	}

	private void cleanup() {
		airportWeatherService.reset();
	}

	public void loadData() throws AirportWeatherServiceException {
		logger.info("Loading data from the file :" + FILE_NAME);

		try {
			cleanup();
			InputStream airportDataStream = Thread.currentThread().getContextClassLoader()
					.getResourceAsStream(FILE_NAME);
			upload(airportDataStream);

			logger.info("Loaded data from the file :" + FILE_NAME + " Sucessfully!");
		} catch (IOException e) {
			logger.error(e.getMessage(), e.getCause());
			throw new AirportWeatherServiceException(e.getMessage());
		}

	}

	public static void main(String args[]) throws IOException {

		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		InputStream airportDataStream = loader.getResourceAsStream(args[0]);

		AirportLoader al = new AirportLoader();
		al.upload(airportDataStream);
		System.exit(0);
	}
}
