package org.ict.datemanagerbackend.domain.user.controller;

import tools.jackson.databind.JsonNode;
import org.ict.datemanagerbackend.domain.user.entity.Subscription;
import org.ict.datemanagerbackend.domain.user.entity.User;
import org.ict.datemanagerbackend.domain.user.repository.SubscriptionRepository;
import org.ict.datemanagerbackend.domain.user.repository.UserRepository;
import org.ict.datemanagerbackend.domain.user.service.TossPaymentsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

// 구독(정기결제) API. 결제 흐름:
//   1) 프론트가 토스페이먼츠 결제위젯으로 카드 등록 인증을 마치면 authKey+customerKey를 받는다
//   2) POST /billing-key 로 그 값을 넘기면, 여기서 토스 API로 빌링키를 발급받아 Subscription에 저장한다
//   3) 이후 POST /charge 로 저장된 빌링키를 이용해 실제 결제를 승인 요청한다 (카드 재인증 불필요)
@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final TossPaymentsService tossPaymentsService;

    public SubscriptionController(SubscriptionRepository subscriptionRepository, UserRepository userRepository,
                                   TossPaymentsService tossPaymentsService) {
        this.subscriptionRepository = subscriptionRepository;
        this.userRepository = userRepository;
        this.tossPaymentsService = tossPaymentsService;
    }

    public record IssueBillingKeyRequest(String authKey, String customerKey, String planCode) {
    }

    public record ChargeRequest(int amount, String orderName) {
    }

    public record SubscriptionDto(Long id, String planCode, String status, LocalDateTime startedAt,
                                   LocalDateTime expiresAt, String paymentProvider, boolean hasBillingKey) {
    }

    @GetMapping("/me")
    public ResponseEntity<?> getMySubscription(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        Optional<Subscription> sub = subscriptionRepository.findTopByUserIdOrderByCreatedAtDesc(userId);
        if (sub.isEmpty()) {
            return ResponseEntity.ok(Map.of("hasSubscription", false));
        }
        return ResponseEntity.ok(toDto(sub.get()));
    }

    @PostMapping("/billing-key")
    public ResponseEntity<?> issueBillingKey(Authentication authentication, @RequestBody IssueBillingKeyRequest req) {
        Long userId = (Long) authentication.getPrincipal();
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("error", "사용자를 찾을 수 없습니다"));
        }
        if (req.authKey() == null || req.customerKey() == null || req.planCode() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "authKey, customerKey, planCode가 모두 필요합니다"));
        }
        JsonNode result;
        try {
            result = tossPaymentsService.issueBillingKey(req.authKey(), req.customerKey());
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(502).body(Map.of("error", "토스페이먼츠 빌링키 발급에 실패했습니다"));
        }
        String billingKey = result.path("billingKey").asText(null);
        if (billingKey == null) {
            return ResponseEntity.status(502).body(Map.of("error", "빌링키를 받지 못했습니다"));
        }
        Subscription subscription = Subscription.builder()
                .user(user)
                .planCode(req.planCode())
                .paymentProvider("TOSS")
                .billingKey(billingKey)
                .customerKey(req.customerKey())
                .build();
        subscriptionRepository.save(subscription);
        return ResponseEntity.ok(toDto(subscription));
    }

    @PostMapping("/charge")
    public ResponseEntity<?> charge(Authentication authentication, @RequestBody ChargeRequest req) {
        Long userId = (Long) authentication.getPrincipal();
        Subscription subscription = subscriptionRepository.findTopByUserIdOrderByCreatedAtDesc(userId).orElse(null);
        if (subscription == null || subscription.getBillingKey() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "등록된 빌링키가 없습니다. 먼저 카드를 등록해주세요"));
        }
        String orderId = UUID.randomUUID().toString();
        JsonNode result;
        try {
            result = tossPaymentsService.charge(subscription.getBillingKey(),
                    subscription.getCustomerKey(), req.amount(), orderId, req.orderName());
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(402).body(Map.of("error", "결제 승인에 실패했습니다"));
        }
        return ResponseEntity.ok(Map.of(
                "orderId", orderId,
                "status", result.path("status").asText(null),
                "approvedAt", result.path("approvedAt").asText(null)
        ));
    }

    private SubscriptionDto toDto(Subscription s) {
        return new SubscriptionDto(s.getId(), s.getPlanCode(), s.getStatus(), s.getStartedAt(),
                s.getExpiresAt(), s.getPaymentProvider(), s.getBillingKey() != null);
    }
}
