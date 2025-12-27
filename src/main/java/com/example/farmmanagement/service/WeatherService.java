package com.example.farmmanagement.service;

import com.example.farmmanagement.model.WeatherResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class WeatherService {

    private final WebClient webClient;

    public WeatherService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder
                .baseUrl("https://api.open-meteo.com")
                .defaultHeader("User-Agent", "JukoCoffeeFactory/1.0")
                .build();
    }

    public Mono<WeatherResponse> getWeather(double latitude, double longitude) {
        String url = "/v1/forecast" +
                "?latitude=" + latitude +
                "&longitude=" + longitude +
                "&current=temperature_2m,relative_humidity_2m,apparent_temperature,precipitation,weather_code,wind_speed_10m" +
                "&daily=weather_code,temperature_2m_max,temperature_2m_min,precipitation_sum" +
                "&timezone=Africa/Nairobi" +
                "&forecast_days=7";

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(WeatherResponse.class)
                .onErrorResume(e -> Mono.just(
                        new WeatherResponse(null, null, "Weather service error: " + e.getMessage())
                ));
    }

    public Mono<Double> getRainLast30Days(double latitude, double longitude) {
        LocalDate today = LocalDate.now();
        LocalDate start = today.minusDays(30);
        String url = "/v1/archive" +
                "?latitude=" + latitude +
                "&longitude=" + longitude +
                "&start_date=" + start.format(DateTimeFormatter.ISO_LOCAL_DATE) +
                "&end_date=" + today.format(DateTimeFormatter.ISO_LOCAL_DATE) +
                "&daily=precipitation_sum" +
                "&timezone=Africa/Nairobi";

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(WeatherResponse.class)
                .map(response -> {
                    if (response.daily() != null && response.daily().precipitation_sum() != null) {
                        return response.daily().precipitation_sum().stream()
                                .mapToDouble(Double::doubleValue)
                                .sum();
                    }
                    return 0.0;
                })
                .onErrorReturn(0.0);
    }
}