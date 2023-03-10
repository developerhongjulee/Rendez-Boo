package com.ssafy.a107.db.repository;

import com.ssafy.a107.db.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    Optional<ChatRoom> findBySeq(Long seq);

    List<ChatRoom> findByUserFemaleSeq(Long userFemaleSeq);

    List<ChatRoom> findByUserMaleSeq(Long userMaleSeq);

    Boolean existsByUserMaleSeqAndUserFemaleSeq(Long userMaleSeq, Long userFemaleSeq);

    ChatRoom findByUserMaleSeqAndUserFemaleSeq(Long userMaleSeq, Long userFemaleSeq);
}
