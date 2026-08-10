package org.ict.datemanagerbackend.domain.user.repository;

import org.ict.datemanagerbackend.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    // 관리자 페이지 회원 검색 - 이메일 또는 닉네임에 검색어가 포함되면 매치 (대소문자 무시)
    Page<User> findByEmailContainingIgnoreCaseOrNicknameContainingIgnoreCase(
            String email, String nickname, Pageable pageable);
}
