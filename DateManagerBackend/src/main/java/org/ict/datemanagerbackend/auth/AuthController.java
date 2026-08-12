package org.ict.datemanagerbackend.auth;

import org.ict.datemanagerbackend.config.JwtService;
import org.ict.datemanagerbackend.domain.user.entity.LoginLog;
import org.ict.datemanagerbackend.domain.user.entity.User;
import org.ict.datemanagerbackend.domain.user.repository.LoginLogRepository;
import org.ict.datemanagerbackend.domain.user.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

// 이메일/비밀번호 방식의 회원가입·로그인 API.
// 소셜로그인(OAuth2)은 이 컨트롤러를 거치지 않고 별도 흐름(SecurityConfig의 oauth2Login,
// 최종적으로 OAuth2LoginSuccessHandler)으로 처리된다 - 두 방식 모두 마지막엔 JwtService로 같은 형태의 JWT를 발급한다.
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final LoginLogRepository loginLogRepository;

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService,
                           LoginLogRepository loginLogRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.loginLogRepository = loginLogRepository;
    }

    public record SignupRequest(String email, String nickname, String gender, String password) {
    }

    public record LoginRequest(String email, String password) {
    }

    // 회원가입 폼에서 입력하는 즉시(제출 전) 중복 여부를 바로 확인하기 위한 API.
    // 실제 가입 차단(UX 게이트)은 프론트에서 프로젝트 막판에 켜기로 했고, 지금은 API만 먼저 열어둔다.
    @GetMapping("/check-email")
    public ResponseEntity<?> checkEmail(@RequestParam String email) {
        boolean available = !isBlank(email) && !userRepository.existsByEmail(email);
        return ResponseEntity.ok(Map.of("available", available));
    }

    @GetMapping("/check-nickname")
    public ResponseEntity<?> checkNickname(@RequestParam String nickname) {
        boolean available = !isBlank(nickname) && !userRepository.existsByNickname(nickname);
        return ResponseEntity.ok(Map.of("available", available));
    }

    // 이메일 회원가입: 비밀번호는 평문 저장 없이 passwordEncoder로 해싱해서 저장하고,
    // 가입 직후 바로 로그인된 상태로 만들기 위해 JWT까지 함께 발급해서 응답한다.
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest req) {
        if (isBlank(req.email()) || isBlank(req.password()) || isBlank(req.nickname())) {
            return ResponseEntity.badRequest().body(Map.of("error", "이메일, 닉네임, 비밀번호를 모두 입력해주세요"));
        }
        if (userRepository.existsByEmail(req.email())) {
            return ResponseEntity.status(409).body(Map.of("error", "이미 가입된 이메일입니다"));
        }
        String gender = isBlank(req.gender()) ? "UNKNOWN" : req.gender();
        User user = userRepository.save(User.builder()
                .email(req.email())
                .nickname(req.nickname())
                .gender(gender)
                .passwordHash(passwordEncoder.encode(req.password()))
                .build());
        recordLogin(user);
        String token = jwtService.generateToken(user.getId(), user.getEmail());
        return ResponseEntity.ok(Map.of("token", token));
    }

    // 이메일 로그인: passwordHash가 null이면 소셜로그인으로만 가입된 계정이라는 뜻이므로
    // 비밀번호 대조 자체를 막고 안내 메시지를 돌려준다 (그런 계정은 소셜로그인으로만 들어올 수 있음).
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        Optional<User> userOpt = userRepository.findByEmail(req.email());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("error", "가입되지 않은 이메일입니다"));
        }
        User user = userOpt.get();
        if (user.getWithdrawnAt() != null) {
            return ResponseEntity.status(403).body(Map.of("error", "탈퇴 처리된 계정입니다"));
        }
        if (user.getPasswordHash() == null) {
            return ResponseEntity.status(400).body(Map.of("error", "소셜 로그인으로 가입된 계정입니다"));
        }
        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            return ResponseEntity.status(401).body(Map.of("error", "비밀번호가 일치하지 않습니다"));
        }
        recordLogin(user);
        String token = jwtService.generateToken(user.getId(), user.getEmail());
        return ResponseEntity.ok(Map.of("token", token));
    }

    // 관리자 대시보드의 방문자 추이 그래프가 이 기록을 집계해서 그린다.
    private void recordLogin(User user) {
        loginLogRepository.save(LoginLog.builder()
                .user(user)
                .loggedInAt(LocalDateTime.now())
                .build());
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
