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

@Entity
@Table(
    name = "couple_member_contexts",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_couple_member_contexts", columnNames = {"couple_daily_context_id", "user_id"})
    }
)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
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

  @Column(name = "battery")
  private Integer battery; // 당일 체력 게이지 (0~100)

  @Lob
  @Column(name = "note")
  private String note; // 특이사항 메모 (CLOB 매핑)

}
