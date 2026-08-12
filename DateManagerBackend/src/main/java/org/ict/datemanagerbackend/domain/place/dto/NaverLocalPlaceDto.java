package org.ict.datemanagerbackend.domain.place.dto;

// 네이버 지역 검색 API(NAVER API HUB) 응답 items 중 places 적재에 필요한 필드만 담음.
// title은 검색어와 일치하는 부분이 <b></b>로 감싸져 내려오므로 태그를 제거한 뒤 담아야 함.
public record NaverLocalPlaceDto(
    String title,       // 업체명 (HTML 태그 제거된 상태)
    String address,      // 지번주소
    String roadAddress,  // 도로명주소
    String mapx,         // x좌표 (경도, WGS84 * 10^7 정수 문자열)
    String mapy          // y좌표 (위도, WGS84 * 10^7 정수 문자열)
) {
}
