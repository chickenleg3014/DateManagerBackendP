package org.ict.datemanagerbackend.weather.controller;

import lombok.RequiredArgsConstructor;
import org.ict.datemanagerbackend.weather.dto.WeatherResponse;
import org.ict.datemanagerbackend.weather.service.WeatherService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/weather")
@RequiredArgsConstructor
public class WeatherController {

  private final WeatherService weatherService;

  // /api/**는 SecurityConfig에서 기본적으로 로그인(JWT)이 있어야 접근 가능하도록 되어 있음
  @GetMapping
  public WeatherResponse getWeather(@RequestParam double lat, @RequestParam double lon) {
    return weatherService.getCurrentWeather(lat, lon);
  }
}
