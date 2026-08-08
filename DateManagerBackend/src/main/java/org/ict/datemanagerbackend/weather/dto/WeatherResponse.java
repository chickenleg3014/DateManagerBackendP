package org.ict.datemanagerbackend.weather.dto;

// 프론트 홈 화면 날씨 칩에 그대로 꽂아 쓸 수 있는 형태 (icon, temp, desc)
public record WeatherResponse(String icon, String temp, String desc) {
}
