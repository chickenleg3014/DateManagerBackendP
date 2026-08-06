package org.ict.datemanagerbackend.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_styles")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 스펙상 기본 생성자 필수 (무분별한 객체 생성 방지)
@AllArgsConstructor
@Builder
public class UserStyle {

  @Id
  @Column(name = "user_id")
  private Long userId; // users.id와 1:1 관계 (PK이자 FK)

  @Column(name = "init_energy")
  private Integer initEnergy;

  @Column(name = "init_immersion")
  private Integer initImmersion;

  @Column(name = "init_vibe")
  private Integer initVibe;

  @Column(name = "init_aesthetic")
  private Integer initAesthetic;

  @Column(name = "init_pacing")
  private Integer initPacing;

  @Column(name = "init_depth")
  private Integer initDepth;

  @Column(name = "updated_at", insertable = false, updatable = false)
  private LocalDateTime updatedAt;

}