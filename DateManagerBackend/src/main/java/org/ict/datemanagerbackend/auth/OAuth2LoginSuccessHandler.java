package org.ict.datemanagerbackend.auth;

import org.ict.datemanagerbackend.config.JwtService;
import org.ict.datemanagerbackend.user.SocialAccount;
import org.ict.datemanagerbackend.user.SocialAccountRepository;
import org.ict.datemanagerbackend.user.User;
import org.ict.datemanagerbackend.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

// 구글/카카오 로그인이 "성공"했을 때(=OAuth2 제공자가 사용자 인증을 마쳤을 때) 스프링 시큐리티가
// 호출해주는 핸들러. 여기서 하는 일:
//   1) 구글/카카오가 준 사용자 정보(attrs)에서 provider별로 다른 필드명을 우리 쪽 공통 필드로 변환
//   2) 이미 연동된 계정인지 / 같은 이메일의 기존 계정이 있는지 확인해서 로그인시킬 User를 결정(또는 신규 생성)
//   3) 우리 서비스 자체 JWT를 발급해서 프론트엔드로 리다이렉트 (구글/카카오 토큰을 그대로 쓰지 않음)
// SecurityConfig에서 oauth2Login(oauth2 -> oauth2.successHandler(successHandler))로 등록되어 있다.
@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final SocialAccountRepository socialAccountRepository;
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final JwtService jwtService;

    @Value("${app.frontend-base-url}")
    private String frontendBaseUrl;

    public OAuth2LoginSuccessHandler(UserRepository userRepository,
                                      SocialAccountRepository socialAccountRepository,
                                      OAuth2AuthorizedClientService authorizedClientService,
                                      JwtService jwtService) {
        this.userRepository = userRepository;
        this.socialAccountRepository = socialAccountRepository;
        this.authorizedClientService = authorizedClientService;
        this.jwtService = jwtService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                         Authentication authentication) throws IOException {
        // registrationId는 application.yaml에 등록한 이름 그대로 "google" 또는 "kakao"로 들어온다
        OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authentication;
        String registrationId = authToken.getAuthorizedClientRegistrationId();
        // 구글/카카오가 각자 다른 JSON 구조로 사용자 정보를 내려주는데, attrs는 그걸 그대로 담은 맵
        Map<String, Object> attrs = ((OAuth2User) authentication.getPrincipal()).getAttributes();

        String providerUserId;
        String email;
        String nickname;

        if ("google".equals(registrationId)) {
            // 구글은 OIDC 표준 필드(sub=고유id, email, name)를 그대로 준다
            providerUserId = String.valueOf(attrs.get("sub"));
            email = (String) attrs.get("email");
            nickname = attrs.get("name") != null ? (String) attrs.get("name") : email;
        } else {
            // 카카오는 attrs.get("id")가 고유 id이고, 닉네임은 kakao_account.profile.nickname 안에 중첩되어 있음.
            // scope에 email 동의를 안 받았기 때문에(application.yaml scope: profile_nickname만 요청)
            // 실제 카카오 이메일은 안 주고, provider id로 우리 쪽에서 가짜 이메일을 만들어 쓴다.
            providerUserId = String.valueOf(attrs.get("id"));
            @SuppressWarnings("unchecked")
            Map<String, Object> kakaoAccount = (Map<String, Object>) attrs.get("kakao_account");
            String kakaoNickname = null;
            if (kakaoAccount != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
                if (profile != null) {
                    kakaoNickname = (String) profile.get("nickname");
                }
            }
            nickname = kakaoNickname != null ? kakaoNickname : "카카오유저";
            email = "kakao_" + providerUserId + "@kakao.local";
        }

        // DB의 social_accounts.provider 컬럼과 맞추기 위해 대문자로 통일 (GOOGLE / KAKAO)
        String providerKey = registrationId.toUpperCase();

        // 이미 이 소셜 계정으로 연동된 적이 있으면 -> 그 유저로 바로 로그인 (이메일 재확인 불필요)
        java.util.Optional<SocialAccount> existingLink =
                socialAccountRepository.findByProviderAndProviderUserId(providerKey, providerUserId);

        User user;
        if (existingLink.isPresent()) {
            // 이전에 이 소셜 계정으로 로그인한 적이 있음 -> social_accounts에 연결된 user를 그대로 사용
            user = userRepository.findById(existingLink.get().getUserId())
                    .orElseThrow(() -> new IllegalStateException("연동된 유저를 찾을 수 없습니다"));
        } else {
            java.util.Optional<User> existingByEmail = userRepository.findByEmail(email);
            if (existingByEmail.isPresent() && existingByEmail.get().getPasswordHash() != null) {
                // 이미 이메일/비밀번호로 가입된 계정 -> 무단 연동 방지, 소셜 로그인 차단
                String redirectUrl = frontendBaseUrl + "/oauth/callback?error="
                        + URLEncoder.encode("이미 일반 회원가입으로 등록된 이메일입니다. 기존 계정으로 로그인해주세요", StandardCharsets.UTF_8);
                response.sendRedirect(redirectUrl);
                return;
            }
            // 같은 이메일의 순수 소셜 계정(다른 provider)이 있으면 그 유저에 연동, 없으면 신규 생성
            user = existingByEmail.orElseGet(() -> userRepository.save(User.builder()
                    .email(email)
                    .nickname(nickname)
                    .gender("UNKNOWN")
                    .passwordHash(null)
                    .build()));

            OAuth2AuthorizedClient authorizedClient =
                    authorizedClientService.loadAuthorizedClient(registrationId, authToken.getName());
            String accessTokenValue = authorizedClient != null
                    ? authorizedClient.getAccessToken().getTokenValue()
                    : null;
            socialAccountRepository.save(SocialAccount.builder()
                    .userId(user.getId())
                    .provider(providerKey)
                    .providerUserId(providerUserId)
                    .accessToken(accessTokenValue)
                    .build());
        }

        // 여기까지 오면 user가 확정된 상태. 우리 서비스 전용 JWT를 만들어서
        // 프론트엔드의 /oauth/callback 라우트로 쿼리스트링에 실어 리다이렉트한다.
        // (프론트는 이 token 쿼리파라미터를 읽어 localStorage에 저장 -> 이후 요청부터 Authorization 헤더로 사용)
        String jwt = jwtService.generateToken(user.getId(), user.getEmail());
        String redirectUrl = frontendBaseUrl + "/oauth/callback?token=" + URLEncoder.encode(jwt, StandardCharsets.UTF_8);
        response.sendRedirect(redirectUrl);
    }
}
