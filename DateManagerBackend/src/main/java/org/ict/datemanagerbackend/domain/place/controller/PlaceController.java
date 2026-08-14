package org.ict.datemanagerbackend.domain.place.controller;

import org.ict.datemanagerbackend.domain.place.dto.PlaceResponseDto;
import org.ict.datemanagerbackend.domain.place.entity.Place;
import org.ict.datemanagerbackend.domain.place.entity.PlaceStyle;
import org.ict.datemanagerbackend.domain.place.repository.PlaceRepository;
import org.ict.datemanagerbackend.domain.place.repository.PlaceStyleRepository;
import org.ict.datemanagerbackend.domain.place.service.PlaceService;
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



}
