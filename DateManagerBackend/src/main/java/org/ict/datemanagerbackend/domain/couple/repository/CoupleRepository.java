package org.ict.datemanagerbackend.domain.couple.repository;

import org.ict.datemanagerbackend.domain.couple.entity.Couple;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoupleRepository extends JpaRepository<Couple, Long> {
}
