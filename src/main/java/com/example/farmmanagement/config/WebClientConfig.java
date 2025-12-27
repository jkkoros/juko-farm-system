package com.example.farmmanagement.config;

import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient.Builder openMeteoWebClientBuilder() {
        // Configure underlying HttpClient with timeouts
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(15))
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(15, TimeUnit.SECONDS))
                            .addHandlerLast(new WriteTimeoutHandler(15, TimeUnit.SECONDS))
                );

        return WebClient.builder()
                // Attach the configured HttpClient
                .clientConnector(new ReactorClientHttpConnector(httpClient))

                // === Default Headers (applied to every request) ===
                .defaultHeader(HttpHeaders.USER_AGENT, "JukoCoffeeFactory/1.0 (Kenya Coffee Monitoring)")
                .defaultHeader(HttpHeaders.ACCEPT, "application/json")
                .defaultHeader("X-Request-Source", "Spring-Boot-Weather-Service")

                // Optional: Increase buffer size if you expect larger JSON responses
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)) // 16MB

                // Optional: Enable logging (useful during development)
                // .filter(logFilter()) // Uncomment if you want request/response logging
                ;
    }

    // Optional: Logging filter (for debugging - remove in production or use conditional profile)
    /*
    private ExchangeFilterFunction logFilter() {
        return (request, next) -> {
            System.out.println("Request: " + request.method() + " " + request.url());
            return next.exchange(request)
                    .doOnNext(response -> System.out.println("Response status: " + response.statusCode()));
        };
    }
    */
}
