package com.ssafy.a107.api.service;

import com.ssafy.a107.api.request.MultiMeetingRoomCreationReq;
import com.ssafy.a107.api.request.MultiMeetingRoomJoinReq;
import com.ssafy.a107.api.request.MultiWebSocketReq;
import com.ssafy.a107.api.response.MeetingRoomRes;
import com.ssafy.a107.api.response.MultiChatFlag;
import com.ssafy.a107.api.response.MultiMeetingRoomRes;
import com.ssafy.a107.api.response.MultiWebSocketRes;
import com.ssafy.a107.common.exception.MeetingRoomAlreadyFullException;
import com.ssafy.a107.common.exception.NotFoundException;
import com.ssafy.a107.common.exception.UserAlreadyExistsInMultiMeetingRoomException;
import com.ssafy.a107.db.entity.MultiMeetingRoom;
import com.ssafy.a107.db.entity.MultiMeetingRoomUser;
import com.ssafy.a107.db.entity.User;
import com.ssafy.a107.db.repository.MultiMeetingRoomUserRepository;
import com.ssafy.a107.db.repository.MultiMeetingRoomRepository;
import com.ssafy.a107.db.repository.UserRepository;
import io.openvidu.java.client.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MultiMeetingRoomServiceImpl implements MultiMeetingRoomService {

    @Value("${openvidu.url}")
    private String OPENVIDU_URL;

    @Value("${openvidu.secret}")
    private String OPENVIDU_SECRET;

    private OpenVidu openVidu;

    @PostConstruct
    public void init() {
        this.openVidu = new OpenVidu(OPENVIDU_URL, OPENVIDU_SECRET);
    }

    private final MultiMeetingRoomRepository multiMeetingRoomRepository;
    private final MultiMeetingRoomUserRepository multiMeetingRoomUserRepository;
    private final UserRepository userRepository;

    private final SimpMessageSendingOperations simpMessageSendingOperations;

    /*
     *   1. Session Initialization, 2. create multimeetingroom
     * @return   MeetingRoomRes dto
     * */
    @Transactional
    @Override
    public Long initializeSession(MultiMeetingRoomCreationReq multiMeetingRoomCreationReq) throws OpenViduJavaClientException, OpenViduHttpException {
        //?????? ?????????
        Map<String, Object> params = Map.of();
        SessionProperties properties = SessionProperties.fromJson(params).build();
        Session session = openVidu.createSession(properties);
        //??????????????? ????????? ??????
        MultiMeetingRoom multiMeetingRoom = MultiMeetingRoom.builder()
                .title(multiMeetingRoomCreationReq.getTitle())
                .status((byte) 0)
                .sessionId(session.getSessionId())
                .build();
        //???????????? ?????????????????? multi_meeting_room???????????? ??????
        return multiMeetingRoomRepository.save(multiMeetingRoom).getSeq();
    }

    @Transactional
    @Override
    public MeetingRoomRes createConnection(MultiMeetingRoomJoinReq multiMeetingRoomJoinReq) throws NotFoundException, OpenViduJavaClientException, OpenViduHttpException {
        MultiMeetingRoom multiMeetingRoom = multiMeetingRoomRepository.findById(multiMeetingRoomJoinReq.getMultiMeetingRoomSeq())
                .orElseThrow(() -> new NotFoundException("Invalid multi meeting sequence!"));
        String sessionId = multiMeetingRoom.getSessionId();
        Session session = openVidu.getActiveSession(sessionId);
        if(session == null){
            throw new NotFoundException("session Id failed to connect to its session");
        }
        ConnectionProperties connectionProperties = new ConnectionProperties.Builder().build();
        Connection connection = session.createConnection(connectionProperties);
        String token = connection.getToken();
        return new MeetingRoomRes(multiMeetingRoomJoinReq.getMultiMeetingRoomSeq(), token);
    }

    /*
     * @return   MultiMeetingRoomRes dto
     * */
    @Override
    public MultiMeetingRoomRes findMultiMeetingRoom(Long multiMeetingRoomSeq) throws NotFoundException {
        return MultiMeetingRoomRes.builder()
                .roomEntity(multiMeetingRoomRepository.findById(multiMeetingRoomSeq)
                        .orElseThrow(() -> new NotFoundException("Invalid multiMeetingRoom sequence")))
                .maleNum(multiMeetingRoomRepository.countByMultiMeetingRoomSeqAndGender(multiMeetingRoomSeq, true))
                .femaleNum(multiMeetingRoomRepository.countByMultiMeetingRoomSeqAndGender(multiMeetingRoomSeq, false))
                .build();
    }

    @Override
    public List<MultiMeetingRoomRes> findAllMultiMeetingRoom() {
        return multiMeetingRoomRepository.findAll().stream()
                .map((multiMeetingRoom) -> MultiMeetingRoomRes.builder()
                        .roomEntity(multiMeetingRoom)
                        .maleNum(multiMeetingRoomRepository.countByMultiMeetingRoomSeqAndGender(multiMeetingRoom.getSeq(), true))
                        .femaleNum(multiMeetingRoomRepository.countByMultiMeetingRoomSeqAndGender(multiMeetingRoom.getSeq(), false))
                        .build())
                .collect(Collectors.toList());
    }

    //????????? ??????
    @Transactional
    @Override
    public void deleteMultiMeetingRoom(Long meetingRoomSeq) throws NotFoundException {
        if (!multiMeetingRoomRepository.existsById(meetingRoomSeq))
            throw new NotFoundException("Invalid multi meeting room sequence!");
        multiMeetingRoomUserRepository.deleteAllByMultiMeetingRoomSeq(meetingRoomSeq);
        multiMeetingRoomRepository.deleteById(meetingRoomSeq);
    }

    //???????????? ?????? ?????????, ????????????
    @Transactional
    @Override
    public Long saveUserToMultiMeetingRoom(Long multiMeetingRoomSeq, Long userSeq) throws NotFoundException, UserAlreadyExistsInMultiMeetingRoomException, MeetingRoomAlreadyFullException, InterruptedException {
        MultiMeetingRoom multiMeetingRoom = multiMeetingRoomRepository.findById(multiMeetingRoomSeq)
                .orElseThrow(() -> new NotFoundException("Invalid Multi meeting room sequence!"));
        User user = userRepository.findById(userSeq)
                .orElseThrow(() -> new NotFoundException("Invalid user sequence!"));
        if(multiMeetingRoomUserRepository.existsByMultiMeetingRoomSeqAndUserSeq(multiMeetingRoomSeq, userSeq)){
            throw new UserAlreadyExistsInMultiMeetingRoomException("User already exists in the meetingroom!");
        }

        if(user.getGender()){
            Long maleNum = multiMeetingRoomRepository.countByMultiMeetingRoomSeqAndGender(multiMeetingRoomSeq, true);
            if(maleNum>=3) throw new MeetingRoomAlreadyFullException("Men are already full!");
        }else{
            Long femaleNum = multiMeetingRoomRepository.countByMultiMeetingRoomSeqAndGender(multiMeetingRoomSeq, false);
            if(femaleNum>=3) throw new MeetingRoomAlreadyFullException("Women are already full!");
        }
        Long seq= multiMeetingRoomUserRepository.save(MultiMeetingRoomUser.builder()
                .multiMeetingRoom(multiMeetingRoom)
                .user(user)
                .build()).getSeq();
        sendToWebSocketAtJoin(multiMeetingRoomSeq, userSeq);
        return seq;
    }

    @Transactional
    @Override
    public void deleteUserFromMultiMeetingRoom(Long multiMeetingRoomSeq, Long userSeq) throws NotFoundException {
        if (!multiMeetingRoomRepository.existsById(multiMeetingRoomSeq))
            throw new NotFoundException("Invalid multi meeting room sequence!");
        else if (!userRepository.existsById(userSeq))
            throw new NotFoundException("Invalid user sequence!");

        multiMeetingRoomUserRepository.deleteByMultiMeetingRoomSeqAndUserSeq(multiMeetingRoomSeq, userSeq);

        Long maleNum = multiMeetingRoomRepository.countByMultiMeetingRoomSeqAndGender(multiMeetingRoomSeq, true);
        Long femaleNum = multiMeetingRoomRepository.countByMultiMeetingRoomSeqAndGender(multiMeetingRoomSeq, false);
        if(maleNum==0L && femaleNum==0L){
            multiMeetingRoomRepository.deleteById(multiMeetingRoomSeq);
        }else{
            sendToWebSocketAtExit(multiMeetingRoomSeq, userSeq);
        }
    }

    //    multimeetingroom ?????? ?????? ??? ????????? ?????? ???????????? ????????????????????? ????????? ????????? ??????
    @Override
    public void sendToWebSocketAtJoin(Long multiMeetingRoomSeq, Long userSeq) throws NotFoundException, InterruptedException {
        if (!multiMeetingRoomRepository.existsById(multiMeetingRoomSeq))
            throw new NotFoundException("Invalid multi meeting room sequence!");
        else if (!userRepository.existsById(userSeq))
            throw new NotFoundException("Invalid user sequence!");

        MultiWebSocketRes res = MultiWebSocketRes.builder()
                .senderSeq(userSeq)
                .multiMeetingRoomSeq(multiMeetingRoomSeq)
                .flag(MultiChatFlag.JOIN)
                .message("?????? " + userSeq + " ??? ??????????????????.")
                .maleNum(multiMeetingRoomRepository.countByMultiMeetingRoomSeqAndGender(multiMeetingRoomSeq, true))
                .femaleNum(multiMeetingRoomRepository.countByMultiMeetingRoomSeqAndGender(multiMeetingRoomSeq, false))
                .createdAt(LocalDateTime.now())
                .build();

        Thread.sleep(1000);

//        ?????? ??? ?????? ?????? ????????? ????????? ??????
        simpMessageSendingOperations.convertAndSend("/sub/multi/" + multiMeetingRoomSeq, res);
    }

    //    multimeetingroom ????????? ?????? ???????????? ????????????????????? ????????? ????????? ??????
    @Override
    public void sendToWebSocket(MultiWebSocketReq req) throws NotFoundException {
        if (!multiMeetingRoomRepository.existsById(req.getMultiMeetingRoomSeq()))
            throw new NotFoundException("Invalid multi meeting room sequence!");
        else if (!userRepository.existsById(req.getSenderSeq()))
            throw new NotFoundException("Invalid user sequence!");

        MultiWebSocketRes res = MultiWebSocketRes.builder()
                .senderSeq(req.getSenderSeq())
                .multiMeetingRoomSeq(req.getMultiMeetingRoomSeq())
                .flag(MultiChatFlag.CHAT)
                .message(req.getMessage())
                .maleNum(multiMeetingRoomRepository.countByMultiMeetingRoomSeqAndGender(req.getMultiMeetingRoomSeq(), true))
                .femaleNum(multiMeetingRoomRepository.countByMultiMeetingRoomSeqAndGender(req.getMultiMeetingRoomSeq(), false))
                .createdAt(LocalDateTime.now())
                .build();

//        ?????? ??? ?????? ?????? ????????? ????????? ??????
        simpMessageSendingOperations.convertAndSend("/sub/multi/" + req.getMultiMeetingRoomSeq(), res);
    }

    @Override
    public void sendToWebSocketAtExit(Long multiMeetingRoomSeq, Long userSeq) throws NotFoundException {
        if (!multiMeetingRoomRepository.existsById(multiMeetingRoomSeq))
            throw new NotFoundException("Invalid multi meeting room sequence!");
        else if (!userRepository.existsById(userSeq))
            throw new NotFoundException("Invalid user sequence!");

        MultiWebSocketRes res = MultiWebSocketRes.builder()
                .senderSeq(userSeq)
                .multiMeetingRoomSeq(multiMeetingRoomSeq)
//                ?????? flag??? ?????? ?????????
                .flag(MultiChatFlag.EXIT)
                .message("?????? " + userSeq + " ??? ??????????????????.")
                .maleNum(multiMeetingRoomRepository.countByMultiMeetingRoomSeqAndGender(multiMeetingRoomSeq, true))
                .femaleNum(multiMeetingRoomRepository.countByMultiMeetingRoomSeqAndGender(multiMeetingRoomSeq, false))
                .createdAt(LocalDateTime.now())
                .build();

        simpMessageSendingOperations.convertAndSend("/sub/multi/" + multiMeetingRoomSeq, res);
    }
}
