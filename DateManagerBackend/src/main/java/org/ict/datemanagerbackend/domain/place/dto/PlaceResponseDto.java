package org.ict.datemanagerbackend.domain.place.dto;

import org.ict.datemanagerbackend.domain.place.entity.Place;

// 큐레이션/코스빌더 화면에 내려주는 장소 응답 DTO
public record PlaceResponseDto(
    Long id,
    String name,
    String category,
    String address,
    Double latitude,
    Double longitude,
    String imageUrl
) {

  public static PlaceResponseDto from(Place place) {
    return new PlaceResponseDto(
        place.getId(),
        place.getName(),
        place.getCategory(),
        place.getAddress(),
        place.getLatitude(),
        place.getLongitude(),
        place.getImageUrl()
    );
  }
}
