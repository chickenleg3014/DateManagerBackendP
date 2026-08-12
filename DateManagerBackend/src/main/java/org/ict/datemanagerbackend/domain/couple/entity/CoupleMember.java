package org.ict.datemanagerbackend.domain.couple.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.ict.datemanagerbackend.domain.user.entity.User;

import java.time.LocalDateTime;

/**
 * {@link Couple}과 {@link User}를 이어주는 중간(연결) 테이블 엔티티.
 * 하나의 Couple에는 이 CoupleMember 행이 정확히 2개(초대자 1 + 수락자 1) 생긴다.
 *
 * <p><b>left_at으로 "탈퇴"를 표현하는 이유(소프트 삭제)</b><br>
 * 연결이 끊어졌을 때 이 행을 DB에서 실제로 지우지 않고 leftAt에 시각만 기록한다.
 * 그래서 "지금 연결되어 있는가?"를 판단하려면 항상 {@code left_at IS NULL} 조건을 같이 봐야 한다
 * ({@link org.ict.datemanagerbackend.domain.couple.repository.CoupleMemberRepository#findByUser_IdAndLeftAtIsNull}).
 * 행을 지워버리면 "예전에 누구와 사귀었었는지" 같은 이력이 영구히 사라지기 때문에 이렇게 설계했다.
 */
// roleType(역할 위임)과 leftAt(탈퇴 처리)만 생성 이후 바뀔 수 있어 개별 setter를 열어둔다.
@Entity
@Table(
    name = "couple_members",
    uniqueConstraints = {
        // 같은 (couple_id, user_id) 조합이 중복 저장되는 걸 DB 레벨에서 막는다
        // (한 유저가 같은 커플에 두 번 가입하는 이상 상황 방지)
        @UniqueConstraint(name = "uq_couple_members", columnNames = {"couple_id", "user_id"})
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE) // Builder 전용, 외부에서 직접 호출 금지
@Builder
public class CoupleMember {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id; // 멤버십 ID (PK)

  // fetch = LAZY: 이 CoupleMember를 조회할 때 연결된 Couple을 항상 같이 조회(JOIN)하지 않고,
  // 실제로 getCouple()을 호출하는 시점에야 추가 쿼리로 가져온다(성능 최적화). 반대는 EAGER.
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "couple_id", nullable = false) // 이 컬럼이 couples.id를 참조하는 외래키(FK)
  private Couple couple; // 커플 (couples.id 참조)

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user; // 유저 (users.id 참조)

  @Setter
  @Column(name = "role_type", length = 20)
  private String roleType; // 권한 타입 (OWNER, PARTNER 등) - 아직 실제 코드에서 사용되는 곳은 없음(향후 확장용)

  @Column(name = "joined_at", insertable = false, updatable = false)
  private LocalDateTime joinedAt; // 참여(=커플 성사) 일시, DB 기본값으로 자동 기록

  @Setter
  @Column(name = "left_at")
  private LocalDateTime leftAt; // 이탈(연결 해제) 일시. null이면 "아직 연결되어 있다"는 뜻

}
