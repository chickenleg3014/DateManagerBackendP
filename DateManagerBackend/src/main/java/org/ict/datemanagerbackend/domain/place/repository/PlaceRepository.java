package org.ict.datemanagerbackend.domain.place.repository;

import org.ict.datemanagerbackend.domain.place.entity.Place;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// JpaRepository<Place, Long>을 상속만 하면, save/findById/findAll/delete 같은 기본 CRUD 메서드를
// Spring Data JPA가 인터페이스만 보고 자동으로 구현체를 만들어줌 (직접 구현 코드 안 짜도 됨)
public interface PlaceRepository extends JpaRepository<Place, Long> {

  // 메서드 이름 자체가 쿼리가 됨 (Query Method) - "findBy" 뒤에 필드명을 이어붙이면
  // Spring Data JPA가 "WHERE external_source = ? AND external_id = ?" SQL을 자동 생성함.
  // KOPIS 동기화 시 "이 공연을 이미 저장한 적 있는지" 확인하는 용도로 사용.
  Optional<Place> findByExternalSourceAndExternalId(String externalSource, String externalId);

  // 큐레이션/코스빌더에서 카테고리(맛집, 숙박 등)별로 장소를 페이지 단위 조회할 때 사용.
  Page<Place> findByCategory(String category, Pageable pageable);

}
