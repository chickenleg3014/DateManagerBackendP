package org.ict.datemanagerbackend.domain.place.controller;

import org.ict.datemanagerbackend.domain.place.dto.PlaceResponseDto;
import org.ict.datemanagerbackend.domain.place.entity.Place;
import org.ict.datemanagerbackend.domain.place.entity.PlaceStyle;
import org.ict.datemanagerbackend.domain.place.repository.PlaceRepository;
import org.ict.datemanagerbackend.domain.place.repository.PlaceStyleRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

// KOPIS/TourAPI 동기화로 채워진 places 데이터를 큐레이션·코스빌더 화면에 내려주는 조회 API.
// 로그인 없이 보이는 홈 탭 추천 카드에서도 쓰이므로 인증 없이 공개한다(SecurityConfig에서 permitAll 처리).
@RestController
@RequestMapping("/api/places")
public class PlaceController {

  private final PlaceRepository placeRepository;
  private final PlaceStyleRepository placeStyleRepository;

  public PlaceController(PlaceRepository placeRepository, PlaceStyleRepository placeStyleRepository) {
    this.placeRepository = placeRepository;
    this.placeStyleRepository = placeStyleRepository;
  }

  // category를 안 넘기면 전체를 최신 등록순으로, 넘기면 해당 카테고리만 조회한다.
  @GetMapping
  public ResponseEntity<Page<PlaceResponseDto>> listPlaces(
      @RequestParam(required = false) String category,
      @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
    Page<Place> page = (category == null || category.isBlank())
        ? placeRepository.findAll(pageable)
        : placeRepository.findByCategory(category, pageable);

    // 이 페이지에 담긴 place id들의 PlaceStyle을 한 번에 조회해서 id로 바로 찾을 수 있는 Map으로 만든다
    // (장소 하나마다 따로 쿼리하면 N+1 문제가 생기기 때문).
    List<Long> placeIds = page.getContent().stream().map(Place::getId).toList();
    Map<Long, PlaceStyle> styleByPlaceId = placeStyleRepository.findByPlace_IdIn(placeIds).stream()
        .collect(Collectors.toMap(s -> s.getPlace().getId(), s -> s));

    return ResponseEntity.ok(page.map(place -> PlaceResponseDto.from(place, styleByPlaceId.get(place.getId()))));
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> getPlace(@PathVariable Long id) {
    Optional<Place> placeOpt = placeRepository.findById(id);
    if (placeOpt.isEmpty()) {
      return ResponseEntity.status(404).body(Map.of("error", "장소를 찾을 수 없습니다"));
    }
    PlaceStyle style = placeStyleRepository.findByPlace_Id(id).orElse(null);
    return ResponseEntity.ok(PlaceResponseDto.from(placeOpt.get(), style));
  }
}
