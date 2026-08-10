package org.ict.datemanagerbackend.domain.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// Setter는 실제로 값이 바뀌어야 하는 init_* 점수 필드에만 붙인다 (성향 재검사 시 갱신됨).
// user는 users.id와 PK를 공유하는 1:1 관계라 생성 이후 절대 바뀌면 안 되고, updatedAt은 DB가 관리하는 값이라 setter를 열지 않는다.
@Entity
@Table(name = "user_styles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 스펙상 기본 생성자 필수 (무분별한 객체 생성 방지)
@AllArgsConstructor(access = AccessLevel.PRIVATE) // Builder 전용, 외부에서 직접 호출 금지
@Builder
public class UserStyle {

  @Id
  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user; // users.id와 PK를 공유하는 1:1 관계

  @Setter
  @Column(name = "init_energy")
  private Integer initEnergy;

  @Setter
  @Column(name = "init_immersion")
  private Integer initImmersion;

  @Setter
  @Column(name = "init_vibe")
  private Integer initVibe;

  @Setter
  @Column(name = "init_aesthetic")
  private Integer initAesthetic;

  @Setter
  @Column(name = "init_pacing")
  private Integer initPacing;

  @Setter
  @Column(name = "init_depth")
  private Integer initDepth;

  @Column(name = "updated_at", insertable = false, updatable = false)
  private LocalDateTime updatedAt;

}
