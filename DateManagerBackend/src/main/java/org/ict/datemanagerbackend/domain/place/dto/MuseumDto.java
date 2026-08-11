package org.ict.datemanagerbackend.domain.place.dto;

// 전국박물관미술관정보표준데이터(tn_pubr_public_museum_artgr_info_api) 응답 중 places 적재에 필요한 필드만 담음
public record MuseumDto(
    String fcltyNm,   // 시설명
    String rdnmadr,   // 도로명주소
    String lnmadr,    // 지번주소 (도로명주소가 비어있을 때 대체용)
    String latitude,  // 위도
    String longitude  // 경도
) {
}
