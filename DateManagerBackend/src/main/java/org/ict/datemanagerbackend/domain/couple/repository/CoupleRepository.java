package org.ict.datemanagerbackend.domain.couple.repository;

import org.ict.datemanagerbackend.domain.couple.entity.Couple;
import org.springframework.data.jpa.repository.JpaRepository;

// JpaRepository<Couple, Long>만 상속하면 save/findById/findAll/delete 같은 기본 CRUD 메서드를
// Spring Data JPA가 인터페이스 선언만 보고 알아서 구현해준다(직접 SQL이나 구현 클래스를 안 짜도 됨).
// <Couple, Long>의 의미: 이 리포지토리가 다루는 엔티티는 Couple, 그 PK 타입은 Long이라는 뜻.
//
// 지금은 커스텀 조회 메서드가 하나도 없다 - CoupleController에서 save()/findById()만으로 충분하기 때문.
// (커플을 "누구와 누구의 관계인지"로 찾는 건 CoupleMember 쪽에서 user_id로 찾아 들어가는 방식이라
//  Couple 자체를 조건으로 검색할 일이 아직 없다.)
public interface CoupleRepository extends JpaRepository<Couple, Long> {
}
