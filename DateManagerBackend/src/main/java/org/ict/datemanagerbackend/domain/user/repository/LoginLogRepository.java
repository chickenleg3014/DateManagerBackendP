package org.ict.datemanagerbackend.domain.user.repository;

import org.ict.datemanagerbackend.domain.user.entity.LoginLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface LoginLogRepository extends JpaRepository<LoginLog, Long> {
    // 대시보드 방문자 추이 집계용 - cutoff 이후의 로그인 기록을 전부 가져와 컨트롤러에서 날짜별로 묶는다
    List<LoginLog> findByLoggedInAtAfter(LocalDateTime cutoff);
}
