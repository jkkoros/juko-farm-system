package com.example.farmmanagement.model;

import java.util.List;

/**
 * Response model for Open-Meteo weather API.
 * Uses Java records for clean, type-safe mapping.
 */
public record WeatherResponse(
        CurrentWeather current,
        DailyWeather daily,
        String error  // null if successful
) {
    public record CurrentWeather(
            String time,
            double temperature_2m,
            double relative_humidity_2m,
            double apparent_temperature,
            double precipitation,
            int weather_code,
            double wind_speed_10m
    ) {}

    public record DailyWeather(
            List<String> time,
            List<Double> temperature_2m_max,
            List<Double> temperature_2m_min,
            List<Double> precipitation_sum,
            List<Integer> weather_code
    ) {}

    public boolean isSuccess() {
        return error == null;
    }
}