package org.ict.datemanagerbackend.domain.place.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ict.datemanagerbackend.domain.place.dto.TourApiPlaceDto;
import org.ict.datemanagerbackend.domain.place.entity.Place;
import org.ict.datemanagerbackend.domain.place.repository.PlaceRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 한국관광공사 TourAPI(국문 관광정보서비스, KorService2)의 지역기반 목록조회(areaBasedList2)에서
 * 장소 데이터를 받아와 places 테이블에 채워 넣는 서비스.
 *
 * PlaceSyncService(KOPIS 공연 동기화)와 동일하게 external_source+external_id로 upsert한다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TourApiSyncService {

  private static final String LIST_URL = "https://apis.data.go.kr/B551011/KorService2/areaBasedList2";

  // 우리 DB에서 "이 데이터가 TourAPI에서 왔다"는 걸 표시하기 위한 값 (Place.externalSource에 저장됨)
  private static final String EXTERNAL_SOURCE = "TOURAPI";

  // TourAPI 콘텐츠타입ID -> 우리 서비스 카테고리명 (contenttypeid는 목록 API에서 그대로 내려주지 않는 경우가
  // 있어, 호출할 때 지정한 contentTypeId를 그대로 카테고리로 사용한다)
  private static final Map<String, String> CATEGORY_BY_CONTENT_TYPE = Map.of(
      "12", "관광지",
      "14", "문화시설",
      "15", "공연",
      "28", "액티비티",
      "38", "쇼핑",
      "39", "맛집"
  );

  private final PlaceRepository placeRepository;
  private final RestTemplate restTemplate = new RestTemplate();

  // application.yaml의 tourapi.service-key 값을 그대로 주입받음 (yaml -> .env의 TOUR_API_SERVICE_KEY로 연결됨)
  @Value("${tourapi.service-key}")
  private String serviceKey;

  /**
   * 매일 새벽 4시에 자동 실행됨 (KOPIS 동기화가 새벽 3시라 시간을 겹치지 않게 뒀다).
   */
  @Scheduled(cron = "0 0 4 * * *")
  public void syncPlaces() {
    int created = 0;
    int updated = 0;

    for (Map.Entry<String, String> entry : CATEGORY_BY_CONTENT_TYPE.entrySet()) {
      String contentTypeId = entry.getKey();
      String category = entry.getValue();
      List<TourApiPlaceDto> places = fetchPlaces(contentTypeId);

      for (TourApiPlaceDto p : places) {
        Optional<Place> existing =
            placeRepository.findByExternalSourceAndExternalId(EXTERNAL_SOURCE, p.contentid());

        Double lat = parseCoordinate(p.mapy());
        Double lng = parseCoordinate(p.mapx());
        String image = !p.firstimage().isBlank() ? p.firstimage() : p.firstimage2();
        String address = p.addr2().isBlank() ? p.addr1() : (p.addr1() + " " + p.addr2()).trim();

        if (existing.isPresent()) {
          Place place = existing.get();
          place.setName(p.title());
          place.setCategory(category);
          place.setAddress(address);
          place.setLatitude(lat);
          place.setLongitude(lng);
          place.setImageUrl(image);
          placeRepository.save(place);
          updated++;
        } else {
          Place place = Place.builder()
              .name(p.title())
              .category(category)
              .address(address)
              .latitude(lat)
              .longitude(lng)
              .imageUrl(image)
              .externalSource(EXTERNAL_SOURCE)
              .externalId(p.contentid())
              .build();
          placeRepository.save(place);
          created++;
        }
      }
    }

    log.info("TourAPI 장소 동기화 완료 - 신규 {}건, 갱신 {}건", created, updated);

    // TODO: 전화번호, 가격/주차 정보는 이 목록 API(areaBasedList2)에는 안 들어있음.
    //       필요하면 contentid로 상세조회 API(detailIntro2)를 한 번 더 호출해서
    //       PlaceReality.priceText/parkingInfo를 채우는 로직을 추가할 것.
  }

  /** contentTypeId 하나에 대해 지역기반 목록(최대 100건)을 받아옴 */
  private List<TourApiPlaceDto> fetchPlaces(String contentTypeId) {
    String url = UriComponentsBuilder.fromUriString(LIST_URL)
        .queryParam("serviceKey", serviceKey)
        .queryParam("MobileOS", "ETC")
        .queryParam("MobileApp", "DateManager")
        .queryParam("_type", "json")
        .queryParam("numOfRows", 100)
        .queryParam("pageNo", 1)
        .queryParam("contentTypeId", contentTypeId)
        .toUriString();

    try {
      JsonNode root = restTemplate.getForObject(url, JsonNode.class);
      if (root == null) return List.of();

      JsonNode items = root.path("response").path("body").path("items").path("item");
      List<TourApiPlaceDto> result = new ArrayList<>();
      for (JsonNode item : items) {
        String contentId = item.path("contentid").asText("");
        String title = item.path("title").asText("");
        if (contentId.isBlank() || title.isBlank()) continue; // 필수 정보 없는 항목은 건너뜀

        result.add(new TourApiPlaceDto(
            contentId,
            title,
            item.path("addr1").asText(""),
            item.path("addr2").asText(""),
            item.path("mapx").asText(""),
            item.path("mapy").asText(""),
            item.path("firstimage").asText(""),
            item.path("firstimage2").asText("")
        ));
      }
      return result;
    } catch (Exception e) {
      log.error("TourAPI 호출 실패 (contentTypeId={})", contentTypeId, e);
      return List.of();
    }
  }

  private Double parseCoordinate(String value) {
    if (value == null || value.isBlank()) return null;
    try {
      return Double.parseDouble(value);
    } catch (NumberFormatException e) {
      return null;
    }
  }
}
