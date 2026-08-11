package org.ict.datemanagerbackend.domain.place.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ict.datemanagerbackend.domain.place.dto.KopisFacilityDto;
import org.ict.datemanagerbackend.domain.place.dto.KopisPerformanceDto;
import org.ict.datemanagerbackend.domain.place.entity.Place;
import org.ict.datemanagerbackend.domain.place.repository.PlaceRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * KOPIS(공연예술통합전산망) 공연 목록 API에서 데이터를 받아와 places 테이블에 채워 넣는 서비스.
 *
 * 원래 파이썬 스크립트(fetch_performance_data.py)로 하던 걸 그대로 백엔드 안으로 옮긴 것.
 * 흐름: [1] KOPIS 목록에 HTTP 요청 → [2] 공연별 상세조회로 공연시설 ID(mt10id) 확보
 *      → [3] 시설 ID별로 상세조회를 한 번씩만 호출해 주소/위경도 확보(같은 시설은 캐시로 재사용)
 *      → [4] 이미 저장된 공연이면 갱신, 처음 보는 공연이면 새로 저장(upsert)
 */
@Service
@RequiredArgsConstructor // final 필드(placeRepository)를 인자로 받는 생성자를 롬복이 자동으로 만들어줌 (의존성 주입용)
@Slf4j // log.info(), log.error() 같은 로깅 기능을 롬복이 자동으로 만들어줌
public class PlaceSyncService {

  // KOPIS 공연목록/상세조회(pblprfr) API의 고정 주소 - 목록은 그대로, 상세는 뒤에 /{mt20id}를 붙여서 호출
  private static final String KOPIS_LIST_URL = "http://www.kopis.or.kr/openApi/restful/pblprfr";

  // KOPIS 공연시설 상세조회 API - mt10id로 주소/위경도를 얻기 위해 사용
  private static final String KOPIS_FACILITY_URL = "http://www.kopis.or.kr/openApi/restful/prfplc";

  // 우리 DB에서 "이 데이터가 KOPIS에서 왔다"는 걸 표시하기 위한 값 (Place.externalSource에 저장됨)
  private static final String EXTERNAL_SOURCE = "KOPIS";

  private final PlaceRepository placeRepository;

  // 외부 API를 HTTP로 호출할 때 쓰는 스프링 기본 도구. new로 직접 생성해도 됨 (설정이 단순한 경우)
  private final RestTemplate restTemplate = new RestTemplate();

  // application.yaml의 kopis.service-key 값을 그대로 주입받음 (yaml -> .env의 KOPIS_SERVICE_KEY로 연결됨)
  @Value("${kopis.service-key}")
  private String serviceKey;

  /**
   * 매일 새벽 3시에 자동 실행됨 (cron 표현식: 초 분 시 일 월 요일).
   * cron이 실제로 동작하려면 메인 애플리케이션 클래스에 @EnableScheduling이 붙어있어야 함.
   */
  @Scheduled(cron = "0 0 3 * * *")
  public void syncPerformances() {
    List<KopisPerformanceDto> performances = fetchPerformances();

    // 같은 공연시설(mt10id)을 여러 공연이 공유하는 경우가 많아, 시설 상세조회는 한 번만 호출하고 캐시해서 재사용
    Map<String, KopisFacilityDto> facilityCache = new HashMap<>();

    int created = 0;
    int updated = 0;

    for (KopisPerformanceDto p : performances) {
      // 이미 저장된 공연인지, external_source + external_id 조합으로 확인 (KOPIS의 mt20id가 고유 식별자)
      Optional<Place> existing =
          placeRepository.findByExternalSourceAndExternalId(EXTERNAL_SOURCE, p.mt20id());

      String mt10id = fetchMt10id(p.mt20id());
      KopisFacilityDto facility = mt10id == null
          ? null
          : facilityCache.computeIfAbsent(mt10id, this::fetchFacility);

      String address = facility != null ? facility.adres() : null;
      Double lat = facility != null ? parseCoordinate(facility.la()) : null;
      Double lng = facility != null ? parseCoordinate(facility.lo()) : null;

      if (existing.isPresent()) {
        // 이미 있으면 새로 만들지 않고, 이름/카테고리/포스터/주소/위경도를 최신 값으로 덮어씀 (update)
        Place place = existing.get();
        place.setName(p.prfnm());
        place.setCategory(p.genrenm());
        place.setImageUrl(p.poster());
        if (address != null) place.setAddress(address);
        if (lat != null) place.setLatitude(lat);
        if (lng != null) place.setLongitude(lng);
        placeRepository.save(place);
        updated++;
      } else {
        // 처음 보는 공연이면 새 Place 행을 만듦 (insert)
        Place place = Place.builder()
            .name(p.prfnm())
            .category(p.genrenm())
            .address(address)
            .latitude(lat)
            .longitude(lng)
            .imageUrl(p.poster())
            .externalSource(EXTERNAL_SOURCE)
            .externalId(p.mt20id())
            .build();
        placeRepository.save(place);
        created++;
      }
    }

    log.info("KOPIS 공연 동기화 완료 - 신규 {}건, 갱신 {}건 (전체 조회 {}건, 시설 {}곳 조회)",
        created, updated, performances.size(), facilityCache.size());
  }

  /** KOPIS API를 호출해서 앞으로 30일간의 공연 목록을 받아옴 */
  // KOPIS가 한 번에 내려주는 최대 건수(그 이상 요청하면 "최대 조회수는 100건까지 가능합니다" 에러가 남)
  private static final int KOPIS_PAGE_SIZE = 100;

  // 무한 루프 방지용 안전장치. 페이지당 100건 * 30페이지 = 최대 3000건까지 수집 (평소 30일치가
  // 600~700건 수준으로 확인되어 충분히 여유 있게 잡음)
  private static final int KOPIS_MAX_PAGES = 30;

