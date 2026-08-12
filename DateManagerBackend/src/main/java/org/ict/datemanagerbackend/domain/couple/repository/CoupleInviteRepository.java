package org.ict.datemanagerbackend.domain.couple.repository;

import org.ict.datemanagerbackend.domain.couple.entity.CoupleInvite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CoupleInviteRepository extends JpaRepository<CoupleInvite, Long> {

  // findByToken(String) -> "WHERE token = ?" SQL을 자동 생성 (쿼리 메서드).
  // 프론트가 초대 수락 요청을 보낼 때 아는 값은 URL 경로에 들어있는 token뿐이라
  // (내부 PK인 id는 노출하지 않음), 이 메서드로 그 토큰에 해당하는 초대 행을 찾아온다.
  // Optional인 이유: 잘못되거나 존재하지 않는 토큰이 들어올 수 있어서, "없을 수도 있음"을
  // 타입으로 표현해 호출하는 쪽(CoupleController)이 null 체크 대신 .isEmpty()/.isPresent()로
  // 안전하게 분기 처리하도록 강제한다.
  Optional<CoupleInvite> findByToken(String token);

}
