package org.ict.datemanagerbackend.auth;

import org.ict.datemanagerbackend.config.JwtService;
import org.ict.datemanagerbackend.user.User;
import org.ict.datemanagerbackend.user.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public record SignupRequest(String email, String nickname, String gender, String password) {
    }

    public record LoginRequest(String email, String password) {
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
        User user = userRepository.save(
                new User(req.email(), req.nickname(), gender, passwordEncoder.encode(req.password())));
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
        if (user.getPasswordHash() == null) {
            return ResponseEntity.status(400).body(Map.of("error", "소셜 로그인으로 가입된 계정입니다"));
        }
        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            return ResponseEntity.status(401).body(Map.of("error", "비밀번호가 일치하지 않습니다"));
        }
        String token = jwtService.generateToken(user.getId(), user.getEmail());
        return ResponseEntity.ok(Map.of("token", token));
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
