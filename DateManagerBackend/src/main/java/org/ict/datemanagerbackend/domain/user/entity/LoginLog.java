package org.ict.datemanagerbackend.domain.user.entity;

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

import java.time.LocalDateTime;

// 로그인 성공(이메일/소셜 공통) 시마다 한 줄씩 쌓인다. 관리자 대시보드의 "오늘 방문자"/방문자 추이
// 그래프가 이 테이블을 집계해서 그린다. loggedInAt은 DB 기본값이 아니라 애플리케이션(LocalDateTime.now())이
// 직접 채우므로 insertable=false를 쓰지 않는다 - 새로 만든 테이블이라 DB 기본값 자체가 없다.
@Entity
@Table(name = "login_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class LoginLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private LocalDateTime loggedInAt;
}