  /**
   * KOPIS는 한 번의 요청으로 최대 100건만 내려주기 때문에(rows 파라미터 상한), 앞으로 30일간의
   * 공연이 100건을 넘으면 cpage(페이지 번호)를 1씩 늘려가며 여러 번 호출해서 전부 모아야 한다.
   * "이번 페이지 응답이 정확히 100건"이면 다음 페이지에 더 남아있을 가능성이 있다는 뜻이고,
   * 100건보다 적게(또는 0건) 오면 그게 마지막 페이지라는 뜻이라 그때 반복을 멈춘다.
   */
  private List<KopisPerformanceDto> fetchPerformances() {
    LocalDate today = LocalDate.now();
    // KOPIS는 날짜를 "yyyyMMdd" 형식 문자열로 받음 (예: 20260801)
    String stdate = today.format(DateTimeFormatter.BASIC_ISO_DATE);
    String eddate = today.plusDays(30).format(DateTimeFormatter.BASIC_ISO_DATE);

    List<KopisPerformanceDto> all = new ArrayList<>();

    for (int page = 1; page <= KOPIS_MAX_PAGES; page++) {
      // UriComponentsBuilder: URL에 ?key=value&key2=value2... 를 안전하게(URL 인코딩 포함) 붙여주는 도구
      String url = UriComponentsBuilder.fromUriString(KOPIS_LIST_URL)
          .queryParam("service", serviceKey)
          .queryParam("stdate", stdate)
          .queryParam("eddate", eddate)
          .queryParam("cpage", page)
          .queryParam("rows", KOPIS_PAGE_SIZE)
          .toUriString();

      // GET 요청을 보내고, 응답 body를 그대로 문자열(XML)로 받음
      String xml = restTemplate.getForObject(url, String.class);
      List<KopisPerformanceDto> pageItems = parsePerformances(xml);
      all.addAll(pageItems);

      if (pageItems.size() < KOPIS_PAGE_SIZE) {
        break; // 마지막 페이지까지 다 읽음
      }
    }

    return all;
  }

  /** 공연 상세조회(mt20id)를 호출해 그 공연이 열리는 시설의 ID(mt10id)를 얻어옴 */
  private String fetchMt10id(String mt20id) {
    String url = UriComponentsBuilder.fromUriString(KOPIS_LIST_URL + "/" + mt20id)
        .queryParam("service", serviceKey)
        .toUriString();

    try {
      String xml = restTemplate.getForObject(url, String.class);
      Document doc = parseXml(xml);
      if (doc == null) return null;

      NodeList items = doc.getElementsByTagName("db");
      if (items.getLength() == 0) return null;

      return text((Element) items.item(0), "mt10id");
    } catch (Exception e) {
      log.error("KOPIS 공연 상세조회 실패 (mt20id={})", mt20id, e);
      return null;
    }
  }

  /** 공연시설 상세조회(mt10id)를 호출해 주소/위경도를 얻어옴 */
  private KopisFacilityDto fetchFacility(String mt10id) {
    String url = UriComponentsBuilder.fromUriString(KOPIS_FACILITY_URL + "/" + mt10id)
        .queryParam("service", serviceKey)
        .toUriString();

    try {
      String xml = restTemplate.getForObject(url, String.class);
      Document doc = parseXml(xml);
      if (doc == null) return null;

      NodeList items = doc.getElementsByTagName("db");
      if (items.getLength() == 0) return null;

      Element el = (Element) items.item(0);
      return new KopisFacilityDto(mt10id, text(el, "adres"), text(el, "la"), text(el, "lo"));
    } catch (Exception e) {
      log.error("KOPIS 공연시설 상세조회 실패 (mt10id={})", mt10id, e);
      return null;
    }
  }

  /** KOPIS가 돌려준 XML 문자열을 파싱해서 우리가 쓰기 편한 객체 리스트로 바꿔줌 */
  private List<KopisPerformanceDto> parsePerformances(String xml) {
    List<KopisPerformanceDto> result = new ArrayList<>();
    Document doc = parseXml(xml);
    if (doc == null) return result;

    // KOPIS 응답 구조: <dbs><db>...공연 1건...</db><db>...공연 2건...</db></dbs>
    // "db" 태그 하나하나가 공연 1건에 해당함
    NodeList items = doc.getElementsByTagName("db");

    for (int i = 0; i < items.getLength(); i++) {
      Element el = (Element) items.item(i);
      result.add(new KopisPerformanceDto(
          text(el, "mt20id"),
          text(el, "prfnm"),
          text(el, "fcltynm"),
          text(el, "poster"),
          text(el, "genrenm")
      ));
    }

    return result;
  }

  /** XML 문자열을 DOM Document로 파싱하는 공통 로직 (악성 XML 방지를 위해 DOCTYPE 선언 금지) */
  private Document parseXml(String xml) {
    if (xml == null || xml.isBlank()) {
      return null;
    }

    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      DocumentBuilder builder = factory.newDocumentBuilder();
      return builder.parse(new InputSource(new StringReader(xml)));
    } catch (Exception e) {
      log.error("KOPIS 응답 파싱 실패", e);
      return null;
    }
  }

  /** <db> 안에서 특정 태그(tag)의 텍스트 값만 꺼내는 헬퍼 메서드 */
  private String text(Element parent, String tag) {
    NodeList nl = parent.getElementsByTagName(tag);
    return nl.getLength() > 0 ? nl.item(0).getTextContent() : null;
  }

  private Double parseCoordinate(String value) {
    if (value == null || value.isBlank()) return null;
    try {
      return Double.parseDouble(value.trim());
    } catch (NumberFormatException e) {
      return null;
    }
  }
}
