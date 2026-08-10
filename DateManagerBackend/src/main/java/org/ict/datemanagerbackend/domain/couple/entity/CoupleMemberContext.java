package org.ict.datemanagerbackend.domain.couple.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
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

// battery(체력 갱신)와 note(메모 수정)만 생성 이후 바뀔 수 있어 개별 setter를 열어둔다.
@Entity
@Table(
    name = "couple_member_contexts",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_couple_member_contexts", columnNames = {"couple_daily_context_id", "user_id"})
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE) // Builder 전용, 외부에서 직접 호출 금지
@Builder
public class CoupleMemberContext {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id; // 개인별 컨텍스트 ID (PK)

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "couple_daily_context_id", nullable = false)
  private CoupleDailyContext coupleDailyContext; // 일자별 컨텍스트 (couple_daily_contexts.id 참조)

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user; // 유저 (users.id 참조)

  @Setter
  @Column(name = "battery")
  private Integer battery; // 당일 체력 게이지 (0~100)

  @Setter
  @Lob
  @Column(name = "note")
  private String note; // 특이사항 메모 (CLOB 매핑)

}
