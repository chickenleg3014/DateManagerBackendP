package org.ict.datemanagerbackend.domain.couple.repository;

import org.ict.datemanagerbackend.domain.couple.entity.CoupleMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CoupleMemberRepository extends JpaRepository<CoupleMember, Long> {
    List<CoupleMember> findByCoupleId(Long coupleId);
}
