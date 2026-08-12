package org.ict.datemanagerbackend.domain.admin.controller;

import org.ict.datemanagerbackend.domain.couple.entity.Couple;
import org.ict.datemanagerbackend.domain.couple.entity.CoupleMember;
import org.ict.datemanagerbackend.domain.couple.repository.CoupleMemberRepository;
import org.ict.datemanagerbackend.domain.couple.repository.CoupleRepository;
import org.ict.datemanagerbackend.domain.place.repository.PlaceRepository;
import org.ict.datemanagerbackend.domain.place.service.MuseumSyncService;
import org.ict.datemanagerbackend.domain.place.service.PlaceSyncService;
import org.ict.datemanagerbackend.domain.place.service.TourApiSyncService;
import org.ict.datemanagerbackend.domain.report.entity.Report;
import org.ict.datemanagerbackend.domain.report.repository.ReportRepository;
import org.ict.datemanagerbackend.domain.user.entity.User;
import org.ict.datemanagerbackend.domain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

// 관리자가 한 명뿐이라 별도 role 체계 없이, application.yaml의 app.admin-email과
// 로그인한 유저의 이메일이 같은지만 확인하는 방식으로 관리자 권한을 처리한다.
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final CoupleRepository coupleRepository;
    private final CoupleMemberRepository coupleMemberRepository;
    private final PlaceRepository placeRepository;
    private final PlaceSyncService placeSyncService;
    private final TourApiSyncService tourApiSyncService;
    private final MuseumSyncService museumSyncService;
    private final ReportRepository reportRepository;

    @Value("${app.admin-email}")
    private String adminEmail;

    public AdminController(UserRepository userRepository, CoupleRepository coupleRepository,
                            CoupleMemberRepository coupleMemberRepository, PlaceRepository placeRepository,
                            PlaceSyncService placeSyncService, TourApiSyncService tourApiSyncService,
                            MuseumSyncService museumSyncService, ReportRepository reportRepository) {
        this.userRepository = userRepository;
        this.coupleRepository = coupleRepository;
        this.coupleMemberRepository = coupleMemberRepository;
        this.placeRepository = placeRepository;
        this.placeSyncService = placeSyncService;
        this.tourApiSyncService = tourApiSyncService;
        this.museumSyncService = museumSyncService;
        this.reportRepository = reportRepository;
    }

    public record AdminUserDto(Long id, String email, String nickname, String gender, LocalDateTime createdAt) {
    }

    public record AdminCoupleMemberDto(Long userId, String nickname, String email, String roleType) {
    }

    public record AdminCoupleDto(Long id, String status, LocalDateTime connectedAt, List<AdminCoupleMemberDto> members) {
    }

    public record AdminUpdateUserRequest(String nickname, String gender) {
    }

    public record AdminUpdateCoupleRequest(String status) {
    }

    public record AdminReportDto(Long id, Long reporterUserId, String reporterNickname, String targetType,
                                  Long targetId, String reason, String status, LocalDateTime createdAt) {
    }

    public record AdminUpdateReportRequest(String status) {
    }

    private static final Set<String> VALID_COUPLE_STATUSES = Set.of("ACTIVE", "DISCONNECTED");
    private static final Set<String> VALID_REPORT_STATUSES = Set.of("PENDING", "RESOLVED", "REJECTED");

    private User currentUser(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return userRepository.findById(userId).orElse(null);
    }

    private boolean isAdmin(User user) {
        return user != null && user.getEmail() != null
                && !adminEmail.isBlank()
                && user.getEmail().equalsIgnoreCase(adminEmail);
    }

    @GetMapping("/check")
    public ResponseEntity<?> check(Authentication authentication) {
        return ResponseEntity.ok(Map.of("isAdmin", isAdmin(currentUser(authentication))));
    }

    // 가입일 내림차순 15명씩 페이지네이션 + 이메일/닉네임 검색.
    // app.yaml의 one-indexed-parameters 설정 덕분에 page 쿼리파라미터는 1부터 시작한다.
    @GetMapping("/users")
    public ResponseEntity<?> listUsers(Authentication authentication,
                                        @RequestParam(required = false) String search,
                                        @PageableDefault(size = 15, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        if (!isAdmin(currentUser(authentication))) {
            return ResponseEntity.status(403).body(Map.of("error", "관리자만 접근할 수 있습니다"));
        }
        Page<User> page = (search == null || search.isBlank())
                ? userRepository.findAll(pageable)
                : userRepository.findByEmailContainingIgnoreCaseOrNicknameContainingIgnoreCase(search, search, pageable);
        Page<AdminUserDto> dtoPage = page.map(u ->
                new AdminUserDto(u.getId(), u.getEmail(), u.getNickname(), u.getGender(), u.getCreatedAt()));
        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping("/couples")
    public ResponseEntity<?> listCouples(Authentication authentication) {
        if (!isAdmin(currentUser(authentication))) {
            return ResponseEntity.status(403).body(Map.of("error", "관리자만 접근할 수 있습니다"));
        }
        List<AdminCoupleDto> couples = coupleRepository.findAll().stream()
                .map(this::toCoupleDto)
                .toList();
        return ResponseEntity.ok(couples);
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(Authentication authentication, @PathVariable Long id,
                                         @RequestBody AdminUpdateUserRequest req) {
        if (!isAdmin(currentUser(authentication))) {
            return ResponseEntity.status(403).body(Map.of("error", "관리자만 접근할 수 있습니다"));
        }
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("error", "회원을 찾을 수 없습니다"));
        }
        if (req.nickname() != null && !req.nickname().isBlank()) {
            user.setNickname(req.nickname());
        }
        if (req.gender() != null && !req.gender().isBlank()) {
            user.setGender(req.gender());
        }
        userRepository.save(user);
        return ResponseEntity.ok(new AdminUserDto(user.getId(), user.getEmail(), user.getNickname(),
                user.getGender(), user.getCreatedAt()));
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(Authentication authentication, @PathVariable Long id) {
        User me = currentUser(authentication);
        if (!isAdmin(me)) {
            return ResponseEntity.status(403).body(Map.of("error", "관리자만 접근할 수 있습니다"));
        }
        if (me.getId().equals(id)) {
            return ResponseEntity.badRequest().body(Map.of("error", "관리자 본인 계정은 삭제할 수 없습니다"));
        }
        if (!userRepository.existsById(id)) {
            return ResponseEntity.status(404).body(Map.of("error", "회원을 찾을 수 없습니다"));
        }
        try {
            userRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            // 캘린더/AI채팅/커플 등 연결된 데이터가 있으면 FK 제약으로 삭제가 막힌다
            return ResponseEntity.status(409).body(Map.of("error", "이 회원은 캘린더·채팅 등 연결된 데이터가 있어 삭제할 수 없습니다"));
        }
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PutMapping("/couples/{id}")
    public ResponseEntity<?> updateCouple(Authentication authentication, @PathVariable Long id,
                                           @RequestBody AdminUpdateCoupleRequest req) {
        if (!isAdmin(currentUser(authentication))) {
            return ResponseEntity.status(403).body(Map.of("error", "관리자만 접근할 수 있습니다"));
        }
        if (req.status() == null || !VALID_COUPLE_STATUSES.contains(req.status())) {
            return ResponseEntity.badRequest().body(Map.of("error", "status는 ACTIVE 또는 DISCONNECTED 여야 합니다"));
        }
        Couple couple = coupleRepository.findById(id).orElse(null);
        if (couple == null) {
            return ResponseEntity.status(404).body(Map.of("error", "커플을 찾을 수 없습니다"));
        }
        couple.setStatus(req.status());
        coupleRepository.save(couple);
        return ResponseEntity.ok(toCoupleDto(couple));
    }

    @DeleteMapping("/couples/{id}")
    public ResponseEntity<?> deleteCouple(Authentication authentication, @PathVariable Long id) {
        if (!isAdmin(currentUser(authentication))) {
            return ResponseEntity.status(403).body(Map.of("error", "관리자만 접근할 수 있습니다"));
        }
        if (!coupleRepository.existsById(id)) {
            return ResponseEntity.status(404).body(Map.of("error", "커플을 찾을 수 없습니다"));
        }
        try {
            List<CoupleMember> members = coupleMemberRepository.findByCoupleId(id);
            coupleMemberRepository.deleteAll(members);
            coupleRepository.deleteById(id);
        } catch (DataIntegrityViolationException e) {
            // 커플 기념일/AI채팅/코스 등 연결된 데이터가 있으면 FK 제약으로 삭제가 막힌다
            return ResponseEntity.status(409).body(Map.of("error", "이 커플은 기념일·채팅 등 연결된 데이터가 있어 삭제할 수 없습니다"));
        }
        return ResponseEntity.ok(Map.of("success", true));
    }

    @GetMapping("/reports")
    public ResponseEntity<?> listReports(Authentication authentication,
                                          @RequestParam(required = false) String status,
                                          @PageableDefault(size = 15, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        if (!isAdmin(currentUser(authentication))) {
            return ResponseEntity.status(403).body(Map.of("error", "관리자만 접근할 수 있습니다"));
        }
        Page<Report> page = (status == null || status.isBlank())
                ? reportRepository.findAll(pageable)
                : reportRepository.findByStatus(status, pageable);
        return ResponseEntity.ok(page.map(this::toReportDto));
    }

    @PutMapping("/reports/{id}")
    public ResponseEntity<?> updateReportStatus(Authentication authentication, @PathVariable Long id,
                                                 @RequestBody AdminUpdateReportRequest req) {
        if (!isAdmin(currentUser(authentication))) {
            return ResponseEntity.status(403).body(Map.of("error", "관리자만 접근할 수 있습니다"));
        }
        if (req.status() == null || !VALID_REPORT_STATUSES.contains(req.status())) {
            return ResponseEntity.badRequest().body(Map.of("error", "status는 PENDING, RESOLVED, REJECTED 중 하나여야 합니다"));
        }
        Report report = reportRepository.findById(id).orElse(null);
        if (report == null) {
            return ResponseEntity.status(404).body(Map.of("error", "신고 내역을 찾을 수 없습니다"));
        }
        report.setStatus(req.status());
        reportRepository.save(report);
        return ResponseEntity.ok(toReportDto(report));
    }

    private AdminReportDto toReportDto(Report r) {
        return new AdminReportDto(r.getId(), r.getReporter().getId(), r.getReporter().getNickname(),
                r.getTargetType(), r.getTargetId(), r.getReason(), r.getStatus(), r.getCreatedAt());
    }

    private AdminCoupleDto toCoupleDto(Couple couple) {
        List<AdminCoupleMemberDto> members = coupleMemberRepository.findByCoupleId(couple.getId()).stream()
                .map(cm -> new AdminCoupleMemberDto(cm.getUser().getId(), cm.getUser().getNickname(),
                        cm.getUser().getEmail(), cm.getRoleType()))
                .toList();
        return new AdminCoupleDto(couple.getId(), couple.getStatus(), couple.getConnectedAt(), members);
    }

    // 장소 데이터 동기화는 원래 매일 새벽에 자동(@Scheduled)으로만 도는데, 개발 중 수동으로
    // 바로 실행해서 결과를 확인하고 싶을 때 쓰는 관리자 전용 트리거. 외부 API를 여러 번
    // 호출하느라 응답이 오래 걸릴 수 있어(수십 초~수 분) 동기 방식으로 그대로 기다린다.
    @PostMapping("/places/sync/{source}")
    public ResponseEntity<?> triggerPlaceSync(Authentication authentication, @PathVariable String source) {
        if (!isAdmin(currentUser(authentication))) {
            return ResponseEntity.status(403).body(Map.of("error", "관리자만 접근할 수 있습니다"));
        }
        try {
            switch (source) {
                case "kopis" -> placeSyncService.syncPerformances();
                case "tourapi" -> tourApiSyncService.syncPlaces();
                case "museum" -> museumSyncService.syncMuseums();
                default -> {
                    return ResponseEntity.badRequest().body(Map.of("error", "source는 kopis, tourapi, museum 중 하나여야 합니다"));
                }
            }
        } catch (Exception e) {
            return ResponseEntity.status(502).body(Map.of("error", "동기화 중 오류가 발생했습니다: " + e.getMessage()));
        }
        return placesSyncStatus();
    }

    @GetMapping("/places/sync-status")
    public ResponseEntity<?> placesSyncStatusEndpoint(Authentication authentication) {
        if (!isAdmin(currentUser(authentication))) {
            return ResponseEntity.status(403).body(Map.of("error", "관리자만 접근할 수 있습니다"));
        }
        return placesSyncStatus();
    }

    private ResponseEntity<?> placesSyncStatus() {
        Map<String, Long> counts = new java.util.LinkedHashMap<>();
        for (Object[] row : placeRepository.countGroupedByCategory()) {
            counts.put((String) row[0], (Long) row[1]);
        }
        counts.put("전체", placeRepository.count());
        return ResponseEntity.ok(counts);
    }
}
