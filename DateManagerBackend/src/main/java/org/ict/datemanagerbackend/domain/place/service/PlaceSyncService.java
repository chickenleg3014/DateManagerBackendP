package org.ict.datemanagerbackend.domain.place.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.List;
import java.util.Optional;

/**
 * KOPIS(공연예술통합전산망) 공연 목록 API에서 데이터를 받아와 places 테이블에 채워 넣는 서비스.
 *
 * 원래 파이썬 스크립트(fetch_performance_data.py)로 하던 걸 그대로 백엔드 안으로 옮긴 것.
 * 흐름: [1] KOPIS에 HTTP 요청 → [2] XML 응답 받음 → [3] XML을 우리가 쓸 수 있는 객체로 파싱
 *      → [4] 이미 저장된 공연이면 갱신, 처음 보는 공연이면 새로 저장(upsert)
 */
@Service
@RequiredArgsConstructor // final 필드(placeRepository)를 인자로 받는 생성자를 롬복이 자동으로 만들어줌 (의존성 주입용)
@Slf4j // log.info(), log.error() 같은 로깅 기능을 롬복이 자동으로 만들어줌
public class PlaceSyncService {

  // KOPIS 공연목록 조회(pblprfr) API의 고정 주소
  private static final String KOPIS_LIST_URL = "http://www.kopis.or.kr/openApi/restful/pblprfr";

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

    int created = 0;
    int updated = 0;

    for (KopisPerformanceDto p : performances) {
      // 이미 저장된 공연인지, external_source + external_id 조합으로 확인 (KOPIS의 mt20id가 고유 식별자)
      Optional<Place> existing =
          placeRepository.findByExternalSourceAndExternalId(EXTERNAL_SOURCE, p.mt20id());

      if (existing.isPresent()) {
        // 이미 있으면 새로 만들지 않고, 이름/카테고리/포스터만 최신 값으로 덮어씀 (update)
        Place place = existing.get();
        place.setName(p.prfnm());
        place.setCategory(p.genrenm());
        place.setImageUrl(p.poster());
        placeRepository.save(place);
        updated++;
      } else {
        // 처음 보는 공연이면 새 Place 행을 만듦 (insert)
        // 주소/위경도는 이 목록 API에는 안 들어있어서 일단 비워둠 (TODO: 아래 참고)
        Place place = Place.builder()
            .name(p.prfnm())
            .category(p.genrenm())
            .imageUrl(p.poster())
            .externalSource(EXTERNAL_SOURCE)
            .externalId(p.mt20id())
            .build();
        placeRepository.save(place);
        created++;
      }
    }

    log.info("KOPIS 공연 동기화 완료 - 신규 {}건, 갱신 {}건 (전체 조회 {}건)",
        created, updated, performances.size());

    // TODO: 위도/경도, 주소는 KOPIS 공연시설 상세정보 API(prfplc)를 별도로 한 번 더 호출해야 얻을 수 있음.
    //       위 목록 API 응답의 mt10id(공연시설 ID)로 상세 조회를 걸면 되는데,
    //       상세 API의 정확한 응답 필드명은 KOPIS 공식 문서에서 직접 확인하고 채워 넣을 것.
  }

  /** KOPIS API를 호출해서 앞으로 30일간의 공연 목록을 받아옴 */
  private List<KopisPerformanceDto> fetchPerformances() {
    LocalDate today = LocalDate.now();
    // KOPIS는 날짜를 "yyyyMMdd" 형식 문자열로 받음 (예: 20260801)
    String stdate = today.format(DateTimeFormatter.BASIC_ISO_DATE);
    String eddate = today.plusDays(30).format(DateTimeFormatter.BASIC_ISO_DATE);

    // UriComponentsBuilder: URL에 ?key=value&key2=value2... 를 안전하게(URL 인코딩 포함) 붙여주는 도구
    String url = UriComponentsBuilder.fromUriString(KOPIS_LIST_URL)
        .queryParam("service", serviceKey)
        .queryParam("stdate", stdate)
        .queryParam("eddate", eddate)
        .queryParam("cpage", 1)   // 페이지 번호
        .queryParam("rows", 100) // 한 번에 가져올 개수
        .toUriString();

    // GET 요청을 보내고, 응답 body를 그대로 문자열(XML)로 받음
    String xml = restTemplate.getForObject(url, String.class);
    return parsePerformances(xml);
  }

  /** KOPIS가 돌려준 XML 문자열을 파싱해서 우리가 쓰기 편한 객체 리스트로 바꿔줌 */
  private List<KopisPerformanceDto> parsePerformances(String xml) {
    List<KopisPerformanceDto> result = new ArrayList<>();
    if (xml == null || xml.isBlank()) {
      return result;
    }

    try {
      // 자바에 기본 내장된 XML 파서를 사용 (별도 라이브러리 추가 없이 사용 가능)
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      // 외부 XML을 파싱할 때는 악성 XML(XXE 공격)을 막기 위해 DOCTYPE 선언을 금지시켜두는 게 안전함
      factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

      DocumentBuilder builder = factory.newDocumentBuilder();
      Document doc = builder.parse(new InputSource(new StringReader(xml)));

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
    } catch (Exception e) {
      // 파싱이 실패해도 애플리케이션 전체가 죽으면 안 되니, 로그만 남기고 빈 리스트를 반환함
      log.error("KOPIS 응답 파싱 실패", e);
    }

    return result;
  }

  /** <db> 안에서 특정 태그(tag)의 텍스트 값만 꺼내는 헬퍼 메서드 */
  private String text(Element parent, String tag) {
    NodeList nl = parent.getElementsByTagName(tag);
    return nl.getLength() > 0 ? nl.item(0).getTextContent() : null;
  }
}
