package org.ict.datemanagerbackend.domain.couple.repository;

import org.ict.datemanagerbackend.domain.couple.entity.CoupleMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CoupleMemberRepository extends JpaRepository<CoupleMember, Long> {

    // "쿼리 메서드(Query Method)" 기능: 메서드 이름을 정해진 규칙대로 지으면
    // Spring Data JPA가 이름을 분석해서 SQL을 자동으로 만들어준다(직접 @Query를 안 써도 됨).
    // findByCoupleId(Long) -> "WHERE couple_id = ?" 를 생성.
    // 한 커플의 멤버 전원(보통 2명, 나갔던 사람 포함)을 다 가져올 때 사용
    // (AdminController의 커플 상세 조회, CoupleController의 상대방 찾기에 쓰인다).
    List<CoupleMember> findByCoupleId(Long coupleId);

    // 이름 규칙 해설: findBy + User_Id + And + LeftAt + IsNull
    //   - "User_Id": CoupleMember가 갖고 있는 필드 user(User 타입)의 하위 필드 id를 본다는 뜻.
    //     밑줄(_)은 "여기서 한 번 더 들어간 연관 엔티티의 필드"라는 걸 명시적으로 구분해주는 표기법.
    //   - "LeftAtIsNull": left_at 컬럼이 NULL인 것만.
    // 최종 SQL: SELECT * FROM couple_members WHERE user_id = ? AND left_at IS NULL
    // "현재 연결이 끊어지지 않은(활성) 멤버십 조회" - CoupleController가 "이미 연결된 커플이 있는지"
    // 판단하는 모든 곳(초대 생성/수락/상태조회/연결해제)에서 이 메서드 하나로 처리한다.
    Optional<CoupleMember> findByUser_IdAndLeftAtIsNull(Long userId);
}
