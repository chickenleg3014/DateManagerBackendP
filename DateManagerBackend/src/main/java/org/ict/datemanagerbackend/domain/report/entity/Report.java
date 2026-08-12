package org.ict.datemanagerbackend.domain.report.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.ict.datemanagerbackend.domain.user.entity.User;

import java.time.LocalDateTime;

// 게시판/댓글 등 신고 대상이 될 도메인이 아직 없어서, FK 대신 targetType(USER/POST/COMMENT 등)+targetId
// 조합으로 신고 대상을 범용적으로 참조한다. 나중에 게시판이 생겨도 이 테이블은 그대로 재사용 가능.
// createdAt은 다른 도메인(Place, User 등)과 동일한 이유로 DB 기본값이 없어 @Builder.Default로 애플리케이션이 채운다.
@Entity
@Table(name = "reports")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_user_id", nullable = false)
    private User reporter; // 신고한 사람

    @Column(name = "target_type", nullable = false)
    private String targetType; // 신고 대상 종류 (USER, POST, COMMENT 등 - 게시판 도입 후 확장)

    @Column(name = "target_id", nullable = false)
    private Long targetId; // 신고 대상의 id (target_type 도메인의 PK)

    @Column(nullable = false, length = 1000)
    private String reason; // 신고 사유

    @Setter
    @Builder.Default
    @Column(nullable = false)
    private String status = "PENDING"; // 처리 상태 (PENDING, RESOLVED, REJECTED)

    @Builder.Default
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
