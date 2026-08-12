package org.ict.datemanagerbackend.domain.place.dto;

// 한국문화정보원 문화정보조회서비스(cultureinfo API) 응답 중 places 적재에 필요한 필드만 담음
public record CultureEventDto(
    String seq,       // 문화정보 고유 번호 - 외부 소스 내 중복 확인용 식별자
    String title,     // 제목
    String place,     // 장소명
    String realmName, // 분야 (전시, 행사/축제, 교육/체험 등)
    String gpsX,       // 경도
    String gpsY,       // 위도
    String thumbnail  // 썸네일 이미지 URL
) {
}
