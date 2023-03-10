package com.ssafy.a107.api.controller;

import com.ssafy.a107.api.request.SmsReq;
import com.ssafy.a107.api.response.SmsRes;
import com.ssafy.a107.api.service.SmsService;
import com.ssafy.a107.api.service.UserService;
import com.ssafy.a107.common.exception.BadRequestException;
import com.ssafy.a107.common.exception.ConflictException;
import com.ssafy.a107.common.exception.SmsException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Api(value = "SMS API", tags = {"SMS"})
@RestController
@RequestMapping("/api/sms")
@RequiredArgsConstructor
@Slf4j
public class SmsController {

    private final SmsService smsService;
    private final UserService userService;

    @ApiOperation(value = "사용자에게 인증번호 전송", notes = "번호는 01012345678 형태로 입력")
    @PostMapping("/send")
    public ResponseEntity<SmsRes> sendOne(@RequestBody SmsReq smsReq) throws BadRequestException, SmsException, ConflictException {

        // 인증번호 보내기 전 smsReq가 유효한지, 이미 가입한 번호인지 확인
        smsService.checkSmsReq(smsReq);
        userService.checkPhoneNumberDuplicate(smsReq.getPhoneNumber());

        String code = smsService.makeRandomCode();
        log.debug("Generated code: {}", code);

        smsService.sendSmsToUser(smsReq.getPhoneNumber(), code);

        // SMS 전송 정상 접수
        log.debug("Sent SMS to {}", smsReq.getPhoneNumber());
        smsService.createSms(smsReq, code.toString());
        log.debug("SMS info has been created in Redis.");

        SmsRes res = SmsRes.builder()
                .phoneNumber(smsReq.getPhoneNumber())
                .code(code)
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(res);
    }

    @ApiOperation(value = "인증번호가 일치하는지 확인")
    @PostMapping("/check")
    public ResponseEntity<SmsRes> checkCode(@RequestBody SmsReq smsReq) throws BadRequestException, SmsException, ConflictException {

        // 잘못된 인증코드 체크
        smsService.checkCode(smsReq.getCode(), "user");

        String phoneNumber = smsReq.getPhoneNumber();
        String codeFromRedis = smsService.getRecentCodeByPhoneNumber(phoneNumber);

        // 시간 초과 or 서버 에러 체크
        smsService.checkCode(codeFromRedis, "redis");

        log.debug("Code from Redis: {}", codeFromRedis);
        log.debug("Code typed by user: {}", smsReq.getCode());

        // 인증번호가 일치하는지 체크
        smsService.matchCodes(codeFromRedis, smsReq.getCode());
        log.debug("Two codes match.");

        return ResponseEntity.status(HttpStatus.OK).body(new SmsRes(smsReq));
    }
}
