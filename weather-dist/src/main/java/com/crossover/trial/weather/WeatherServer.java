package com.crossover.trial.weather;

import static java.lang.String.format;

import java.io.IOException;
import java.net.URI;

import org.apache.log4j.Logger;
import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.HttpServerFilter;
import org.glassfish.grizzly.http.server.HttpServerProbe;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;

import com.crossover.trial.config.AppConfig;
import com.crossover.trial.weather.builder.AirportLoader;
import com.crossover.trial.weather.restservice.RestWeatherCollectorEndpoint;
import com.crossover.trial.weather.restservice.RestWeatherQueryEndpoint;
import com.crossover.trial.weather.service.AirportWeatherServiceException;

/**
 * This main method will be use by the automated functional grader. You
 * shouldn't move this class or remove the main method. You may change the
 * implementation, but we encourage caution.
 *
 * @author code test administrator
 */
@Service
public class WeatherServer {
	
	final static Logger logger = Logger.getLogger(WeatherServer.class);


	@Value("${application.url}")
	private String BASE_URL;

	private static void loadData(AirportLoader loader) throws AirportWeatherServiceException {
		loader.loadData();
	}

	public static void main(String[] args) {
		try {
			AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);

			AirportLoader loader = context.getBean(AirportLoader.class);
			loadData(loader);

			WeatherServer weatherServer = context.getBean(WeatherServer.class);
			System.out.println("Starting Weather App local testing server: " + weatherServer.BASE_URL);

			final ResourceConfig resourceConfig = new ResourceConfig();
			resourceConfig.register(context.getBean(RestWeatherCollectorEndpoint.class));
			resourceConfig.register(context.getBean(RestWeatherQueryEndpoint.class));

			HttpServer server = GrizzlyHttpServerFactory.createHttpServer(URI.create(weatherServer.BASE_URL),
					resourceConfig, false);
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				server.shutdownNow();
			}));

			HttpServerProbe probe = new HttpServerProbe.Adapter() {
				public void onRequestReceiveEvent(HttpServerFilter filter, Connection connection, Request request) {
					System.out.println(request.getRequestURI());
				}
			};
			server.getServerConfiguration().getMonitoringConfig().getWebServerConfig().addProbes(probe);

			// the autograder waits for this output before running automated
			// tests, please don't remove it
			server.start();
			System.out.println(format("Weather Server started.\n url=%s\n", weatherServer.BASE_URL));

			// blocks until the process is terminated
			Thread.currentThread().join();
			server.shutdown();
		} catch (IOException | InterruptedException | AirportWeatherServiceException ex) {
			ex.printStackTrace();
			logger.error(ex.getMessage());			
		}
	}
}
