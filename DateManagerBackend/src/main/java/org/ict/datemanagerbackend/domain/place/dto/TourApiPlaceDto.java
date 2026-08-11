package org.ict.datemanagerbackend.domain.place.dto;

// 한국관광공사 TourAPI(KorService2) 지역기반 목록조회(areaBasedList2) 응답 중 places 적재에 필요한 필드만 담음
public record TourApiPlaceDto(
    String contentid,     // 콘텐츠 ID (외부 고유 식별자)
    String title,         // 장소명
    String addr1,         // 주소
    String addr2,         // 상세 주소
    String mapx,          // 경도
    String mapy,          // 위도
    String firstimage,    // 대표 이미지 URL
    String firstimage2    // 대표 이미지 URL(예비)
) {
}
