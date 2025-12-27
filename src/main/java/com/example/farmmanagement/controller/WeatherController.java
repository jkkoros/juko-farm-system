package com.example.farmmanagement.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WeatherController {

    @GetMapping("/weather")
    public String showWeatherDashboard(Model model) {
        model.addAttribute("locationName", "Kericho Coffee Region");
        model.addAttribute("latitude", -0.37);
        model.addAttribute("longitude", 35.29);

        // Add real data later (from service)
        // For now, just show the page
        return "weather-dashboard";
    }
    
    @GetMapping("/weather/current")
    public String showCurrentWeather(Model model) {
        model.addAttribute("locationName", "Kericho");
        // Later: add real current data here
        return "weather-current";
    }

    @GetMapping("/weather/rainfall")
    public String showRainfall(Model model) {
        model.addAttribute("locationName", "Kericho");
        // Later: add real rainfall data
        return "weather-rainfall";
    }

    @GetMapping("/weather/forecast")
    public String showForecast(Model model) {
        model.addAttribute("locationName", "Kericho");
        // Later: add real forecast data
        return "weather-forecast";
    }
}