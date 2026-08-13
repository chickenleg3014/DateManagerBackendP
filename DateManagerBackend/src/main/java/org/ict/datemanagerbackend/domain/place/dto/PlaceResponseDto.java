package org.ict.datemanagerbackend.domain.place.dto;

import org.ict.datemanagerbackend.domain.place.entity.Place;
import org.ict.datemanagerbackend.domain.place.entity.PlaceStyle;

import java.time.LocalDate;

// 큐레이션/코스빌더 화면에 내려주는 장소 응답 DTO
// style은 아직 이 place의 place_styles 행이 없을 수도 있어서(예: 새로 동기화된 장소) null 허용.
// startDate ~ performanceState는 공연류(KOPIS) 장소만 값이 있고, 나머지 소스는 전부 null.
public record PlaceResponseDto(
    Long id,
    String name,
    String category,
    String address,
    Double latitude,
    Double longitude,
    String imageUrl,
    Integer scoreEnergy,
    Integer scoreImmersion,
    Integer scoreVibe,
    Integer scoreAesthetic,
    Integer scoreDepth,
    Integer isIndoor,
    Integer isActivity,
    LocalDate startDate,
    LocalDate endDate,
    String runtimeText,
    String priceInfo,
    String showTimeInfo,
    String bookingUrl,
    Integer isOpenRun,
    String performanceState
) {

  public static PlaceResponseDto from(Place place, PlaceStyle style) {
    return new PlaceResponseDto(
        place.getId(),
        place.getName(),
        place.getCategory(),
        place.getAddress(),
        place.getLatitude(),
        place.getLongitude(),
        place.getImageUrl(),
        style != null ? style.getScoreEnergy() : null,
        style != null ? style.getScoreImmersion() : null,
        style != null ? style.getScoreVibe() : null,
        style != null ? style.getScoreAesthetic() : null,
        style != null ? style.getScoreDepth() : null,
        style != null ? style.getIsIndoor() : null,
        style != null ? style.getIsActivity() : null,
        place.getStartDate(),
        place.getEndDate(),
        place.getRuntimeText(),
        place.getPriceInfo(),
        place.getShowTimeInfo(),
        place.getBookingUrl(),
        place.getIsOpenRun(),
        place.getPerformanceState()
    );
  }
}
