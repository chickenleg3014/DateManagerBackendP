package org.ict.datemanagerbackend.auth;

import org.ict.datemanagerbackend.domain.user.repository.UserRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {

    private final UserRepository userRepository;

    public HealthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/api/health")
    public Map<String, Object> health() {
        long userCount = userRepository.count();
        return Map.of("ok", true, "userCount", userCount);
    }
}
