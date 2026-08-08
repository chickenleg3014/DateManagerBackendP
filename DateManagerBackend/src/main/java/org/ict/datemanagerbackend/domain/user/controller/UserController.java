package org.ict.datemanagerbackend.domain.user.controller;

import org.ict.datemanagerbackend.domain.user.entity.User;
import org.ict.datemanagerbackend.domain.user.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/me")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public record UpdateMeRequest(String nickname, String gender) {
    }

    public record ChangePasswordRequest(String currentPassword, String newPassword) {
    }

    @GetMapping
    public ResponseEntity<?> getMe(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "사용자를 찾을 수 없습니다"));
        }
        return ResponseEntity.ok(toResponse(userOpt.get()));
    }

    @PutMapping
    public ResponseEntity<?> updateMe(Authentication authentication, @RequestBody UpdateMeRequest req) {
        Long userId = (Long) authentication.getPrincipal();
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "사용자를 찾을 수 없습니다"));
        }
        User user = userOpt.get();
        if (req.nickname() != null && !req.nickname().isBlank()) {
            user.setNickname(req.nickname());
        }
        if (req.gender() != null && !req.gender().isBlank()) {
            user.setGender(req.gender());
        }
        userRepository.save(user);
        return ResponseEntity.ok(toResponse(user));
    }

    // 소셜 로그인으로만 가입해 passwordHash가 없는 계정은 "현재 비밀번호"가 없으므로
    // 비밀번호 변경이 아니라 신규 설정으로 취급해 currentPassword 검증을 건너뛴다.
    @PutMapping("/password")
    public ResponseEntity<?> changePassword(Authentication authentication, @RequestBody ChangePasswordRequest req) {
        if (req.newPassword() == null || req.newPassword().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "새 비밀번호를 입력해주세요"));
        }
        Long userId = (Long) authentication.getPrincipal();
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "사용자를 찾을 수 없습니다"));
        }
        User user = userOpt.get();
        if (user.getPasswordHash() != null) {
            if (req.currentPassword() == null || !passwordEncoder.matches(req.currentPassword(), user.getPasswordHash())) {
                return ResponseEntity.status(401).body(Map.of("error", "현재 비밀번호가 일치하지 않습니다"));
            }
        }
        user.setPasswordHash(passwordEncoder.encode(req.newPassword()));
        userRepository.save(user);
        return ResponseEntity.ok(Map.of("success", true));
    }

    private Map<String, Object> toResponse(User user) {
        return Map.of(
                "id", user.getId(),
                "email", user.getEmail(),
                "nickname", user.getNickname(),
                "gender", user.getGender()
        );
    }
}
