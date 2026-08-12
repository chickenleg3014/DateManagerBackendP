package org.ict.datemanagerbackend.domain.notice.controller;

import org.ict.datemanagerbackend.domain.notice.entity.Notice;
import org.ict.datemanagerbackend.domain.notice.repository.NoticeRepository;
import org.ict.datemanagerbackend.domain.user.entity.User;
import org.ict.datemanagerbackend.domain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

// 관리자만 글을 올릴 수 있는 공지사항 게시판. 관리자가 한 명뿐이라 role 체계 없이
// AdminController와 동일한 방식(app.admin-email과 로그인 이메일 비교)으로 인가 처리한다.
// 지금은 관리자 페이지 안에서만 쓰는 용도라 조회도 관리자 인증을 요구한다.
@RestController
@RequestMapping("/api/admin/notices")
public class AdminNoticeController {

    private final NoticeRepository noticeRepository;
    private final UserRepository userRepository;

    @Value("${app.admin-email}")
    private String adminEmail;

    public AdminNoticeController(NoticeRepository noticeRepository, UserRepository userRepository) {
        this.noticeRepository = noticeRepository;
        this.userRepository = userRepository;
    }

    public record NoticeDto(Long id, String title, String content, LocalDateTime createdAt, LocalDateTime updatedAt) {
    }

    public record NoticeRequest(String title, String content) {
    }

    private boolean isAdmin(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        User user = userRepository.findById(userId).orElse(null);
        return user != null && user.getEmail() != null
                && !adminEmail.isBlank()
                && user.getEmail().equalsIgnoreCase(adminEmail);
    }

    @GetMapping
    public ResponseEntity<?> list(Authentication authentication,
                                   @PageableDefault(size = 10) Pageable pageable) {
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(403).body(Map.of("error", "관리자만 접근할 수 있습니다"));
        }
        Page<Notice> page = noticeRepository.findAllByOrderByCreatedAtDesc(pageable);
        return ResponseEntity.ok(page.map(this::toDto));
    }

    @PostMapping
    public ResponseEntity<?> create(Authentication authentication, @RequestBody NoticeRequest req) {
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(403).body(Map.of("error", "관리자만 공지를 작성할 수 있습니다"));
        }
        if (isBlank(req.title()) || isBlank(req.content())) {
            return ResponseEntity.badRequest().body(Map.of("error", "제목과 내용을 모두 입력해주세요"));
        }
        Notice saved = noticeRepository.save(Notice.builder()
                .title(req.title())
                .content(req.content())
                .createdAt(LocalDateTime.now())
                .build());
        return ResponseEntity.ok(toDto(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(Authentication authentication, @PathVariable Long id, @RequestBody NoticeRequest req) {
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(403).body(Map.of("error", "관리자만 공지를 수정할 수 있습니다"));
        }
        Notice notice = noticeRepository.findById(id).orElse(null);
        if (notice == null) {
            return ResponseEntity.status(404).body(Map.of("error", "공지를 찾을 수 없습니다"));
        }
        if (!isBlank(req.title())) {
            notice.setTitle(req.title());
        }
        if (!isBlank(req.content())) {
            notice.setContent(req.content());
        }
        notice.setUpdatedAt(LocalDateTime.now());
        noticeRepository.save(notice);
        return ResponseEntity.ok(toDto(notice));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(Authentication authentication, @PathVariable Long id) {
        if (!isAdmin(authentication)) {
            return ResponseEntity.status(403).body(Map.of("error", "관리자만 공지를 삭제할 수 있습니다"));
        }
        if (!noticeRepository.existsById(id)) {
            return ResponseEntity.status(404).body(Map.of("error", "공지를 찾을 수 없습니다"));
        }
        noticeRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private NoticeDto toDto(Notice n) {
        return new NoticeDto(n.getId(), n.getTitle(), n.getContent(), n.getCreatedAt(), n.getUpdatedAt());
    }
}
