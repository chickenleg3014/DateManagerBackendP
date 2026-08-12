package org.ict.datemanagerbackend.domain.user.repository;

import org.ict.datemanagerbackend.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    // 관리자 페이지 회원 목록(탈퇴 회원 제외) - 검색어 없으면 전체, 있으면 이메일/닉네임 부분일치(대소문자 무시)
    @Query("SELECT u FROM User u WHERE u.withdrawnAt IS NULL "
            + "AND (:search IS NULL OR :search = '' OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.nickname) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<User> searchActive(@Param("search") String search, Pageable pageable);

    // 구독회원 탭 - 위와 동일 조건 + 활성 구독(status=ACTIVE)이 있는 회원만
    @Query("SELECT u FROM User u WHERE u.withdrawnAt IS NULL "
            + "AND (:search IS NULL OR :search = '' OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.nickname) LIKE LOWER(CONCAT('%', :search, '%'))) "
            + "AND EXISTS (SELECT 1 FROM Subscription s WHERE s.user = u AND s.status = 'ACTIVE')")
    Page<User> searchActiveSubscribed(@Param("search") String search, Pageable pageable);

    // 일반회원 탭 - 활성 구독이 없는 회원만
    @Query("SELECT u FROM User u WHERE u.withdrawnAt IS NULL "
            + "AND (:search IS NULL OR :search = '' OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.nickname) LIKE LOWER(CONCAT('%', :search, '%'))) "
            + "AND NOT EXISTS (SELECT 1 FROM Subscription s WHERE s.user = u AND s.status = 'ACTIVE')")
    Page<User> searchActiveFree(@Param("search") String search, Pageable pageable);

    // 탈퇴 후 cutoff 시점 이전에 탈퇴 처리된 계정 - 1년 경과 자동 삭제 배치에서 사용
    List<User> findByWithdrawnAtBefore(LocalDateTime cutoff);

    // 관리자 대시보드 "총 회원수" 통계용 - 탈퇴 회원 제외
    long countByWithdrawnAtIsNull();

    // 관리자 대시보드 남녀 비율 다이어그램용 - 탈퇴 회원 제외하고 성별별로 카운트
    long countByWithdrawnAtIsNullAndGender(String gender);
}
