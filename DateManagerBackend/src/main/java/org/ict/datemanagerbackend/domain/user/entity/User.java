package org.ict.datemanagerbackend.domain.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

// Setter는 실제로 값이 바뀌어야 하는 nickname/gender/passwordHash(비밀번호 변경)/withdrawnAt(탈퇴 처리)/
// profileImageUrl(프로필 사진 변경)에만 붙인다.
// id/email/createdAt은 생성 시점(Builder) 이후로는 절대 바뀌면 안 되는 값이라 setter를 열지 않는다.
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 스펙상 기본 생성자 필수 (무분별한 객체 생성 방지)
@AllArgsConstructor(access = AccessLevel.PRIVATE) // Builder 전용, 외부에서 직접 호출 금지
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Setter
    @Column(nullable = false)
    private String nickname;

    @Setter
    @Column(nullable = false)
    private String gender;

    @Setter
    @Column(name = "password_hash")
    private String passwordHash;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    // null이면 활동 중, 값이 있으면 탈퇴 처리된 계정 (soft-delete). 관리자 삭제는 이제 이 값만 채우고
    // 실제 row 삭제는 WithdrawnUserCleanupService가 탈퇴 1년 경과 후 배치로 처리한다.
    @Setter
    @Column(name = "withdrawn_at")
    private LocalDateTime withdrawnAt;

    // S3에 업로드된 프로필 사진의 공개 URL만 저장한다 (실제 파일은 DB에 안 넣음, places.image_url과 동일 패턴).
    // null이면 프론트가 성별 기반 기본 이미지로 대체해서 보여준다.
    @Setter
    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;
}
