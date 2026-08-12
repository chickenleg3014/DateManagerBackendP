package org.ict.datemanagerbackend.domain.user.service;

import org.ict.datemanagerbackend.domain.user.entity.User;
import org.ict.datemanagerbackend.domain.user.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

// 관리자가 회원을 "삭제"하면 실제로는 users.withdrawn_at만 채우는 soft-delete로 처리되는데(AdminController 참고),
// 이 서비스가 매일 새벽 탈퇴 후 1년이 지난 계정을 찾아 실제로 DB에서 영구 삭제한다.
// 캘린더/AI채팅 등 연결된 데이터가 남아있어 FK 제약으로 삭제가 막히는 계정은 이번 배치는 건너뛰고 다음 배치에서 재시도한다.
@Service
public class WithdrawnUserCleanupService {

    private final UserRepository userRepository;

    public WithdrawnUserCleanupService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Scheduled(cron = "0 0 4 * * *")
    public void deleteLongWithdrawnUsers() {
        LocalDateTime cutoff = LocalDateTime.now().minusYears(1);
        List<User> targets = userRepository.findByWithdrawnAtBefore(cutoff);
        for (User user : targets) {
            try {
                userRepository.delete(user);
            } catch (DataIntegrityViolationException e) {
                // 연결된 데이터가 남아있어 삭제 불가 - 다음 배치 때 다시 시도
            }
        }
    }
}
