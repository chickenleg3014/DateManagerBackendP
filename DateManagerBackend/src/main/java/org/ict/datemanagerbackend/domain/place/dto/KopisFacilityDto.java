package org.ict.datemanagerbackend.domain.place.dto;

// KOPIS 공연시설 상세조회(prfplc/{mt10id}) 응답 중 places 적재에 필요한 필드만 담음
public record KopisFacilityDto(
    String mt10id,  // 공연시설 ID
    String adres,   // 주소
    String la,      // 위도
    String lo       // 경도
) {
}
