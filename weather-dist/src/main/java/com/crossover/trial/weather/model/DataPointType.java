package com.crossover.trial.weather.model;

/**
 * The various types of data points we can collect.
 *
 * @author code test administrator
 */
public enum DataPointType {
    WIND,
    TEMPERATURE,
    HUMIDTY,
    PRESSURE,
    CLOUDCOVER,
    PRECIPITATION;
    
    public static DataPointType fromString(final String s) {
        return DataPointType.valueOf(s);
    }
}
