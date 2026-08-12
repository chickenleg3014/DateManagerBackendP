package org.ict.datemanagerbackend.weather.service;

import tools.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.ict.datemanagerbackend.weather.dto.WeatherResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 기상청 API 허브(초단기실황/초단기예보)를 호출해서 위경도 기준 현재 날씨를 조회하는 서비스.
 *
 * 원래 프론트엔드(src/lib/weather.js)에서 브라우저가 직접 기상청 API를 호출하던 로직을
 * 그대로 백엔드로 옮긴 것. 인증키를 프론트에 노출하지 않기 위해 백엔드가 대신 호출하고,
 * 프론트는 이 서비스가 계산해준 결과(아이콘/기온/설명)만 받아간다.
 */
@Service
@Slf4j
public class WeatherService {

  private static final String BASE_URL = "https://apihub.kma.go.kr/api/typ02/openApi/VilageFcstInfoService_2.0";

  private final RestTemplate restTemplate = new RestTemplate();

  @Value("${kma.auth-key}")
  private String authKey;

  // 기상청 격자(Lambert Conformal Conic) 변환 공식에 쓰이는 고정 상수들
  private static final double DEGRAD = Math.PI / 180.0;
  private static final double RE = 6371.00877; // 지구 반경(km)
  private static final double GRID = 5.0; // 격자 간격(km)
  private static final double SLAT1 = 30.0 * DEGRAD; // 투영 위도1
  private static final double SLAT2 = 60.0 * DEGRAD; // 투영 위도2
  private static final double OLON = 126.0 * DEGRAD; // 기준점 경도
  private static final double OLAT = 38.0 * DEGRAD; // 기준점 위도
  private static final double XO = 43; // 기준점 X좌표(GRID)
  private static final double YO = 136; // 기준점 Y좌표(GRID)

  public WeatherResponse getCurrentWeather(double lat, double lon) {
    int[] grid = latLonToGrid(lat, lon);
    int nx = grid[0];
    int ny = grid[1];

    JsonNode ncstItems = fetchItems("getUltraSrtNcst", ncstBaseDateTime(), nx, ny);
    JsonNode fcstItems = fetchItems("getUltraSrtFcst", fcstBaseDateTime(), nx, ny);

    String temp = findValue(ncstItems, "T1H", "obsrValue");
    String pty = findValue(ncstItems, "PTY", "obsrValue");
    if (pty == null) {
      pty = findNearestFcstValue(fcstItems, "PTY"); // 초단기실황에 값이 없으면 예보값으로 대체
    }
    String sky = findNearestFcstValue(fcstItems, "SKY");

    return describeWeather(sky, pty, temp);
  }

  // 위경도를 기상청 격자 좌표(nx, ny)로 변환 - 기상청이 배포하는 공식 좌표 변환식 그대로 이식
  private int[] latLonToGrid(double lat, double lon) {
    double re = RE / GRID;
    double sn = Math.tan(Math.PI * 0.25 + SLAT2 * 0.5) / Math.tan(Math.PI * 0.25 + SLAT1 * 0.5);
    sn = Math.log(Math.cos(SLAT1) / Math.cos(SLAT2)) / Math.log(sn);
    double sf = Math.tan(Math.PI * 0.25 + SLAT1 * 0.5);
    sf = (Math.pow(sf, sn) * Math.cos(SLAT1)) / sn;
    double ro = Math.tan(Math.PI * 0.25 + OLAT * 0.5);
    ro = (re * sf) / Math.pow(ro, sn);

    double ra = Math.tan(Math.PI * 0.25 + lat * DEGRAD * 0.5);
    ra = (re * sf) / Math.pow(ra, sn);
    double theta = lon * DEGRAD - OLON;
    if (theta > Math.PI) theta -= 2.0 * Math.PI;
    if (theta < -Math.PI) theta += 2.0 * Math.PI;
    theta *= sn;

    int nx = (int) Math.floor(ra * Math.sin(theta) + XO + 0.5);
    int ny = (int) Math.floor(ro - ra * Math.cos(theta) + YO + 0.5);
    return new int[]{nx, ny};
  }

  // 초단기실황은 매시 40분에 생성되므로, 40분을 당겨서 아직 안 나온 시각을 조회하지 않도록 함
  private String[] ncstBaseDateTime() {
    LocalDateTime d = LocalDateTime.now().minusMinutes(40);
    return new String[]{
        d.format(DateTimeFormatter.ofPattern("yyyyMMdd")),
        d.format(DateTimeFormatter.ofPattern("HH")) + "00"
    };
  }

  // 초단기예보는 매시 30분에 생성 -> base_time은 항상 xx30
  private String[] fcstBaseDateTime() {
    LocalDateTime d = LocalDateTime.now().minusMinutes(45);
    return new String[]{
        d.format(DateTimeFormatter.ofPattern("yyyyMMdd")),
        d.format(DateTimeFormatter.ofPattern("HH")) + "30"
    };
  }

  private JsonNode fetchItems(String endpoint, String[] baseDateTime, int nx, int ny) {
    String url = UriComponentsBuilder.fromUriString(BASE_URL + "/" + endpoint)
        .queryParam("pageNo", 1)
        .queryParam("numOfRows", 100)
        .queryParam("dataType", "JSON")
        .queryParam("base_date", baseDateTime[0])
        .queryParam("base_time", baseDateTime[1])
        .queryParam("nx", nx)
        .queryParam("ny", ny)
        .queryParam("authKey", authKey)
        .toUriString();

    try {
      JsonNode root = restTemplate.getForObject(url, JsonNode.class);
      if (root == null) return null;
      return root.path("response").path("body").path("items").path("item");
    } catch (Exception e) {
      log.error("기상청 API 호출 실패: {}", endpoint, e);
      return null;
    }
  }

  private String findValue(JsonNode items, String category, String valueField) {
    if (items == null) return null;
    for (JsonNode item : items) {
      if (category.equals(item.path("category").asText())) {
        return item.path(valueField).asText(null);
      }
    }
    return null;
  }

  // 초단기예보는 여러 미래 시각이 섞여 오므로, fcstTime이 가장 이른(=가장 가까운) 항목을 사용
  private String findNearestFcstValue(JsonNode items, String category) {
    if (items == null) return null;
    String result = null;
    int minTime = Integer.MAX_VALUE;
    for (JsonNode item : items) {
      if (category.equals(item.path("category").asText())) {
        int fcstTime = item.path("fcstTime").asInt();
        if (fcstTime < minTime) {
          minTime = fcstTime;
          result = item.path("fcstValue").asText(null);
        }
      }
    }
    return result;
  }

  private WeatherResponse describeWeather(String sky, String pty, String temp) {
    String icon;
    String desc;
    if ("1".equals(pty) || "5".equals(pty)) {
      icon = "🌧️"; desc = "비가 와요, 실내 데이트 어때요";
    } else if ("2".equals(pty) || "6".equals(pty)) {
      icon = "🌨️"; desc = "비와 눈이 섞여 내려요";
    } else if ("3".equals(pty) || "7".equals(pty)) {
      icon = "❄️"; desc = "눈 내리는 날";
    } else if ("1".equals(sky)) {
      icon = "☀️"; desc = "맑고 산책하기 좋은 날";
    } else if ("3".equals(sky)) {
      icon = "⛅"; desc = "구름 많은 날";
    } else if ("4".equals(sky)) {
      icon = "☁️"; desc = "흐린 날";
    } else {
      icon = "🌤️"; desc = "오늘의 날씨";
    }
    String tempText = temp != null ? temp + "°C" : "—";
    return new WeatherResponse(icon, tempText, desc);
  }
}
